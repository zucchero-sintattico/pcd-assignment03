package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.actors.FileScanner

import java.nio.file.Path

object FolderScanner:
  enum Command:
    case Scan(path: Path, replyTo: ActorRef[FileScanner.Command])

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, replyTo) =>
          // TODO: scan folder
          Behaviors.same
      }
    }
