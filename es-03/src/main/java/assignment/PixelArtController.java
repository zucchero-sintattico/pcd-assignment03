package assignment;

import assignment.client.Client;
import assignment.client.PixelArtClient;
import assignment.model.Model;
import assignment.model.ModelImpl;

import static java.rmi.registry.LocateRegistry.getRegistry;

public class PixelArtController {

    private final Client client;

    public PixelArtController() {
        this(new ModelImpl());
    }
    public PixelArtController(final Model model) {
        this.client = new PixelArtClient(model);
    }

    public void join(final String sessionId) {
        this.client.join(sessionId);
    }

    public String create() {
        return this.client.create();
    }

    public void leave() {
        this.client.leave();
    }

}
