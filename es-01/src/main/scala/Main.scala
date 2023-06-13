import ReportAggregator.Command.AddStatistic
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors

import java.nio.file.{Files, Path}
import scala.concurrent.Future

case class Statistic(path: Path, size: Long)

object ReportAggregator:
  enum Command:
    case AddStatistic(statistic: Statistic)
    case Complete

  def apply(): Behavior[Command] =
    import Command._
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case AddStatistic(statistic) =>
          // TODO: add statistic
          Behaviors.same
        case Complete =>
          Behaviors.stopped
      }
    }

object FileScanner:
  enum Command:
    case Scan(path: Path, replyTo: ActorRef[ReportAggregator.Command])

  def apply(): Behavior[Command] =
    import Command._
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, replyTo) =>
          // TODO: scan file
          Behaviors.same
      }
    }

object FolderScanner:
  enum Command:
    case Scan(path: Path, replyTo: ActorRef[FileScanner.Command])

  def apply(): Behavior[Command] =
    import Command._
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, replyTo) =>
          // TODO: scan folder
          Behaviors.same
      }
    }

object App:
  enum Command:
    case Start(path: Path)

  def apply(): Behavior[Nothing] =
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case _ => Behaviors.same
      }
    }

@main def main(): Unit =
  val system = ActorSystem(App(), "example")



