package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.Statistic

import java.nio.file.Path
import scala.io.Source
import scala.util.Try

object FileScanner:
  enum Command:
    case Scan(path: Path, replyTo: ActorRef[ReportAggregator.Command])

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, replyTo) =>
          // Create the Statistic with file lines
          // Use try with resources to close the file
          val statistic = Try {
            val source = Source.fromFile(path.toFile)
            val lines = source.getLines().toList.size
            source.close()
            Statistic(path, lines)
          }.getOrElse(Statistic(path, 0))
          replyTo ! ReportAggregator.Command.AddStatistic(statistic)
          Behaviors.stopped
      }
    }
