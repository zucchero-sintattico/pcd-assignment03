package assignment.controller;

import assignment.client.ObservableClient;
import assignment.client.PixelArtClient;
import assignment.utils.MousePosition;
import assignment.utils.PixelGrid;
import assignment.view.View;

import java.util.Map;

public class PixelArtController implements Controller {

    private final ObservableClient client = new PixelArtClient();
    private View view;

    @Override
    public String getClientId() {
        return this.client.getID();
    }

    @Override
    public PixelGrid getGrid() {
        return this.client.getModel().getGrid();
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    private int randomColor() {
        return (int) (Math.random() * 256 * 256 * 256);
    }
    @Override
    public void join(final String sessionId) {
        this.setupListeners();
        this.client.join(sessionId, randomColor());
    }

    @Override
    public String create(String sessionId) {
        this.setupListeners();
        this.client.create(sessionId);
        this.client.join(sessionId, randomColor());
        return sessionId;
    }

    private void setupListeners() {
        this.client.setOnModelReadyListener((model) -> {
            this.view.onModelReady(model);
        });
        this.client.setOnUserJoinListener((entry) -> {
            this.view.onNewPlayer(entry.getKey(), entry.getValue());
        });
        this.client.setOnUserLeaveListener((clientId) -> {
            this.view.onPlayerLeave(clientId);
        });
        this.client.setOnUserColorUpdateListener(entry -> {
            this.view.onPlayerColorUpdate(entry.getKey(), entry.getValue());
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

    @Override
    public int getUserColor() {
        return this.client.getModel().getPlayersColor().get(this.client.getID());
    }

    @Override
    public Map<String, Integer> getPlayersColor() {
        return this.client.getModel().getPlayersColor();
    }

    @Override
    public Map<String, MousePosition> getPlayersMouse() {
        return this.client.getModel().getPlayersMouse();
    }

    @Override
    public void updateUserColor(int rgb) {
        this.client.updateUserColor(rgb);
    }

}
