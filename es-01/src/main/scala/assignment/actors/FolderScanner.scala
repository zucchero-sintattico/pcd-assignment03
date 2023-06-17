package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior, Terminated}
import assignment.Domain.Statistic

import java.io.File
import java.nio.file.Path
import scala.math.Fractional.Implicits.infixFractionalOps
import assignment.actors.Algorithm

object FolderScanner:
  enum Command:
    private[FolderScanner] case NewFolder(folder: File)
    private[FolderScanner] case NewFile(file: File)
    private[FolderScanner] case FileScannerTerminated
    private[FolderScanner] case FolderScannerTerminated

  import Command.*

  def apply(path: Path, algorithm: ActorRef[Algorithm.Command]): Behavior[Command] =
    Behaviors.setup { context =>

      var directories = getDirectories(path)
      var files = getFiles(path)

      directories.foreach(context.self ! NewFolder(_))
      files.foreach(context.self ! NewFile(_))

      if directories.isEmpty && files.isEmpty then
        Behaviors.stopped
      else
        Behaviors.receiveMessage {
          case NewFolder(folder) =>
            val folderScanner = context.spawn(FolderScanner(folder.toPath, algorithm), folder.getName.hashCode.toString)
            context.watchWith(folderScanner, FolderScannerTerminated)
            Behaviors.same
          case NewFile(file) =>
            val fileScanner = context.spawn(FileScanner(file.toPath, algorithm), file.getName.hashCode.toString)
            context.watchWith(fileScanner, FileScannerTerminated)
            Behaviors.same
          case FileScannerTerminated =>
            files = files.tail
            if files.isEmpty && directories.isEmpty then
              Behaviors.stopped
            else
              Behaviors.same
          case FolderScannerTerminated =>
            directories = directories.tail
            if files.isEmpty && directories.isEmpty then
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




