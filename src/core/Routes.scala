package core

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.http.scaladsl.server.RouteConcatenation._enhanceRouteWithConcatenation
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.model.StatusCodes.{OK, ServiceUnavailable}
import akka.http.scaladsl.server.Directives._
import com.google.inject.{Inject, Singleton}
import model.DownstreamError
import service.GoogleService

import scala.util.{Failure, Success}
import util.ImplicitJsonConversions._

/**
  * Created by emma on 19/02/2018.
  */
@Singleton
class Routes @Inject()(googleService: GoogleService){

  val exceptionHandler = ExceptionHandler {
    case e: Exception =>
        complete(ServiceUnavailable, DownstreamError(e.getMessage))
  }

  lazy val routes: Route = {
    handleExceptions(exceptionHandler) {
      pathPrefix("query") {
        path(Remaining) { query =>
          get {
            onComplete(googleService.stripOutNthResult(query, 2)) {
              case Success(searchResult) => complete(OK, searchResult)
            }
          }
        }
      } ~
        path("healthcheck") {
          get {
            complete(OK)
          }
        }
    }
  }
}
