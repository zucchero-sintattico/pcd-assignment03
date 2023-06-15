import assignment.mvc.controller.{Controller, ControllerImpl}
import assignment.mvc.view.{View, ViewImpl}

object MainGUI extends App:
  val controller = new ControllerImpl
  val view = new ViewImpl

  view.setController(controller)
  controller.setView(view)

  view.start()
