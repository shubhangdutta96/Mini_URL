package actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.http.scaladsl.model.StatusCodes

import scala.util.{Failure, Success}
import service.urlHandler.{shortenUrl, getOriginalUrl}
import akka.util.Timeout

import java.time.ZonedDateTime
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object urlActor {
  implicit val timeout: Timeout = Timeout(5.seconds)

  case class ShortenUrlRequest(originalUrl: String,
                               expiryDays: Option[Int])
  case class ShortenUrlResponse(
                         id: Int,
                         originalUrl: String,
                         shortCode: String,
                         createdAt: String,
                         expiry: String,
                         clickCount: Int = 0
                       )

  final case class ApplicationResponse(
                                        statusCode: Int,
                                        data: Object,
                                        errorMessage: String)

  case class URLMapping(
                         id: Option[Int],
                         originalUrl: String,
                         shortCode: String,
                         createdAt: ZonedDateTime,
                         expiry: Option[ZonedDateTime],
                         clickCount: Int
                       )


  sealed trait Query
  implicit val system: ActorSystem[Query] = ActorSystem(Behaviors.empty, "query")

  final case class createShortUrlRequest(request: ShortenUrlRequest, replyTo: ActorRef[ApplicationResponse]) extends Query
  final case class getOriginalUrlRequest(shortCode: String, replyTo: ActorRef[ApplicationResponse]) extends Query

  def apply(): Behavior[Query] = {
    Behaviors.receiveMessage {
      case createShortUrlRequest(request, replyTo) =>
        shortenUrl(request).onComplete {
          case Success(applicationResponse) =>
            replyTo ! applicationResponse
          case Failure(_) =>
            val errorResponse = ApplicationResponse(StatusCodes.InternalServerError.intValue, null, s"Failed to create short URL")
            replyTo ! errorResponse
        }
        Behaviors.same

      case getOriginalUrlRequest(shortCode, replyTo) =>
        getOriginalUrl(shortCode).onComplete {
          case Success(applicationResponse) =>
            replyTo ! applicationResponse
          case Failure(_) =>
            val errorResponse = ApplicationResponse(StatusCodes.InternalServerError.intValue, null, s"Failed to create short URL")
            replyTo ! errorResponse
        }
        Behaviors.same
    }
  }
}
