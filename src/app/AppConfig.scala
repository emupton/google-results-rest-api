package app

import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by emma on 19/02/2018.
  */
class AppConfig {

  lazy val config: Config = ConfigFactory.load()

  lazy val httpHost: String = config.getString("http.host")
  lazy val httpPort: Int = config.getInt("http.port")

  lazy val googleUrl: String = config.getString("google.url")

  lazy val appName: String = config.getString("app.name")
}
