package util

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

/**
  * Created by emma on 21/02/2018.
  */
trait AkkaSystemUtils {
  implicit val system: ActorSystem = ActorSystem("actor-system")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
}
