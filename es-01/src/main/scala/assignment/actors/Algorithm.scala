package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import assignment.Domain.ReportConfiguration
import assignment.actors.Algorithm.Command
import assignment.actors.Algorithm.Command.{Start, Stop}

import java.nio.file.Path
import assignment.mvc.view.View

object Algorithm:
  enum Command:
    case Start(path: Path, reportConfiguration: ReportConfiguration, view: View)
    case Stop

  def apply(): Behavior[Command] = idle()

  private def idle(): Behavior[Command] =
    Behaviors.receiveMessage[Command] {
        case Start(path, reportConfiguration, view) => started(path, reportConfiguration, view)
        case Stop => Behaviors.same
    }


  private def started(path: Path, reportConfiguration: ReportConfiguration, view: View): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      println("Algorithm switched to started state")
      val notificationListener = context.spawn(ViewNotificationListeners(view), "notificationListeners")
      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListener), "reportBuilder")

      val folderScanner = context.spawn(FolderScanner(), "folderScanner")
      context.watch(folderScanner)
      folderScanner ! FolderScanner.Command.Scan(path, reportBuilder)

      Behaviors.receiveMessage[Command] {
        case Start(_, _, _) => Behaviors.same
        case Stop =>
          println("Stopping Algorithm")
          context.unwatch(folderScanner)
          context.stop(folderScanner)
          reportBuilder ! ReportBuilder.Command.Complete
          context.stop(notificationListener)
          context.stop(reportBuilder)
          idle()
      }.receiveSignal {
        case (_, Terminated(_)) =>
          println("FolderScanner terminated")
          reportBuilder ! ReportBuilder.Command.Complete
          context.watch(reportBuilder)
          Behaviors.receiveSignal {
            case (_, Terminated(_)) =>
              println("ReportBuilder terminated")
              context.stop(notificationListener)
              context.stop(reportBuilder)
              context.stop(folderScanner)
              idle()
          }
      }

    }