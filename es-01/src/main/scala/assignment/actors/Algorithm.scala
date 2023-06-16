package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy, Terminated}
import assignment.Domain.{ReportConfiguration, Statistic}
import assignment.actors.Algorithm.Command
import assignment.actors.Algorithm.Command.{FileStatistic, Start, Stop}

import java.nio.file.Path
import assignment.mvc.view.View

object Algorithm:
  enum Command:
    case Start(path: Path, reportConfiguration: ReportConfiguration, view: View)
    case Stop
    case FileStatistic(statistic: Statistic)

  def apply(): Behavior[Command] = idle()

  private def idle(): Behavior[Command] =
    println("Algorithm in idle state")
    Behaviors.receiveMessage[Command] {
        case Start(path, reportConfiguration, view) => started(path, reportConfiguration, view)
        case _ => Behaviors.same
    }


  private def started(path: Path, reportConfiguration: ReportConfiguration, view: View): Behavior[Command] =
    import Command.*
    // Handle the termination of the folderScanner
    println("Algorithm in started state")
    Behaviors.setup { context =>

      val notificationListener = context.spawn(ViewNotificationListeners(view), "notificationListeners")
      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListener), "reportBuilder")

      val folderScanner = context.spawn(FolderScanner(), "folderScanner")
      context.watch(folderScanner)
      folderScanner ! FolderScanner.Command.Scan(path, context.self)

      Behaviors.receiveMessage[Command] {
        case FileStatistic(statistic) =>
          println(s"Received statistic: $statistic")
          reportBuilder ! ReportBuilder.Command.AddStatistic(statistic)
          Behaviors.same
        case Stop =>
          println("Stopping Algorithm")
          context.stop(folderScanner)
          Behaviors.receiveSignal {
            case (_, Terminated(_)) =>
              println("FolderScanner terminated")
              context.watch(reportBuilder)
              reportBuilder ! ReportBuilder.Command.Complete
              Behaviors.receiveSignal {
                case (_, Terminated(_)) =>
                  println("ReportBuilder terminated")
                  context.stop(notificationListener)
                  context.stop(reportBuilder)
                  idle()
              }
          }
        case _ => Behaviors.same
      }.receiveSignal {
        case (_, Terminated(_)) =>
          println("FolderScanner terminated")
          reportBuilder ! ReportBuilder.Command.Complete
          notificationListener ! ViewNotificationListeners.Command.Stop
          idle()
      }

    }

  private def stopping(folderScanner: ActorRef[FolderScanner.Command], reportBuilder: ActorRef[ReportBuilder.Command], notificationListener: ActorRef[ViewNotificationListeners.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      Behaviors.receiveSignal {
        case (_, Terminated(_)) =>
          println("ReportBuilder terminated")
          context.stop(notificationListener)
          context.stop(reportBuilder)
          idle()
      }
    }