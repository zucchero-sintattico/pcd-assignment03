package assignment.mvc.controller
import akka.actor.typed.ActorSystem
import assignment.Domain.ReportConfiguration
import assignment.actors.Algorithm
import assignment.mvc.view.View

import java.nio.file.Path

class ControllerImpl extends Controller:

  private var view: View = _
  private var status = AlgorithmStatus.IDLE
  private val algorithm = ActorSystem(Algorithm(), "algorithm")

  override def setView(view: View): Unit = this.view = view

  override def startAlgorithm(path: Path, topN: Int, nOfIntervals: Int, maxL: Int): Unit =
    if status != AlgorithmStatus.RUNNING then
      val reportConfiguration = ReportConfiguration(topN, nOfIntervals, maxL)
      algorithm ! Algorithm.Command.Start(path, reportConfiguration, this.view)


      status = AlgorithmStatus.RUNNING
      view.updateAlgorithmStatus(status)


  override def stopAlgorithm(): Unit =
    if status == AlgorithmStatus.RUNNING then
      algorithm ! Algorithm.Command.Stop
      status = AlgorithmStatus.STOPPED
      view.updateAlgorithmStatus(status)
