package assignment;

import assignment.controller.Controller;
import assignment.controller.PixelArtController;

import java.util.List;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        final List<Controller> controllers = List.of(
                new PixelArtController(),
                new PixelArtController(),
                new PixelArtController()
        );

        final String sessionId = controllers.get(0).create();
        controllers.stream()
                .skip(1)
                .forEach(controller -> controller.join(sessionId));

        controllers.forEach(controller -> controller.updateMousePosition(1, 1));
        controllers.forEach(controller -> controller.updatePixel(1, 1, 0));
        controllers.forEach(Controller::leave);
    }
}