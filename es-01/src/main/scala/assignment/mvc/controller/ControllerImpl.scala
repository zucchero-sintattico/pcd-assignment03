package assignment.mvc.controller
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import assignment.Domain.*
import assignment.Domain.ReportConfiguration
import assignment.actors
import assignment.actors.{Algorithm, System}
import assignment.actors.Algorithm.AppListener
import assignment.mvc.view.View

import java.nio.file.Path




class ControllerImpl extends Controller:

  private var view: View = _
  private var status = AlgorithmStatus.IDLE
  private var system = ActorSystem(System(), "System")

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
        override def onComplete(report: Report): Unit = updateStatus(AlgorithmStatus.FINISHED)

        override def onNumberOfFileChanged(numberOfFile: Int): Unit =
          view.updateNumberOfFiles(numberOfFile)
        override def onTopNChanged(topN: List[Statistic]): Unit =
          import scala.jdk.CollectionConverters.SeqHasAsJava
          view.updateTopN(topN.asJava)
        override def onDistributionChanged(distribution: Map[Range, Int]): Unit =
          import scala.jdk.CollectionConverters.MapHasAsJava
          view.updateDistribution(distribution.asInstanceOf[Map[Range, Integer]].asJava)

      system ! System.Command.StartAlgorithm(path, reportConfiguration, appListener)


  override def stopAlgorithm(): Unit =
    if status == AlgorithmStatus.RUNNING then
      system ! System.Command.StopAlgorithm
