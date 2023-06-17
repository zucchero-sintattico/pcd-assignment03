package assignment.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.actors
import assignment.Domain.*
import assignment.actors.Algorithm.AppListener
import assignment.mvc.view.View


object ViewNotificationListeners:

  enum Command:
    case Start
    case Complete
    case Stop
    case NumberOfFilesChanged(numberOfFiles: Int)
    case TopNChanged(top: List[Statistic])
    case DistributionChanged(distribution: Map[Range, Int])

  def apply(appListener: AppListener): Behavior[Command] =
    import Command._
    Behaviors.receiveMessage {
      case Start =>
        appListener.onStart()
        Behaviors.same
      case Complete =>
        appListener.onComplete()
        Behaviors.stopped
      case Stop =>
        appListener.onStop()
        Behaviors.stopped
      case NumberOfFilesChanged(numberOfFiles) =>
        appListener.onNumberOfFileChanged(numberOfFiles)
        Behaviors.same
      case TopNChanged(top) =>
        appListener.onTopNChanged(top)
        Behaviors.same
      case DistributionChanged(distribution) =>
        appListener.onDistributionChanged(distribution)
        Behaviors.same
    }
