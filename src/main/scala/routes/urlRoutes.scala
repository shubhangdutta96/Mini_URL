package routes

import actors.urlActor._
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import models.JsonFormats
import net.liftweb.json.{NoTypeHints, Serialization}

import scala.concurrent.Future

class urlRoutes(urlActor: ActorRef[Query])(implicit val system: ActorSystem[_]) extends JsonFormats with SprayJsonSupport {
  implicit val formats = Serialization.formats(NoTypeHints)

  private def createShortUrl(request: ShortenUrlRequest): Future[ApplicationResponse] = urlActor.ask(createShortUrlRequest(request, _))
  def getOriginalUrl(shortCode: String): Future[ApplicationResponse] = urlActor.ask(getOriginalUrlRequest(shortCode, _))

  val routes: Route = {
    concat (
      path("healthCheck") {
        get {
          println(s"Request recieved for Health Check. Service is Running")
          complete(s"Service is Running")
        }
      },
      path("short") {
        post {
          entity(as[ShortenUrlRequest]) { request =>
            rejectEmptyResponse {
              onSuccess(createShortUrl(request)) { response =>
                complete(JsonResponse(Serialization.write(response)))
              }
            }
          }
        }
      },
      path(Segment) { shortCode =>
        get {
          onSuccess(getOriginalUrl(shortCode)) {
            case a@ApplicationResponse(code, url, _) if code == StatusCodes.Found.intValue =>
//              redirect(Uri(url.toString), StatusCodes.Found)
              complete(HttpResponse(
                status = StatusCode.int2StatusCode(code),
                entity = HttpEntity(ContentTypes.`application/json`, Serialization.write(a))
              ))
            case ApplicationResponse(code, _, message) =>
              complete(code -> message)
          }
        }
      }
    )
  }
}
