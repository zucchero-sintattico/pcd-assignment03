package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy, Terminated}
import assignment.Domain.*
import assignment.actors.Algorithm.Command
import assignment.actors.Algorithm.Command.{FileStatistic, Start, Stop}

import java.nio.file.Path
import assignment.mvc.view.View

object Algorithm:

  trait AppListener:
    def onStart(): Unit
    def onStop(): Unit
    def onComplete(): Unit

    def onNumberOfFileChanged(numberOfFile: Int): Unit
    def onTopNChanged(topN: List[Statistic]): Unit
    def onDistributionChanged(distribution: Map[Range, Int]): Unit

  enum Command:
    case Start(path: Path, reportConfiguration: ReportConfiguration, appListener: AppListener)
    case Stop
    case FileStatistic(statistic: Statistic)

  def apply(): Behavior[Command] = idle()

  private def idle(): Behavior[Command] =
    println("Algorithm in idle state")
    Behaviors.receiveMessage[Command] {
        case Start(path, reportConfiguration, appListener) => started(path, reportConfiguration, appListener)
        case _ => Behaviors.same
    }


  private def started(path: Path, reportConfiguration: ReportConfiguration, appListener: AppListener): Behavior[Command] =
    import Command.*
    // Handle the termination of the folderScanner
    println("Algorithm in started state")
    Behaviors.setup { context =>

      val notificationListener = context.spawn(ViewNotificationListeners(appListener), "notificationListeners")
      notificationListener ! ViewNotificationListeners.Command.Start

      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListener), "reportBuilder")

      val folderScanner = context.spawn(FolderScanner(), "folderScanner")
      context.watch(folderScanner)
      folderScanner ! FolderScanner.Command.Scan(path, context.self)

      var counter = 0
      Behaviors.receiveMessage[Command] {
        case FileStatistic(statistic) =>
          counter += 1
          println(s"$counter files scanned")
          reportBuilder ! ReportBuilder.Command.AddStatistic(statistic)
          Behaviors.same
        case Stop =>
          println("Stopping Algorithm")
          context.stop(folderScanner)
          context.unwatch(folderScanner)
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