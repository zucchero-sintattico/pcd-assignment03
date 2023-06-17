package assignment

import java.nio.file.Path

object Domain:
  case class Statistic(path: Path, size: Long)
  case class Range(min: Int, max: Int)
  case class ReportConfiguration(n: Int, nOfIntervals: Int, maxLines: Int)

  class Report(val reportConfiguration: ReportConfiguration):
    var configuration: ReportConfiguration = reportConfiguration
    var statistics: List[Statistic] = List.empty
    var topStatistics: List[Statistic] = List.empty
    var distribution: Map[Range, Int] = Map.empty
