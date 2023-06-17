package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.Domain.Statistic
import assignment.Domain._

import java.nio.file.Path
import scala.io.Source
import scala.util.Try

import assignment.actors.Algorithm

object FileScanner:


  def apply(path: Path, algorithm: ActorRef[Algorithm.Command]): Behavior[Nothing] =
    println(s"FileScanner: ${path.getFileName}")
    Behaviors.setup { context =>
      val statistic = getStatistic(path)
      algorithm ! Algorithm.Command.FileStatistic(statistic)
      Behaviors.stopped
    }

  private def getStatistic(path: Path): Statistic =
    Try {
      val source = Source.fromFile(path.toFile)
      val lines = source.getLines().toList.size
      source.close()
      Statistic(path, lines)
    }.getOrElse(Statistic(path, 0))
