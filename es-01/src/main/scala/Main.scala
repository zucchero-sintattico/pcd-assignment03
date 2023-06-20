
import akka.actor.typed.receptionist.{Receptionist, ServiceKey}
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import assignment.Domain
import assignment.Domain.{Report, ReportConfiguration, Statistic}
import assignment.actors.Algorithm.AppListener
import assignment.actors.{Algorithm, System}

import java.lang
import java.nio.file.Path

@main def main(): Unit =
  val system = ActorSystem(System(), "system")
  val commandLineViewListener = new AppListener {
    private var startTime: Long = 0
    private var endTime: Long = 0
    override def onStart(): Unit =
      println("Started")
      startTime = lang.System.currentTimeMillis()
    override def onStop(): Unit =
      println("Stopped")


    override def onComplete(report: Report): Unit =
      println("\nCompleted: " + report)
      endTime = lang.System.currentTimeMillis()
      println(s"Time: ${(endTime - startTime)} ms")

    private def logInSameLine(numberOfFile: Int): Unit = {
      print(s"\rFiles: $numberOfFile")
    }

    override def onNumberOfFileChanged(numberOfFile: Int): Unit = logInSameLine(numberOfFile)
    override def onTopNChanged(topN: List[Statistic]): Unit = print("")
    override def onDistributionChanged(distribution: Map[Domain.Range, Int]): Unit = print("")
  }
  val reportConfiguration = ReportConfiguration(
    n = 10,
    nOfIntervals = 10,
    maxLines = 1000
  )

  system ! System.Command.StartAlgorithm(
    Path.of("../../pcd-assignment01/generator/generated_files"),
    reportConfiguration,
    commandLineViewListener
  )



