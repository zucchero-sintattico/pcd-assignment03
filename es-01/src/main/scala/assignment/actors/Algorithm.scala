package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, PostStop, SupervisorStrategy, Terminated}
import assignment.Domain.*
import assignment.actors.ViewNotificationListeners.Command.Complete

import java.nio.file.Path
import assignment.mvc.view.View

object Algorithm:

  trait AppListener:
    def onStart(): Unit
    def onStop(): Unit
    def onComplete(report: Report): Unit

    def onNumberOfFileChanged(numberOfFile: Int): Unit
    def onTopNChanged(topN: List[Statistic]): Unit
    def onDistributionChanged(distribution: Map[Range, Int]): Unit

  enum Command:
    case FileStatistic(statistic: Statistic)
    private[Algorithm] case FolderScannerCompleted
    private[Algorithm] case AllCompleted

  import Command.*

  def apply(path: Path, reportConfiguration: ReportConfiguration, appListener: AppListener): Behavior[Command] =
    Behaviors.setup { context =>

      val notificationListener = context.spawn(ViewNotificationListeners(appListener), "notificationListeners")
      context.watchWith(notificationListener, AllCompleted)

      val folderScanner = context.spawn(FolderScanner(path, context.self), "folderScanner")
      context.watchWith(folderScanner, FolderScannerCompleted)

      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListener), "reportBuilder")

      notificationListener ! ViewNotificationListeners.Command.Start

      Behaviors.receiveMessage[Command] {
        case FileStatistic(statistic) =>
          reportBuilder ! ReportBuilder.Command.AddStatistic(statistic)
          Behaviors.same
        case FolderScannerCompleted =>
          reportBuilder ! ReportBuilder.Command.Complete
          Behaviors.same
        case AllCompleted =>
          Behaviors.stopped
      }
    }
