package assignment.controller;

import assignment.client.ObservableClient;
import assignment.client.PixelArtClient;
import assignment.model.Model;
import assignment.view.View;

public class PixelArtController implements Controller {

    private final ObservableClient client = new PixelArtClient();
    private Model model;
    private View view;

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void join(final String sessionId) {
        this.setupListeners();
        this.client.join(sessionId);
    }

    @Override
    public String create() {
        this.setupListeners();
        final String sessionId = this.client.create();
        this.client.join(sessionId);
        return sessionId;
    }

    private void setupListeners() {
        this.client.setOnUserJoinListener((clientId) -> {
            this.view.onNewPlayer(clientId);
        });
        this.client.setOnUserLeaveListener((clientId) -> {
            this.view.onPlayerLeave(clientId);
        });
        this.client.setOnNewMousePositionListener(mousePosition -> {
            this.view.onNewMousePosition(mousePosition.clientId, mousePosition.x, mousePosition.y);
        });
        this.client.setOnPixelUpdatedListener(pixelInfo -> {
            this.view.onPixelUpdated(pixelInfo.x, pixelInfo.y, pixelInfo.color);
        });
    }

    @Override
    public void leave() {
        this.client.leave();
    }

    @Override
    public void updateMousePosition(int x, int y) {
        this.client.updateMousePosition(x, y);
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        this.client.updatePixel(x, y, color);
    }

}
