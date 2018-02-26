import com.google.inject.Guice
import main.core.{AppConfig, Module}
import main.model.SearchResult
import main.service.GoogleService
import org.scalatest.{Matchers, WordSpec}
import org.scalatest.concurrent._
import org.scalatest.time.{Millis, Span}

import scala.concurrent.Future
import scala.io.Source

/**
  * Created by emma on 26/02/2018.
  */
class GoogleServiceSpec extends WordSpec with Matchers with ScalaFutures {

  val injector = Guice.createInjector(new Module())
  val appConfig = injector.getInstance(classOf[AppConfig])

  lazy val googleService = new GoogleService(appConfig) {
    lazy val searchResultPage = Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("helloWorldGoogleResult.html")).getLines.mkString
    override def search(query: String): Future[String] = {
      Future.successful(searchResultPage)
    }
  }

  "encode" should {
    "substitute the URL-encoded spaces in a string for + characters and strip out any additional non-alpha-numeric characters" in {
      googleService.encode("hello%20world") shouldBe "hello+world"
      googleService.encode("hello@@@@@****w0rld") shouldBe "hellow0rld"
    }
  }

  "stripOutNthResult" should {
    "obtain the appropriate nth result when supplied with valid Google search result page HTML" in {
      whenReady(googleService.stripOutNthResult("dummyQuery", 2)) {
        result: SearchResult =>
          result.uri shouldBe "https://en.wikipedia.org/wiki/%22Hello,_World!%22_program"
          result.title shouldBe "\"Hello, World!\" program - Wikipedia"
          result.description should include("A \"Hello, World!\" program is a computer program that outputs or displays \"Hello, World!\" to a user.")
          result.requestUrl shouldBe googleService.url("dummyQuery")
      }

      whenReady(googleService.stripOutNthResult("dummyQuery", 5)) {
        result: SearchResult =>
          result.uri shouldBe "https://helloworld.raspberrypi.org/"
          result.title shouldBe "Hello World - Raspberry Pi"
          result.description should include("Join our panel of experts in issue 4 of Hello World magazine")
          result.requestUrl shouldBe googleService.url("dummyQuery")
      }
    }
  }

}
