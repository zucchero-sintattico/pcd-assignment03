package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import assignment.Statistic

import java.nio.file.Path
import scala.math.Fractional.Implicits.infixFractionalOps

object FolderScanner:
  enum Command:
    case Scan(path: Path, reportBuilder: ActorRef[ReportBuilder.Command])
    case Stop

  def apply(): Behavior[Command] = idle()

  def idle(): Behavior[Command] =
    import Command.*
    Behaviors.receiveMessage[Command] {
      case Scan(path, reportBuilder) => started(path, reportBuilder)
      case Stop => Behaviors.same
    }

  def started(path: Path, reportBuilder: ActorRef[ReportBuilder.Command]): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      var children = 0
      val allFiles = path.toFile.listFiles().toList
      val directories = allFiles.filter(_.isDirectory)
      val files = allFiles
        .filterNot(_.isDirectory)
        .filter(_.toString.endsWith(".java"))

      directories.foreach { directory =>
        val child = context.spawn(FolderScanner(), s"folderScanner-${directory.getName}")
        children += 1
        context.watch(child)
        child ! Scan(directory.toPath, reportBuilder)
      }

      files.foreach { file =>
        val child = context.spawn(FileScanner(), s"fileScanner-${file.getName}")
        children += 1
        context.watch(child)
        child ! FileScanner.Command.Scan(file.toPath, reportBuilder)
      }

      if children == 0 then
        Behaviors.stopped
      else
        Behaviors.receiveMessage[Command] {
          case Scan(_, _) => Behaviors.same
          case Stop =>
            Behaviors.stopped
        }.receiveSignal {
          case (_, Terminated(ref)) =>
            children -= 1
            if children == 0 then
              Behaviors.stopped
            else
              Behaviors.same
        }
    }

