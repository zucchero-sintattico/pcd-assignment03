package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}

import java.io.File
import java.nio.file.Path
import scala.math.Fractional.Implicits.infixFractionalOps
import assignment.actors.Algorithm

object FolderScanner:
  enum Command:
    case Stop

  import Command.*

  def apply(path: Path, algorithm: ActorRef[Algorithm.Command]): Behavior[Command] =
    Behaviors.setup { context =>
      println(s"FolderScanner: ${path.toFile.getName}")
      val directories = getDirectories(path)
      val files = getFiles(path)

      var folderScanners = directories.map { directory =>
        val child = context.spawn(FolderScanner(directory.toPath, algorithm), s"folderScanner-${directory.getName.hashCode}")
        context.watch(child)
        child
      }

      var fileScanners = files.map { file =>
        val child = context.spawn(FileScanner(file.toPath, algorithm), s"fileScanner-${file.getName.hashCode}")
        context.watch(child)
        child
      }

      if folderScanners.isEmpty && fileScanners.isEmpty then
        Behaviors.stopped
      else
        Behaviors.receiveMessage[Command] {
          case Stop =>
            folderScanners.foreach(context.stop)
            fileScanners.foreach(context.stop)
            Behaviors.same
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

  private def getDirectories(path: Path): Seq[File] =
    path.toFile.listFiles()
      .filter(_.isDirectory)
      .filterNot(_.getName.startsWith("."))
      .toSeq

  private def getFiles(path: Path): Seq[File] =
    path.toFile.listFiles()
      .filterNot(_.isDirectory)
      .filter(_.toString.endsWith(".java"))
      .toSeq




