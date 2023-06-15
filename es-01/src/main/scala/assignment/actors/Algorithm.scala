package assignment.actors

import Algorithm.Command
import Algorithm.Command.{Start, Stop}
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import assignment.Statistic

import java.nio.file.Path

object Algorithm:
  enum Command:
    case Start(path: Path)
    case Stop

  def apply(): Behavior[Command] = idle()

  private def idle(): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
        case Start(path) => started(path)
        case Stop => Behaviors.same
    }


  private def started(path: Path): Behavior[Command] =
    import Command._
    Behaviors.setup { context =>
      println("Algorithm switched to started state")

      val reportConfiguration = ReportConfiguration(10, 20, 30)
      val notificationListener = context.spawn(NotificationListeners(), "notificationListeners")
      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListener), "reportBuilder")

      val folderScanner = context.spawn(FolderScanner(), "folderScanner")
      context.watch(folderScanner)
      folderScanner ! FolderScanner.Command.Scan(path, reportBuilder)

      Behaviors.receiveMessage[Command] {
        case Start(path) => Behaviors.same
        case Stop =>
          println("Stopping Algorithm")
          context.unwatch(folderScanner)
          context.stop(folderScanner)
          reportBuilder ! ReportBuilder.Command.Complete
          idle()
      }.receiveSignal {
        case (_, Terminated(_)) =>
          println("FolderScanner terminated")
          reportBuilder ! ReportBuilder.Command.Complete
          context.watch(reportBuilder)
          Behaviors.receiveSignal {
            case (_, Terminated(_)) =>
              println("ReportBuilder terminated")
              idle()
          }
      }

    }