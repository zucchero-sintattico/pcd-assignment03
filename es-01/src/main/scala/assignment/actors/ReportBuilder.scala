package assignment.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import assignment.Statistic

case class Range(min: Int, max: Int)

case class ReportConfiguration(n: Int, nOfIntervals: Int, maxLines: Int)

class Report(val reportConfiguration: ReportConfiguration):
  var configuration: ReportConfiguration = reportConfiguration
  var statistics: List[Statistic] = List.empty
  var topStatistics: List[Statistic] = List.empty
  var distribution: Map[Range, Int] = Map.empty

object ReportBuilder:
  enum Command:
    case AddStatistic(statistic: Statistic)
    case Complete

  def apply(reportConfiguration: ReportConfiguration, replyTo: ActorRef[NotificationListeners.Command]): Behavior[Command] =
    import Command.*
    var statisticsList: List[Statistic] = List.empty
    val report = new Report(reportConfiguration)

    Behaviors.setup { context =>
      Behaviors.receiveMessage {

        case AddStatistic(statistic) =>
          statisticsList = statisticsList :+ statistic
          // update topN

          // check if topN is not full
          if (report.topStatistics.size < report.configuration.n) {
            this.insertTopSorted(report, statistic)
            replyTo ! NotificationListeners.Command.TopNChanged(report.topStatistics)
          }
          // check if topN is full and the new statistic is bigger than the smallest
          else if (report.topStatistics.apply(report.configuration.n - 1).size < statistic.size) {
            report.topStatistics = report.topStatistics.dropRight(1)
            this.insertTopSorted(report, statistic)
            replyTo ! NotificationListeners.Command.TopNChanged(report.topStatistics)
          }

          // update distribution
          for range <- report.distribution.keys do
            if (statistic.size >= range.min && statistic.size <= range.max) then
              report.distribution = report.distribution.updated(range, report.distribution.apply(range) + 1)
              replyTo ! NotificationListeners.Command.DistributionChanged(report.distribution)

          replyTo ! NotificationListeners.Command.NumberOfFilesChanged(statisticsList.size)
          Behaviors.same
        case Complete =>
          println("Completed")
          Behaviors.stopped
      }
    }

  private def insertTopSorted(report: Report, statistic: Statistic): Unit = {
    report.topStatistics = report.topStatistics :+ statistic
    report.topStatistics = report.topStatistics.sortBy(_.size)
  }
