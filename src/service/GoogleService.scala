package service

import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, StatusCodes}
import app.AppConfig
import com.google.inject.{Inject, Singleton}
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
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

  implicit val system: ActorSystem = ActorSystem("actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  def search(query: String) = {
    Http().singleRequest(HttpRequest(HttpMethods.GET, url + encode(query))).flatMap {
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
        val doc: Document = Jsoup.parse(htmlBody)
        doc.select("div.g :nth-of-type(2)").select("cite").first().text
    }
  }
}
