package assignment.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment.Statistic

import java.nio.file.Path

object NotificationListeners:

  enum Command:
    case NumberOfFilesChanged(numberOfFiles: Int)
    case TopNChanged(top: List[Statistic])
    case DistributionChanged(distribution: Map[Range, Int])

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

object Algorithm:

  enum Command:
    case Start(path: Path)
    case Stop

  def idle(): Behavior[Command] = Behaviors.receiveMessage {
    case Command.Start(path, notifyTo) =>
      running(path, notifyTo)
    case Command.Stop =>
      Behaviors.stopped
  }

  def running(path: Path, notifyTo: ActorRef[NotificationListeners.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      val scanner = context.spawn(FolderScanner(), "scanner")

      Behaviors.receiveMessage {
        case Command.Stop =>
          Behaviors.stopped
        case Command.Start(path, notifyTo) =>
          running(path, notifyTo)
      }
    }

  def completed(): Behavior[Command] = Behaviors.stopped

  def apply(): Behavior[Command] = idle()