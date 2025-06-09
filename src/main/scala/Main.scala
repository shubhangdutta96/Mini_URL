import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import routes.urlRoutes
import actors.urlActor

import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  def main(args: Array[String]): Unit = {
    // Create the root actor (urlActor)
    val rootBehavior = urlActor()
    implicit val system: ActorSystem[urlActor.Query] = ActorSystem(rootBehavior, "urlShortenerSystem")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    // Create routes and start the HTTP server
    val route: Route = new urlRoutes(system).routes

    val host = "0.0.0.0"
    val port = 8080

    val bindingFuture = Http().newServerAt(host, port).bind(route)

    bindingFuture.onComplete {
      case Success(_) =>
        println(s"Server online at http://$host:$port/")
      case Failure(ex) =>
        println(s"Failed to bind HTTP server: ${ex.getMessage}")
        system.terminate()
    }

    // Keep the server running until terminated
    Await.result(system.whenTerminated, Duration.Inf)
  }
}