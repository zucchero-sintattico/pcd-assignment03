package assignment.mvc.controller
import akka.actor.typed.ActorSystem
import assignment.Domain._
import assignment.Domain.ReportConfiguration
import assignment.actors.Algorithm
import assignment.actors.Algorithm.AppListener
import assignment.mvc.view.View

import java.nio.file.Path

class ControllerImpl extends Controller:

  private var view: View = _
  private var status = AlgorithmStatus.IDLE
  private val algorithm = ActorSystem(Algorithm(), "algorithm")

  override def setView(view: View): Unit = this.view = view

  private def updateStatus(newStatus: AlgorithmStatus): Unit =
    status = newStatus
    view.updateAlgorithmStatus(status)

  override def startAlgorithm(path: Path, topN: Int, nOfIntervals: Int, maxL: Int): Unit =
    if status != AlgorithmStatus.RUNNING then
      val reportConfiguration = ReportConfiguration(topN, nOfIntervals, maxL)

      val appListener = new AppListener:
        override def onStart(): Unit = updateStatus(AlgorithmStatus.RUNNING)
        override def onStop(): Unit = updateStatus(AlgorithmStatus.STOPPED)
        override def onComplete(): Unit = updateStatus(AlgorithmStatus.FINISHED)

        override def onNumberOfFileChanged(numberOfFile: Int): Unit =
          view.updateNumberOfFiles(numberOfFile)
        override def onTopNChanged(topN: List[Statistic]): Unit =
          import scala.jdk.CollectionConverters.SeqHasAsJava
          view.updateTopN(topN.asJava)
        override def onDistributionChanged(distribution: Map[Range, Int]): Unit =
          import scala.jdk.CollectionConverters.MapHasAsJava
          view.updateDistribution(distribution.asInstanceOf[Map[Range, Integer]].asJava)

      algorithm ! Algorithm.Command.Start(path, reportConfiguration, appListener)


      status = AlgorithmStatus.RUNNING
      view.updateAlgorithmStatus(status)


  override def stopAlgorithm(): Unit =
    if status == AlgorithmStatus.RUNNING then
      algorithm ! Algorithm.Command.Stop
      status = AlgorithmStatus.STOPPED
      view.updateAlgorithmStatus(status)
