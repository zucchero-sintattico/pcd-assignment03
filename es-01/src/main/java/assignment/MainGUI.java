package assignment;


import assignment.controller.Controller;
import assignment.controller.ControllerImpl;
import assignment.view.View;
import assignment.view.ViewImpl;

public class MainGUI {
    static public void main(String[] args) {

        final Controller controller = new ControllerImpl();
        final View view = new ViewImpl();

        view.setController(controller);
        controller.setView(view);

        view.start();
    }
}

