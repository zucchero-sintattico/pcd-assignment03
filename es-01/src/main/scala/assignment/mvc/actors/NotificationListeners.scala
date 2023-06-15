package assignment.mvc.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.mvc.{Statistic, actors}

import scala.collection.immutable.Range

object NotificationListeners:

  enum Command:
    case NumberOfFilesChanged(numberOfFiles: Int)
    case TopNChanged(top: List[Statistic])
    case DistributionChanged(distribution: Map[actors.Range, Int])

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Command.NumberOfFilesChanged(numberOfFiles) =>
        println(s"Number of files changed to $numberOfFiles")
        Behaviors.same
      case Command.TopNChanged(top) =>
        println(s"Top N changed to $top")
        Behaviors.same
      case Command.DistributionChanged(distribution) =>
        println(s"Distribution changed to $distribution")
        Behaviors.same
    }
