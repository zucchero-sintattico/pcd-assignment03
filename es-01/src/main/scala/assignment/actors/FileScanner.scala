package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern.pipe
import assignment.Domain.Statistic
import assignment.Domain.*

import java.nio.file.Path
import scala.io.Source
import scala.util.Try
import assignment.actors.Algorithm

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object FileScanner:

  enum Command:
    private[FileScanner] case ScanResult(statistic: Statistic)

  import Command.*
  def apply(path: Path, algorithm: ActorRef[Algorithm.Command]): Behavior[Command] =
    Behaviors.setup { context =>

      context.self ! ScanResult(getStatistic(path))

      Behaviors.receiveMessage {
        case ScanResult(statistic) =>
          algorithm ! Algorithm.Command.FileStatistic(statistic)
          Behaviors.stopped
      }
    }

  private def getStatistic(path: Path): Statistic =
      Try {
        val source = Source.fromFile(path.toFile)
        val lines = source.getLines().toList.size
        source.close()
        Statistic(path, lines)
      }.getOrElse(Statistic(path, 0))

