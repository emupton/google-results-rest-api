package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import core.AppConfig
import com.google.inject.{Inject, Singleton}
import java.net.URLEncoder

import akka.http.scaladsl.model.headers.`User-Agent`
import akka.http.scaladsl.unmarshalling.Unmarshal
import model.SearchResult
import org.jsoup.Jsoup
import util.AkkaSystemUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import exception._

/**
  * Created by emma on 19/02/2018.
  */
@Singleton
class GoogleService @Inject()(appConfig: AppConfig) extends AkkaSystemUtils {
  /*when deployed search results are inconsistent due to Heroku having different geographic locations across their availability regions.
  * There's limited configuration options available in terms of ensuring where in these regions a request will be served from (e.g you can't
  * make it country-specific), and thus this effects the kinds of search results coming back.
  * So to make these consistent I've added an additional search parameter that's supposed to specify the location
  * since apparently using just the .co.uk Google domain isn't a reliable method to ensure you aren't
  * redirected to the calling region's Google search engine. Using the near parameter does still
  * effect the results though*/
  def url(query: String) = s"${appConfig.googleUrl}/search?q=${encode(query)}&uule=w+CAIQICIOVW5pdGVkIEtpbmdkb20&safe=high&gws_rd=cr&dcr=0&ei=u9mSWrvSCIPKwQLjr6O4Dg"
  //user agent required due to google using different style sheets depending on browser...
  val USER_AGENT_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36"
  val RESULTS_CSS_SELECTOR = "div.srg"

  def search(query: String): Future[String] = {

    println(url(query))

    lazy val request = Http().singleRequest(HttpRequest(HttpMethods.GET,
      url(query),
      headers = List(`User-Agent`(USER_AGENT_VALUE))))

    def handleResp(httpResponse: HttpResponse, responseBody: String): String = {
      httpResponse.status match {
        case StatusCodes.OK | StatusCodes.PermanentRedirect =>
          println(httpResponse.status.toString())
          responseBody
        case code => throw new Exception(s"Non-OK response from Google: code ${code.toString()}") //would normally avoid throwing an exception and instead use EitherT pattern but seems overkill here
      }
    }

    for {
      response <- request
      respMarshal <- Unmarshal(response.entity).to[String] //loading the response body into a String of HTML
    } yield handleResp(response, respMarshal)
  }

  def encode(query: String): String = {
    URLEncoder.encode(query, "UTF-8")
  }

  def stripOutNthResult(query: String, n: Int): Future[SearchResult] = {

    def extractSearchResultFromHTMLBody(googleResults: String): SearchResult = {
      val doc = Jsoup.parse(googleResults)

      val searchResultsDiv = doc.select(s"div.srg")

      if(searchResultsDiv.isEmpty) {
        throw new GoogleFormatException("The formatting of the Google search results has changed")
      }

      val nthResult = doc.select(s"div.srg .g:nth-child(${n}) h3").first()
      val h3Text = nthResult.text()
      val h3Url = nthResult.select("a").first().attr("href")
      SearchResult(h3Url, h3Text, googleResults)
    }

    search(query).map { googleSearchResults => extractSearchResultFromHTMLBody(googleSearchResults) }
  }
}
