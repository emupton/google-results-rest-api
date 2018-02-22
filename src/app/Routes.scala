package app

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._enhanceRouteWithConcatenation
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.MethodDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.model.StatusCodes.{OK, ServiceUnavailable}
import com.google.inject.Inject
import model.DownstreamError
import service.GoogleService

import scala.util.{Failure, Success}
import util.ImplicitJsonConversions._

/**
  * Created by emma on 19/02/2018.
  */
class Routes @Inject()(googleService: GoogleService){

  lazy val routes: Route = {
    pathPrefix("query") {
      path(Remaining) { query => onComplete(googleService.stripOutNthResult(query, 2)) {
        case Success(searchResult) => complete(OK, searchResult)
        case Failure(exception) => complete(ServiceUnavailable, DownstreamError(s"Connectivity issues to Google: received a ${exception.getMessage} response"))
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
