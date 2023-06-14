import akka.actor.typed.ActorSystem
import assignment.actors.Algorithm

import java.nio.file.{Files, Path}

@main def main(): Unit =
  val system = ActorSystem(Algorithm(), "example")
  system ! Algorithm.Command.Start(Path.of("../"))
  system ! Algorithm.Command.Stop



