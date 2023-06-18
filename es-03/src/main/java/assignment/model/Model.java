package assignment.model;

import java.util.List;

public interface Model {

    List<String> getPlayers();

    int[][] getGrid();

    void setGrid(int[][] grid);

    void addClient(String clientId);

    void removeClient(String clientId);

    void updateMousePosition(String clientId, int x, int y);

    void updatePixel(int x, int y, int color);
}
