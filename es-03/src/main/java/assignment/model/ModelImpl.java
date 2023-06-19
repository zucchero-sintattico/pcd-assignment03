package assignment.model;

import assignment.utils.MousePosition;
import assignment.utils.PixelGrid;

import java.io.Serializable;
import java.util.*;

public class ModelImpl implements Model, Serializable {

    private final List<String> clients = new ArrayList<>();
    private final Map<String, MousePosition> clientsMouse = new HashMap<>();
    private final Map<String, Integer> clientsColor = new HashMap<>();

    private PixelGrid grid = new PixelGrid(40, 40);

    @Override
    public List<String> getPlayers() {
        return Collections.unmodifiableList(clients);
    }

    @Override
    public Map<String, MousePosition> getPlayersMouse() {
        return Collections.unmodifiableMap(clientsMouse);
    }

    @Override
    public Map<String, Integer> getPlayersColor() {
        return Collections.unmodifiableMap(clientsColor);
    }

    @Override
    public PixelGrid getGrid() {
        return grid;
    }

    @Override
    public void setGrid(PixelGrid grid) {
        for (int x = 0; x < grid.getNumColumns(); x++) {
            for (int y = 0; y < grid.getNumRows(); y++) {
                this.grid.set(x, y, grid.get(x, y));
            }
        }
    }

    @Override
    public void addClient(String clientId, int color) {
        if (!clients.contains(clientId)) {
            clients.add(clientId);
            clientsColor.put(clientId, color);
            clientsMouse.put(clientId, new MousePosition(clientId, 0, 0));
        }
    }

    @Override
    public void removeClient(String clientId) {
        clients.remove(clientId);
        clientsColor.remove(clientId);
    }

    @Override
    public void updateMousePosition(String clientId, int x, int y) {
        clientsMouse.put(clientId, new MousePosition(clientId, x, y));
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        grid.set(x, y, color);
    }

    @Override
    public void copyFrom(Model model) {
        clients.clear();
        clients.addAll(model.getPlayers());
        clientsMouse.clear();
        clientsMouse.putAll(model.getPlayersMouse());
        clientsColor.clear();
        clientsColor.putAll(model.getPlayersColor());
        this.setGrid(model.getGrid());
    }

    @Override
    public void updateUserColor(String clientId, int color) {
        clientsColor.put(clientId, color);
    }
}
