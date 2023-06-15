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
      var children = List.empty[ActorRef[_]]
      val allFiles = path.toFile.listFiles().toList
      val directories = allFiles
        .filter(_.isDirectory)
        .filterNot(_.getName.startsWith(".")) // ignore hidden directories

      val files = allFiles
        .filterNot(_.isDirectory)
        .filter(_.toString.endsWith(".java"))

      directories.foreach { directory =>
        val child = context.spawn(FolderScanner(), s"folderScanner-${directory.getName.hashCode}")
        children = children :+ child
        context.watch(child)
        child ! Scan(directory.toPath, algorithm)
      }

      files.foreach { file =>
        val child = context.spawn(FileScanner(), s"fileScanner-${file.getName.hashCode()}")
        children = children :+ child
        context.watch(child)
        child ! FileScanner.Command.Scan(file.toPath, algorithm)
      }

      if children.isEmpty then
        Behaviors.stopped
      else
        Behaviors.receiveMessage[Command] {
          case Scan(_, _) => Behaviors.same
          case Stop =>
            children.foreach(context.stop)
            Behaviors.stopped
        }.receiveSignal {
          case (_, Terminated(ref)) =>
            children = children.filterNot(_ == ref)
            if children.isEmpty then
              println("All children stopped, stopping myself")
              Behaviors.stopped
            else
              Behaviors.same
        }
    }


