package assignment;

import assignment.client.Client;
import assignment.client.ObservableClient;
import assignment.client.PixelArtClient;
import assignment.model.Model;
import assignment.model.ModelImpl;

import static java.rmi.registry.LocateRegistry.getRegistry;

public class PixelArtController {

    private final ObservableClient client = new PixelArtClient();

    public void join(final String sessionId) {
        this.client.join(sessionId);
    }

    public String create() {
        return this.client.create();
    }

    private void setupListeners() {
        this.client.setOnUserJoinListener((clientId) -> {
            System.out.println("New user joined: " + clientId);
        });
        this.client.setOnUserLeaveListener((clientId) -> {
            System.out.println("User left: " + clientId);
        });
        this.client.setOnNewMousePositionListener(mousePosition -> {
            System.out.println("New mouse position: " + mousePosition);
        });
        this.client.setOnPixelUpdatedListener(pixelInfo -> {
            System.out.println("Pixel updated: " + pixelInfo);
        });
    }
    public void leave() {
        this.client.leave();
    }

}
