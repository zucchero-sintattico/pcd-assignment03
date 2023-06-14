package assignment.actors

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import assignment.Statistic

import java.nio.file.Path

object FolderScanner:
  enum Command:
    case Scan(path: Path, aggregator: ActorRef[ReportAggregator.Command])

  def apply(): Behavior[Command] =
    import Command.*
    Behaviors.setup { context =>
      Behaviors.receiveMessage {
        case Scan(path, aggregator) =>
          // Scan the folder and spawn other folder scanners for subfolders
          val files = path.toFile.listFiles()
          files.foreach { file =>
            if file.isDirectory then
              context.spawn(FolderScanner(), file.getName) ! Scan(file.toPath, aggregator)
            else
              context.spawn(FileScanner(), file.getName) ! FileScanner.Command.Scan(file.toPath, aggregator)
          }
          Behaviors.same
      }
    }
