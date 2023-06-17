package assignment.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.actors
import assignment.Domain.*
import assignment.actors.Algorithm.AppListener
import assignment.mvc.view.View

import scala.concurrent.duration.DurationInt


object ViewNotificationListeners:

  enum Command:
    case Start
    case Complete(report: Report)
    case Stop
    case NumberOfFilesChanged(numberOfFiles: Int)
    case TopNChanged(top: List[Statistic])
    case DistributionChanged(distribution: Map[Range, Int])
    private [ViewNotificationListeners] case NotifyChanges

  def apply(appListener: AppListener): Behavior[Command] =
    import Command._
    Behaviors.setup { context =>
      Behaviors.withTimers { timers =>
        var lastNumberOfFiles = 0
        var lastTopN = List.empty[Statistic]
        var lastDistribution = Map.empty[Range, Int]

        timers.startTimerWithFixedDelay(NotifyChanges, 100.millisecond)

        Behaviors.receiveMessage {
          case NotifyChanges =>
            appListener.onNumberOfFileChanged(lastNumberOfFiles)
            appListener.onTopNChanged(lastTopN)
            appListener.onDistributionChanged(lastDistribution)
            Behaviors.same
          case Start =>
            appListener.onStart()
            Behaviors.same
          case Complete(report) =>
            appListener.onNumberOfFileChanged(lastNumberOfFiles)
            appListener.onTopNChanged(lastTopN)
            appListener.onDistributionChanged(lastDistribution)
            appListener.onComplete(report)
            Behaviors.stopped
          case Stop =>
            appListener.onStop()
            Behaviors.stopped
          case NumberOfFilesChanged(numberOfFiles) =>
            if numberOfFiles != lastNumberOfFiles then
              lastNumberOfFiles = numberOfFiles
            Behaviors.same
          case TopNChanged(top) =>
            if top != lastTopN then
              lastTopN = top
            Behaviors.same
          case DistributionChanged(distribution) =>
            if distribution != lastDistribution then
              lastDistribution = distribution
            Behaviors.same
        }

      }
    }
