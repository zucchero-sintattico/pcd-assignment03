package assignment.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.Statistic

object ReportBuilder:
  enum Command:
    case AddStatistic(statistic: Statistic)
    case Complete

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case AddStatistic(statistic) =>
          println(s"Received statistic: $statistic")
          Behaviors.same
        case Complete =>
          println("Completed")
          Behaviors.stopped
      }
    }
