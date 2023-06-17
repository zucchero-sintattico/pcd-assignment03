package assignment.actors

import akka.actor.typed.{ActorRef, Behavior}
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
      var algorithm: Option[ActorRef[Algorithm.Command]] = None
      Behaviors.receiveMessage {
        case Command.StartAlgorithm(path, reportConfiguration, appListener) =>
          algorithm = Some(context.spawn(Algorithm(path, reportConfiguration, appListener), "algorithm"))
          context.watchWith(algorithm.get, Command.AlgorithmCompleted)
          Behaviors.same
        case Command.StopAlgorithm =>
          println("System: Stopping algorithm")
          algorithm.foreach(context.stop)
          algorithm = None
          Behaviors.same
        case Command.AlgorithmCompleted =>
          Behaviors.same
      }
    }
