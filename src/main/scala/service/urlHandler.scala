package service

import actors.urlActor._
import actors.urlActor.system.executionContext
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import db.DatabaseConnection._
import models.JsonFormats
import slick.jdbc.PostgresProfile.api._
import tables.URLMappingTable

import java.net.URI
import java.security.SecureRandom
import java.time.{ZoneOffset, ZonedDateTime}
import scala.concurrent.Future
import scala.tools.nsc.tasty.SafeEq

object urlHandler extends JsonFormats with SprayJsonSupport {
  def shortenUrl(request: ShortenUrlRequest): Future[ApplicationResponse] = {
    val originalUrl: String = request.originalUrl.trim

    if (!isValidUrl(originalUrl)) {
      Future.successful(ApplicationResponse(StatusCodes.InternalServerError.intValue, null, "Invalid URL"))
    } else {
      val mayBeExpiry = request.expiryDays.map(days => ZonedDateTime.now(ZoneOffset.UTC).plusDays(days))
      val now = ZonedDateTime.now(ZoneOffset.UTC)

      val urlsTable = TableQuery[URLMappingTable]

      val query = mayBeExpiry match {
        case Some(expiryDate) =>
          urlsTable.filter( row =>
            row.originalUrl === originalUrl &&
            row.expiry === Option(expiryDate))

        case None =>
          urlsTable.filter(row =>
            row.originalUrl === originalUrl &&
              (row.expiry.isDefined === false)
          )
      }

      db.run(query.result.headOption).flatMap {
        case Some(existing) =>
          // Optional: You may remove this if clickCount is only meant to increase on redirects
          val updatedClickCount = existing.clickCount + 1
          val updateQuery = urlsTable.filter(_.id === existing.id.get)
            .map(_.clickCount)
            .update(updatedClickCount)

          db.run(updateQuery).map { _ =>
            val response = ShortenUrlResponse(
              id            =   existing.id.get,
              originalUrl   =   existing.originalUrl,
              shortCode     =   existing.shortCode,
              createdAt     =   existing.createdAt.toString,
              expiry        =   existing.expiry.map(x => x.toString).getOrElse(""),
              clickCount    =   updatedClickCount
            )
            ApplicationResponse(StatusCodes.OK.intValue, response, "URL already shortened")
          }

        case None =>
          val shortCode = generateShortCode()
          val newRow = URLMapping(
            id          =   None,
            originalUrl =   originalUrl,
            shortCode   =   shortCode,
            createdAt   =   now,
            expiry      =   mayBeExpiry,
            clickCount  =   0
          )

          db.run(URLMappingTable.table returning URLMappingTable.table.map(_.id) into ((url, id) => url.copy(id = Some(id))) += newRow)
            .map { savedRow =>
              val response = ShortenUrlResponse(
                id           =   savedRow.id.get,
                originalUrl  =   savedRow.originalUrl,
                shortCode    =   savedRow.shortCode,
                createdAt    =   savedRow.createdAt.toString,
                expiry       =   savedRow.expiry.map(x => x.toString).getOrElse(""),
                clickCount   =   savedRow.clickCount
              )
              ApplicationResponse(StatusCodes.Created.intValue, response, "URL successfully shortened")
            }
      }
    }
  }

  def getOriginalUrl(shortCode: String): Future[ApplicationResponse] = {
    val urlsTable = TableQuery[URLMappingTable]

    val query = urlsTable.filter(_.shortCode === shortCode)
    db.run(query.result.headOption).flatMap {
      case Some(entry) =>
        // check expiry
        val now = ZonedDateTime.now(ZoneOffset.UTC)
        entry.expiry match {
          case Some(expiryDate) if now.isAfter(expiryDate) =>
            Future.successful(ApplicationResponse(StatusCodes.Gone.intValue, null, s"URL Expired"))
          case _ =>
            // update click count
            val updatedClickCount = entry.clickCount + 1
            val updatedQuery = urlsTable.filter(_.id === entry.id.get).map(_.clickCount).update(updatedClickCount)

            db.run(updatedQuery).map {_ =>
              ApplicationResponse(StatusCodes.Found.intValue, entry.originalUrl, s"Redirecting")
            }
        }
      case None =>
        Future.successful(ApplicationResponse(StatusCodes.NotFound.intValue, null, s"Short URL not found"))
    }
  }

  private val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
  private val random = new SecureRandom()

  private def generateShortCode(length: Int = 6): String = {
    require(length > 0, "Short code length must be positive")
    val sb = new StringBuilder(length)
    for (_ <- 1 to length) {
      val idx = random.nextInt(chars.length)
      sb.append(chars.charAt(idx))
    }
    sb.toString()
  }

  private def isValidUrl(url: String): Boolean = {
    try {
      val parsedUrl: String = new URI(url).toString
      parsedUrl.startsWith("http://") || parsedUrl.startsWith("https://")
    } catch {
      case _: Exception => false
    }
  }
}
/// ssxdcfvgbhnjmhh