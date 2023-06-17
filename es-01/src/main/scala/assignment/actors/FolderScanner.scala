package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}

import java.nio.file.Path
import scala.math.Fractional.Implicits.infixFractionalOps

object FolderScanner:
  enum Command:
    case Scan(path: Path, algorithm: ActorRef[Algorithm.Command])
    case Stop

  def apply(): Behavior[Command] = idle()

  private def idle(): Behavior[Command] =
    import Command.*
    Behaviors.receiveMessage[Command] {
      case Scan(path, algorithm) => starting(path, algorithm)
      case Stop => Behaviors.stopped
    }

  private def starting(path: Path, algorithm: ActorRef[Algorithm.Command]): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      var folderScanners = List.empty[ActorRef[FolderScanner.Command]]
      var fileScanners = List.empty[ActorRef[FileScanner.Command]]

      val allFiles = path.toFile.listFiles().toList
      val directories = allFiles
        .filter(_.isDirectory)
        .filterNot(_.getName.startsWith(".")) // ignore hidden directories

      val files = allFiles
        .filterNot(_.isDirectory)
        .filter(_.toString.endsWith(".java"))

      directories.foreach { directory =>
        val child = context.spawn(FolderScanner(), s"folderScanner-${directory.getName.hashCode}")
        folderScanners = folderScanners :+ child
        context.watch(child)
        child ! Scan(directory.toPath, algorithm)
      }

      files.foreach { file =>
        val child = context.spawn(FileScanner(), s"fileScanner-${file.getName.hashCode()}")
        fileScanners = fileScanners :+ child
        context.watch(child)
        child ! FileScanner.Command.Scan(file.toPath, algorithm)
      }

      if folderScanners.isEmpty && fileScanners.isEmpty then
        Behaviors.stopped
      else
        Behaviors.receiveMessage[Command] {
          case Stop =>
            folderScanners.foreach(_ ! Stop)
            fileScanners.foreach(_ ! FileScanner.Command.Stop)
            Behaviors.same
          case _ => Behaviors.same
        }.receiveSignal {
          case (_, Terminated(ref: ActorRef[_])) =>
              folderScanners = folderScanners.filterNot(_ == ref)
              fileScanners = fileScanners.filterNot(_ == ref)
              if folderScanners.isEmpty && fileScanners.isEmpty then
                Behaviors.stopped
              else
                Behaviors.same
        }
    }


