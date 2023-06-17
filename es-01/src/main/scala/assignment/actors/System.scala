package assignment.actors

import akka.actor.typed.{ActorRef, Behavior, scaladsl}
import akka.actor.typed.scaladsl.Behaviors
import assignment.Domain.ReportConfiguration
import assignment.actors.Algorithm.AppListener

import java.nio.file.Path

object System:

  enum Command:
    case StartAlgorithm(path: Path, reportConfiguration: ReportConfiguration, appListener: AppListener)
    case StopAlgorithm
    private[System] case AlgorithmCompleted

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      var listener: Option[AppListener] = None
      Behaviors.receiveMessage {
        case Command.StartAlgorithm(path, reportConfiguration, appListener) =>
          if context.child("algorithm").isEmpty then
            val algorithm = context.spawn(Algorithm(path, reportConfiguration, appListener), "algorithm")
            context.watchWith(algorithm, Command.AlgorithmCompleted)
            listener = Some(appListener)
          Behaviors.same
        case Command.StopAlgorithm =>
          context.child("algorithm").foreach(context.stop)
          listener.foreach(_.onStop())
          Behaviors.same
        case Command.AlgorithmCompleted =>
          Behaviors.same
      }
    }
