import assignment.actors.ReportBuilder.Command.AddStatistic
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

import java.nio.file.{Files, Path}
import scala.concurrent.Future

object System:
  enum Command:
    case Start(path: Path)

  def apply(): Behavior[Nothing] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case _ => Behaviors.same
      }
    }

@main def main(): Unit =
  val system = ActorSystem(System(), "example")



