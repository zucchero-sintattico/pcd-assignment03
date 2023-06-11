import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors

object ExampleActor:
  enum Command:
    case Hello(name: String)
    case Goodbye

  def apply(): Behavior[Command] =
    Behaviors.receiveMessage {
      case Command.Hello(name) =>
        println(s"Hello, $name!")
        Behaviors.same
      case Command.Goodbye =>
        println("Goodbye!")
        Behaviors.stopped
    }

@main def main(): Unit =
  val system = ActorSystem(ExampleActor(), "example")
  system ! ExampleActor.Command.Hello("Alice")
  system ! ExampleActor.Command.Goodbye



