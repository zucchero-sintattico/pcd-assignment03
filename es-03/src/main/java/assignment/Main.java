package assignment;

import assignment.client.PixelArtClient;

import java.util.List;

import static java.rmi.registry.LocateRegistry.getRegistry;

public class Main {

    public static void main(String[] args) throws InterruptedException {

        final List<PixelArtController> controllers = List.of(
                new PixelArtController(),
                new PixelArtController(),
                new PixelArtController()
        );

        final String sessionId = controllers.get(0).create();
        controllers.stream()
                .skip(1)
                .forEach(controller -> controller.join(sessionId));

        controllers.forEach(PixelArtController::leave);
    }
}