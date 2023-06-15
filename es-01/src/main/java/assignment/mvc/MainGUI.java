package assignment.mvc;


import assignment.mvc.controller.Controller;
import assignment.mvc.controller.ControllerImpl;
import assignment.mvc.view.View;
import assignment.mvc.view.ViewImpl;
import assignment.mvc.view.View;
import assignment.mvc.view.ViewImpl;

public class MainGUI {
    static public void main(String[] args) {

        final Controller controller = new ControllerImpl();
        final View view = new ViewImpl();

        view.setController(controller);
        controller.setView(view);

        view.start();
    }
}

