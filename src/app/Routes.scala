package app

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._enhanceRouteWithConcatenation
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.MethodDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.directives.FutureDirectives.onSuccess
import akka.http.scaladsl.model.StatusCodes._
import com.google.inject.Inject
import service.GoogleService
import util.ImplicitJsonConversions._

import scala.concurrent.ExecutionContext

/**
  * Created by emma on 19/02/2018.
  */
class Routes @Inject()(googleService: GoogleService){

  lazy val routes: Route = {
    pathPrefix("query") {
      path(Remaining) { query => onSuccess(googleService.stripOutSecondSearchResult(query)) { result =>
        complete(OK, result)
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
