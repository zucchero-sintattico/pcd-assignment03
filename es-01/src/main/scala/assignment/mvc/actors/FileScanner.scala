package assignment.mvc.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.mvc.Statistic

import java.nio.file.Path
import scala.io.Source
import scala.util.Try

object FileScanner:
  enum Command:
    case Scan(path: Path, replyTo: ActorRef[ReportBuilder.Command])

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, reportBuilder) =>
          println(s"Scanning file: $path")
          val statistic = Try {
            val source = Source.fromFile(path.toFile)
            val lines = source.getLines().toList.size
            source.close()
            Statistic(path, lines)
          }.getOrElse(Statistic(path, 0))
          println(s"Scanned file: $path")
          reportBuilder ! ReportBuilder.Command.AddStatistic(statistic)
          Behaviors.stopped
      }
    }
