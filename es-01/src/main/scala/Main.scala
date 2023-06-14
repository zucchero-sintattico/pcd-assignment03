import assignment.actors.ReportBuilder.Command.AddStatistic
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import assignment.actors.{FolderScanner, ReportBuilder}

import java.nio.file.{Files, Path}
import scala.concurrent.Future

object Algorithm:
  enum Command:
    case Start(path: Path)
    case Stop

  def apply(): Behavior[Command] = idle()

  def idle(): Behavior[Command] =
    Behaviors.setup { context =>
      import Command._
      println("Algorithm started in idle state")
      Behaviors.receiveMessage[Command] {
        case Start(path) => started(path)
        case Stop => Behaviors.same
      }
    }

  def started(path: Path): Behavior[Command] =
    Behaviors.setup { context =>
      import Command._
      println("Algorithm switched to started state")
      val reportBuilder = context.spawn(ReportBuilder(), "reportBuilder")
      val folderScanner = context.spawn(FolderScanner(), "folderScanner")
      context.watch(folderScanner)
      folderScanner ! FolderScanner.Command.Scan(path, reportBuilder)
      Behaviors.receiveMessage[Command] {
        case Start(path) => Behaviors.same
        case Stop =>
          println("Stopping Algorithm")
          context.stop(folderScanner)
          reportBuilder ! ReportBuilder.Command.Complete
          context.stop(reportBuilder)
          Behaviors.stopped
      }.receiveSignal {
        case (_, Terminated(_)) =>
          println("FolderScanner terminated")
          reportBuilder ! ReportBuilder.Command.Complete
          Behaviors.stopped
      }

    }
@main def main(): Unit =
  val system = ActorSystem(Algorithm(), "example")
  system ! Algorithm.Command.Start(Path.of("../"))
  system ! Algorithm.Command.Stop



