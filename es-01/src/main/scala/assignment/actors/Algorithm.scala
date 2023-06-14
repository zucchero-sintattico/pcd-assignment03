package assignment.actors

import Algorithm.Command
import Algorithm.Command.{Start, Stop}
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import akka.actor.typed.scaladsl.Behaviors
import assignment.Statistic

import java.nio.file.Path

object Algorithm:
  enum Command:
    case Start(path: Path)
    case Stop

  def apply(): Behavior[Command] = idle()

  def idle(): Behavior[Command] =
    Behaviors.setup { context =>
      import Command._
      println("Algorithm started in idle state")
      Behaviors.receiveMessage[Command] {
        case Start(path) => started(path)
        case Stop => Behaviors.same
      }
    }

  def started(path: Path): Behavior[Command] =
    Behaviors.setup { context =>
      import Command._
      println("Algorithm switched to started state")
      val reportConfiguration = ReportConfiguration(10, 20, 30)
      val notificationListeners = context.spawn(NotificationListeners(), "notificationListeners")
      val reportBuilder = context.spawn(ReportBuilder(reportConfiguration, notificationListeners), "reportBuilder")
      val folderScanner = context.spawn(FolderScanner(), "folderScanner")
      context.watch(folderScanner)
      folderScanner ! FolderScanner.Command.Scan(path, reportBuilder)
      Behaviors.receiveMessage[Command] {
        case Start(path) => Behaviors.same
        case Stop =>
          println("Stopping Algorithm")
          context.stop(folderScanner)
          reportBuilder ! ReportBuilder.Command.Complete
          context.stop(reportBuilder)
          Behaviors.stopped
      }.receiveSignal {
        case (_, Terminated(_)) =>
          println("FolderScanner terminated")
          reportBuilder ! ReportBuilder.Command.Complete
          Behaviors.stopped
      }

    }