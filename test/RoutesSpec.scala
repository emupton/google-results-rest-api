import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.google.inject.Guice
import main.core.{AppConfig, Module, Routes}
import org.scalatest.Matchers
import org.scalatest.WordSpec
import main.service.GoogleService

import scala.concurrent.Future
import scala.io.Source

class RoutesSpec extends WordSpec with Matchers with ScalatestRouteTest {

  val injector = Guice.createInjector(new Module())
  val appConfig = injector.getInstance(classOf[AppConfig])

  "/query/:query" should {

    "return a 200 when successful" in {
      lazy val googleService = new GoogleService(appConfig) {
        lazy val searchResultPage = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("helloWorldGoogleResult.html")).getLines.mkString
        override def search(query: String): Future[String] = {
          Future.successful(searchResultPage)
        }
      }
      val serviceRoutes = new Routes(googleService)
      HttpRequest(HttpMethods.GET, uri = "/query/helloworld") ~> serviceRoutes.routes ~> check {
        response.status shouldBe StatusCodes.OK
        response.entity.toString() should include("wikipedia")
      }
    }

    "return a 503 when Google returns a non-200 response" in {
      val googleService = new GoogleService(appConfig) {
        override def search(query: String): Future[String] = {
          throw new Exception(StatusCodes.BadRequest.toString)
        }
      }
      val serviceRoutes = new Routes(googleService)

        HttpRequest(HttpMethods.GET, uri = "/query/helloworld") ~> serviceRoutes.routes ~> check {
          response.status shouldBe StatusCodes.ServiceUnavailable
          response.entity.toString() should include("400")
        }
      }

    "return a 503 when Google returns a 200 response with different formatting" in {
      val googleService = new GoogleService(appConfig) {
        override def search(query: String): Future[String] = {
          Future.successful("1234")
        }
      }
      val serviceRoutes = new Routes(googleService)

      HttpRequest(HttpMethods.GET, uri = "/query/helloworld") ~> serviceRoutes.routes ~> check {
        response.status shouldBe StatusCodes.ServiceUnavailable
        response.entity.toString() should include("The formatting of the Google search results has changed")
      }
    }
    }
}
