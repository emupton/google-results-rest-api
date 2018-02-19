package app

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.RouteConcatenation._enhanceRouteWithConcatenation
import akka.http.scaladsl.server.directives.PathDirectives._
import akka.http.scaladsl.server.directives.MethodDirectives._
import akka.http.scaladsl.server.directives.RouteDirectives._
import akka.http.scaladsl.server.directives.FutureDirectives.onComplete
import akka.http.scaladsl.model.StatusCodes._
import com.google.inject.Inject
import service.GoogleService

/**
  * Created by emma on 19/02/2018.
  */
class Routes @Inject()(googleService: GoogleService){

  lazy val routes: Route = {
    pathPrefix("query") {
      path(Remaining) { query =>
      onComplete(googleService.stripOutSecondSearchResult(query)) {
        case x => complete(OK, x)
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
