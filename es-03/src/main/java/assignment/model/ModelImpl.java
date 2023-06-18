package assignment.model;

import assignment.utils.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelImpl implements Model {

    private int[][] grid = new int[100][100];
    private final List<String> clients = new ArrayList<>();
    private final Map<String, Position> clientMap = new HashMap<>();

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
        clientMap.put(clientId, new Position(x, y));
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        grid[x][y] = color;
    }
}
