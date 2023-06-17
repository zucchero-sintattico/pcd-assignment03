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
        println("Received start")
        appListener.onStart()
        Behaviors.same
      case Complete =>
        println("Received complete")
        appListener.onComplete()
        Behaviors.stopped
      case Stop =>
        println("Received stop")
        appListener.onStop()
        Behaviors.stopped
      case NumberOfFilesChanged(numberOfFiles) =>
        println(s"Received number of files changed: $numberOfFiles")
        appListener.onNumberOfFileChanged(numberOfFiles)
        Behaviors.same
      case TopNChanged(top) =>
        println(s"Received top n changed: $top")
        appListener.onTopNChanged(top)
        Behaviors.same
      case DistributionChanged(distribution) =>
        println(s"Received distribution changed: $distribution")
        appListener.onDistributionChanged(distribution)
        Behaviors.same
    }
