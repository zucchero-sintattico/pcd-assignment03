package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.Domain.Statistic
import assignment.Domain._

import java.nio.file.Path
import scala.io.Source
import scala.util.Try

object FileScanner:
  enum Command:
    case Scan(path: Path, algorithm: ActorRef[Algorithm.Command])
    case Stop

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Stop => Behaviors.stopped
        case Scan(path, reportBuilder) =>
          val statistic = Try {
            val source = Source.fromFile(path.toFile)
            val lines = source.getLines().toList.size
            source.close()
            Statistic(path, lines)
          }.getOrElse(Statistic(path, 0))
          reportBuilder ! Algorithm.Command.FileStatistic(statistic)
          Behaviors.stopped
      }
    }
