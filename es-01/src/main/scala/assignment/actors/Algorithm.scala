package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy, Terminated}
import assignment.Domain.*
import assignment.actors.ViewNotificationListeners.Command.Complete

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
    case Stop
    case FileStatistic(statistic: Statistic)
    private[Algorithm] case Completed
    private[Algorithm] case FolderScannerCompleted
    private[Algorithm] case ReportBuilderCompleted
    private[Algorithm] case NotificationListenerCompleted

  import Command.*

  def apply(path: Path, reportConfiguration: ReportConfiguration, appListener: AppListener): Behavior[Command] =
    Behaviors.setup { context =>
      println("Algorithm started")
      val notificationListener = context.spawn(ViewNotificationListeners(appListener), "notificationListeners")
      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListener), "reportBuilder")
      val folderScanner = context.spawn(FolderScanner(path, context.self), "folderScanner")

      context.watchWith(folderScanner, FolderScannerCompleted)
      context.watchWith(reportBuilder, ReportBuilderCompleted)
      context.watchWith(notificationListener, NotificationListenerCompleted)

      Behaviors.receiveMessage {
        case FolderScannerCompleted =>
          println("FolderScanner completed")
          reportBuilder ! ReportBuilder.Command.Complete
          Behaviors.same
        case ReportBuilderCompleted =>
          println("ReportBuilder completed")
          Behaviors.same
        case NotificationListenerCompleted =>
          println("NotificationListener completed")
          Behaviors.stopped
        case FileStatistic(statistic) =>
          println(s"FileStatistic: $statistic")
          reportBuilder ! ReportBuilder.Command.AddStatistic(statistic)
          Behaviors.same
        case Stop =>
          ??? // TODO
      }

    }