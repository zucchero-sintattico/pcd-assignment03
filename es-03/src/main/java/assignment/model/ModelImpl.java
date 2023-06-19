package assignment.model;

import assignment.utils.MousePosition;
import assignment.utils.PixelGrid;
import assignment.utils.Position;

import java.io.Serializable;
import java.util.*;

public class ModelImpl implements Model, Serializable {

    private final List<String> clients = new ArrayList<>();
    private final Map<String, MousePosition> clientsMouse = new HashMap<>();
    private PixelGrid grid;

    @Override
    public List<String> getPlayers() {
        return Collections.unmodifiableList(clients);
    }

    @Override
    public Map<String, MousePosition> getPlayersMouse() {
        return Collections.unmodifiableMap(clientsMouse);
    }

    @Override
    public PixelGrid getGrid() {
        return grid;
    }

    @Override
    public void setGrid(PixelGrid grid) {
        this.grid = grid;
    }

    @Override
    public void addClient(String clientId) {
        if (!clients.contains(clientId)) {
            clients.add(clientId);
        }
    }

    @Override
    public void removeClient(String clientId) {
        clients.remove(clientId);
    }

    @Override
    public void updateMousePosition(String clientId, int x, int y) {
        clientsMouse.put(clientId, new MousePosition(clientId, x, y));
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        grid.set(x, y, color);
    }
}
