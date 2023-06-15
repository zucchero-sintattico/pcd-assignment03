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

  def apply(reportConfiguration: ReportConfiguration, notifyTo: ActorRef[NotificationListeners.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      import Command.*
      val report = new Report(reportConfiguration)
      // fill the distribution map
      fillDistritionMap(report, reportConfiguration)
      Behaviors.receiveMessage {
        case AddStatistic(statistic) =>
          println("Received statistic: " + statistic)
          report.statistics = report.statistics :+ statistic
          // update topN

          // check if topN is not full
          if (report.topStatistics.size < report.configuration.n) {
            this.insertTopSorted(report, statistic)
            notifyTo ! NotificationListeners.Command.TopNChanged(report.topStatistics)
          }
          // check if topN is full and the new statistic is bigger than the smallest
          else if (report.topStatistics.apply(report.configuration.n - 1).size < statistic.size) {
            report.topStatistics = report.topStatistics.dropRight(1)
            this.insertTopSorted(report, statistic)
            notifyTo ! NotificationListeners.Command.TopNChanged(report.topStatistics)
          }

          // update distribution
          for range <- report.distribution.keys do
            if (statistic.size >= range.min && statistic.size <= range.max) then
              report.distribution = report.distribution.updated(range, report.distribution.apply(range) + 1)
              println("im here")
              notifyTo ! NotificationListeners.Command.DistributionChanged(report.distribution)

          notifyTo ! NotificationListeners.Command.NumberOfFilesChanged(report.statistics.size)
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

  private def fillDistritionMap(report: Report, configuration: ReportConfiguration): Unit =
    val rangeSize = configuration.maxLines / configuration.nOfIntervals
    for i <- 0 until configuration.nOfIntervals do
      val range = Range(i * rangeSize, (i + 1) * rangeSize)
      report.distribution = report.distribution.updated(range, 0)
