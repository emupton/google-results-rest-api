import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.google.inject.Guice
import core.{AppConfig, Module, Routes}
import exception.GoogleFormatException
import model.SearchResult
import org.scalatest.Matchers
import org.scalatest.WordSpec
import service.GoogleService

import scala.concurrent.Future

class RoutesSpec extends WordSpec with Matchers with ScalatestRouteTest {

  object TestData {
    val helloWorldResults = "<div class=\"srg\"><div class=\"g\"><!--m--><link href=\"https://www.helloworldlive.com/\" rel=\"prerender\">" +
      "<div data-hveid=\"38\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQFQgmKAAwAA\"><div class=\"rc\"><h3 class=\"r\">" +
      "<a href=\"https://www.helloworldlive.com/\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=" +
      "https://www.helloworldlive.com/&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQFggnMAA\">HelloWorld 2018. This is your World.</a>" +
      "</h3><div class=\"s\"><div><div class=\"f kv _SWb\" style=\"white-space:nowrap\"><cite class=\"_Rm\">https://www.helloworldlive.com/</cite>" +
      "<div class=\"action-menu ab_ctl\"><a class=\"_Fmb ab_button\" href=\"#\" id=\"am-b0\" aria-label=\"Result details\" aria-expanded=\"false\" aria-haspopup=" +
      "\"true\" role=\"button\" jsaction=\"m.tdd;keydown:m.hbke;keypress:m.mskpe\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ7B0IKDAA\"><span class=\"mn-dwn-arw\">" +
      "</span></a><div class=\"action-menu-panel ab_dropdown\" role=\"menu\" tabindex=\"-1\" jsaction=\"keydown:m.hdke;mouseover:m.hdhne;mouseout:m.hdhue\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQqR8IKTAA\">" +
      "<ol><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a class=\"fl\" href=\"https://webcache.googleusercontent.com/search?q=cache:qURzCIYVqFsJ:https://www.helloworldlive.com/+&amp;cd=1&amp;hl=en&amp;ct=clnk&amp;gl=uk\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://webcache.googleusercontent.com/search%3Fq%3Dcache:qURzCIYVqFsJ:https://www.helloworldlive.com/%2B%26cd%3D1%26hl%3Den%26ct%3Dclnk%26gl%3Duk&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQIAgqMAA\">" +
      "Cached</a></li></ol></div></div></div><span class=\"st\">Home of the incredible <em>HelloWorld</em> 2018, bringing the world&#39;s biggest vloggers and you together for a unique experience. Sign up for news and information about <em>HelloWorld</em> 2018 and take a look at our official merch store.</span></div></div><div jsl=\"$t t--ddbPTeIsNI$t-nEjf2CObCkc;$x 0;\" class=\"r-ipUdMnztOYiU\">" +
      "<div class=\"kN9UDnqLeMg__explore-main ipUdMnztOYiU-7_jVsFT_9Io\" id=\"eobm_0\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ2Z0BCCswAA\">" +
      "<div id=\"eobd_0\" class=\"ipUdMnztOYiU-uhagcrfPmuU\" style=\"display:none\"><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCCwoADAA\">hello world vip tickets</div>" +
      "<div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCC0oATAA\">hello world twitter</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCC4oAjAA\">eprize</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCC8oAzAA\">hello world instagram</div>" +
      "<div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCDAoBDAA\">canyon studios hello world</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCDEoBTAA\">genting arena confiscation list</div></div><span class=\"kN9UDnqLeMg__dismiss-button\" id=\"eobs_0\" aria-label=\"Dismiss suggested follow ups\" role=\"button\" tabindex=\"0\" jsaction=\"r.pz0qjfJrMDo\" data-rtid=\"ipUdMnztOYiU\" jsl=\"$x 2;\"></span><div><div class=\"kN9UDnqLeMg__explore-container ipUdMnztOYiU-eEjGhTK0s34\" id=\"eobc_0\"><h4 class=\"kN9UDnqLeMg__carousel-caption ipUdMnztOYiU-ZgH0LU9o8RU\" id=\"eobp_0\">People also search for</h4><div class=\"kN9UDnqLeMg__explore-result ipUdMnztOYiU-ICxnu-SGsqE\" id=\"eobr_0\"></div></div></div></div></div></div></div><!--n--></div><div class=\"g\"><!--m--><div data-hveid=\"50\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQFQgyKAEwAQ\"><div class=\"rc\"><h3 class=\"r\"><a href=\"https://en.wikipedia.org/wiki/%22Hello,_World!%22_program\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://en.wikipedia.org/wiki/%2522Hello,_World!%2522_program&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQFggzMAE\">&quot;Hello, World!&quot; program - Wikipedia</a></h3><div class=\"s\"><div><div class=\"f kv _SWb\" style=\"white-space:nowrap\">" +
      "<cite class=\"_Rm\">https://en.wikipedia.org/wiki/%22Hello,_World!%22_program</cite><div class=\"action-menu ab_ctl\"><a class=\"_Fmb ab_button\" href=\"#\" id=\"am-b1\" aria-label=\"Result details\" aria-expanded=\"false\" aria-haspopup=\"true\" role=\"button\" jsaction=\"m.tdd;keydown:m.hbke;keypress:m.mskpe\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ7B0INDAB\"><span class=\"mn-dwn-arw\"></span></a><div class=\"action-menu-panel ab_dropdown\" role=\"menu\" tabindex=\"-1\" jsaction=\"keydown:m.hdke;mouseover:m.hdhne;mouseout:m.hdhue\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQqR8INTAB\"><ol><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a class=\"fl\" href=\"https://webcache.googleusercontent.com/search?q=cache:UInCiDfJyeUJ:https://en.wikipedia.org/wiki/%2522Hello,_World!%2522_program+&amp;cd=2&amp;hl=en&amp;ct=clnk&amp;gl=uk\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://webcache.googleusercontent.com/search%3Fq%3Dcache:UInCiDfJyeUJ:https://en.wikipedia.org/wiki/%252522Hello,_World!%252522_program%2B%26cd%3D2%26hl%3Den%26ct%3Dclnk%26gl%3Duk&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQIAg2MAE\">Cached</a></li><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a class=\"fl\" href=\"/search?q=related:https://en.wikipedia.org/wiki/%2522Hello,_World!%2522_program+hello+world&amp;tbo=1&amp;sa=X&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQHwg3MAE\">Similar</a></li></ol></div></div></div><span class=\"st\">A &quot;<em>Hello</em>, <em>World</em>!&quot; program is a computer program that outputs or displays &quot;<em>Hello</em>, <em>World</em>!&quot; to a user. Being a very simple program in most programming languages, it is often used to illustrate the basic syntax of a programming language for a working program. It is often the very first program people write when they are new to a&nbsp;...</span>" +
      "<div class=\"osl\">\u200E<a class=\"fl\" href=\"https://en.wikipedia.org/wiki/%22Hello,_World!%22_program#Purpose\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://en.wikipedia.org/wiki/%2522Hello,_World!%2522_program%23Purpose&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ0gIIOSgAMAE\">Purpose</a> ·&nbsp;\u200E<a class=\"fl\" href=\"https://en.wikipedia.org/wiki/%22Hello,_World!%22_program#History\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://en.wikipedia.org/wiki/%2522Hello,_World!%2522_program%23History&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ0gIIOigBMAE\">History</a> ·&nbsp;\u200E<a class=\"fl\" href=\"https://en.wikipedia.org/wiki/%22Hello,_World!%22_program#Variations\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://en.wikipedia.org/wiki/%2522Hello,_World!%2522_program%23Variations&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ0gIIOygCMAE\">Variations</a></div></div></div><div jsl=\"$t t--ddbPTeIsNI$t-nEjf2CObCkc;$x 0;\" class=\"r-iYVdt9uAKE7k\"><div class=\"kN9UDnqLeMg__explore-main iYVdt9uAKE7k-7_jVsFT_9Io\" id=\"eobm_1\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ2Z0BCDwwAQ\"><div id=\"eobd_1\" class=\"iYVdt9uAKE7k-uhagcrfPmuU\" style=\"display:none\"><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCD0oADAB\">hello world program c++</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCD4oATAB\">hello world book</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCD8oAjAB\">origin of hello word</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCEAoAzAB\">eprize</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCEEoBDAB\">hello world in different languages</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCEIoBTAB\">hello world html</div></div><span class=\"kN9UDnqLeMg__dismiss-button\" id=\"eobs_1\" aria-label=\"Dismiss suggested follow ups\" role=\"button\" tabindex=\"0\" " +
      "jsaction=\"r.pz0qjfJrMDo\" data-rtid=\"iYVdt9uAKE7k\" jsl=\"$x 2;\">" +
      "</span><div><div class=\"kN9UDnqLeMg__explore-container iYVdt9uAKE7k-eEjGhTK0s34\" id=\"eobc_1\"><h4 class=\"kN9UDnqLeMg__carousel-caption iYVdt9uAKE7k-ZgH0LU9o8RU\" id=\"eobp_1\">People also search for</h4><div class=\"kN9UDnqLeMg__explore-result iYVdt9uAKE7k-ICxnu-SGsqE\" id=\"eobr_1\"></div></div></div></div></div></div></div><!--n--></div><div class=\"g\"><!--m--><div data-hveid=\"67\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQFQhDKAIwAg\"><div class=\"rc\"><h3 class=\"r\"><a href=\"https://www.newstatesman.com/science-tech/social-media/2017/10/i-feel-i-ve-been-robbed-inside-helloworld-fyre-festival-youtubers\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://www.newstatesman.com/science-tech/social-media/2017/10/i-feel-i-ve-been-robbed-inside-helloworld-fyre-festival-youtubers&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQFghEMAI\">“I feel like I&#39;ve been robbed”: inside HelloWorld, the YouTuber event ...</a></h3><div class=\"s\"><div><div class=\"f kv _SWb\" style=\"white-space:nowrap\"><cite class=\"_Rm\">https://www.newstatesman.com/.../i-feel-i-ve-been-robbed-inside-helloworld-fyre-festi...</cite><div class=\"action-menu ab_ctl\"><a class=\"_Fmb ab_button\" href=\"#\" id=\"am-b2\" aria-label=\"Result details\" aria-expanded=\"false\" aria-haspopup=\"true\" role=\"button\" jsaction=\"m.tdd;keydown:m.hbke;keypress:m.mskpe\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ7B0IRTAC\"><span class=\"mn-dwn-arw\"></span></a><div class=\"action-menu-panel ab_dropdown\" role=\"menu\" tabindex=\"-1\" jsaction=\"keydown:m.hdke;mouseover:m.hdhne;mouseout:m.hdhue\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQqR8IRjAC\"><ol><li class=\"action-menu-item ab_dropdownitem\" role=\"menuitem\"><a class=\"fl\" href=\"https://webcache.googleusercontent.com/search?q=cache:IcYtOC2-EU4J:https://www.newstatesman.com/science-tech/social-media/2017/10/i-feel-i-ve-been-robbed-inside-helloworld-fyre-festival-youtubers+&amp;cd=3&amp;hl=en&amp;ct=clnk&amp;gl=uk\" ping=\"/url?sa=t&amp;source=web&amp;rct=j&amp;url=https://webcache.googleusercontent.com/search%3Fq%3Dcache:IcYtOC2-EU4J:https://www.newstatesman.com/science-tech/social-media/2017/10/i-feel-i-ve-been-robbed-inside-helloworld-fyre-festival-youtubers%2B%26cd%3D3%26hl%3Den%26ct%3Dclnk%26gl%3Duk&amp;ved=0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQIAhHMAI\">Cached</a></li></ol></div></div></div><span class=\"st\"><span class=\"f\">30 Oct 2017 - </span>Initially, Darren Day told his 12-year-old daughter Macy that the family couldn&#39;t afford to go to <em>HelloWorld</em>. Billed as an “immersive live experience”, the two-day event was going to be a place for young fans to see their YouTube heroes in the flesh, enjoy carnival rides, listen to live music, and play games.</span></div></div><div jsl=\"$t t--ddbPTeIsNI$t-nEjf2CObCkc;$x 0;\" class=\"r-iWECFME28Xr8\"><div class=\"kN9UDnqLeMg__explore-main iWECFME28Xr8-7_jVsFT_9Io\" id=\"eobm_2\" data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ2Z0BCEkwAg\"><div id=\"eobd_2\" class=\"iWECFME28Xr8-uhagcrfPmuU\" style=\"display:none\"><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCEooADAC\">hello world song</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCEsoATAC\">hello world book</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCEwoAjAC\">hello world magazine</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCE0oAzAC\">hello world album</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQsKwBCE4oBDAC\">hello world baby shower</div><div data-ved=\"0ahUKEwizrtnclLnZAhVsIsAKHWGMD6YQ"
  }

  val injector = Guice.createInjector(new Module())
  val appConfig = injector.getInstance(classOf[AppConfig])

  "/query/:query" should {

    "return a 200 when successful" in {
      val googleService = new GoogleService(appConfig) {
        override def search(query: String): Future[String] = {
          Future.successful(TestData.helloWorldResults)
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
