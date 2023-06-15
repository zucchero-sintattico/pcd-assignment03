package assignment.actors

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import assignment.actors
import assignment.Domain.*
import assignment.mvc.view.View


object ViewNotificationListeners:

  enum Command:
    case NumberOfFilesChanged(numberOfFiles: Int)
    case TopNChanged(top: List[Statistic])
    case DistributionChanged(distribution: Map[Range, Int])

  def apply(view: View): Behavior[Command] =
    Behaviors.receiveMessage {
      case Command.NumberOfFilesChanged(numberOfFiles) =>
        view.updateNumberOfFiles(numberOfFiles)
        Behaviors.same
      case Command.TopNChanged(top) =>
        import scala.jdk.CollectionConverters.SeqHasAsJava
        val javaList: java.util.List[Statistic] = top.asJava
        view.updateTopN(javaList)
        Behaviors.same
      case Command.DistributionChanged(distribution) =>
        import collection.JavaConverters.mapAsJavaMapConverter
        val javaMap: java.util.Map[Range, Integer] = distribution.asJava.asInstanceOf[java.util.Map[Range, Integer]]
        view.updateDistribution(javaMap)

        Behaviors.same
    }
