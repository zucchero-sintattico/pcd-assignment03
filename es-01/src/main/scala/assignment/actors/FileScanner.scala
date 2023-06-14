package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import java.nio.file.Path

object FileScanner:
  enum Command:
    case Scan(path: Path, replyTo: ActorRef[ReportAggregator.Command])

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, replyTo) =>
          // TODO: scan file
          Behaviors.same
      }
    }
