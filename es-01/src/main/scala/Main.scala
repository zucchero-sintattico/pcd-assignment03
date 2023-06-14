
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import assignment.actors.Algorithm

import java.nio.file.Path

@main def main(): Unit =
  val system = ActorSystem(Algorithm(), "example")
  system ! Algorithm.Command.Start(Path.of("../"))
  //system ! Algorithm.Command.Stop



