package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import app.AppConfig
import com.google.inject.{Inject, Singleton}
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.model.headers.{RawHeader, `User-Agent`}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import model.SearchResult
import org.jsoup.Jsoup

import scala.concurrent.ExecutionContext.Implicits.global
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

/**
  * Created by emma on 19/02/2018.
  */
@Singleton
class GoogleService @Inject()(appConfig: AppConfig){
  val url = appConfig.googleUrl + "/search?q="
  //user agent required due to google using different style sheets depending on browser...
  val USER_AGENT_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.89 Safari/537.36"

  implicit val system: ActorSystem = ActorSystem("actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def search(query: String) = {
    Http().singleRequest(HttpRequest(HttpMethods.GET,
                        url + encode(query),
                        headers = List(`User-Agent`(USER_AGENT_VALUE)))).flatMap {
      resp =>
        resp.status match {
          case StatusCodes.OK => Unmarshal(resp.entity).to[String]
        }
    }
  }

  def encode(query: String): String = {
    URLEncoder.encode(query, "UTF-8")
  }

  def stripOutSecondSearchResult(query: String) = {
    search(query).map {
      htmlBody: String =>
        val doc = Jsoup.parse(htmlBody)
        val results = doc.select("div.srg").first()
        val h3 = results.childNodes().get(1).childNodes().get(1).childNodes().get(0).childNodes.get(0).toString
        println(results)
//        System.out.println(results)
//        val secondResult = results.select("div.g :nth-of-type(2)")
//        val h3 = secondResult.select("h3").first().html()
        val h3url = Jsoup.parse(h3).select("a").first().attr("href")
        val h3text = Jsoup.parse(h3).text()
        SearchResult(h3url, h3text)
        doc.html
    }
  }
}
