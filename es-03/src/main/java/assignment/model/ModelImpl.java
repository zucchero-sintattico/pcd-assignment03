package assignment.model;

import assignment.utils.Position;

import java.io.Serializable;
import java.util.*;

public class ModelImpl implements Model, Serializable {

    private final List<String> clients = new ArrayList<>();
    private final Map<String, Position> clientsMouse = new HashMap<>();
    private int[][] grid = new int[100][100];

    @Override
    public List<String> getPlayers() {
        return Collections.unmodifiableList(clients);
    }

    @Override
    public int[][] getGrid() {
        return grid;
    }

    @Override
    public void setGrid(int[][] grid) {
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
        clientsMouse.put(clientId, new Position(x, y));
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        grid[x][y] = color;
    }
}
