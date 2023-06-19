package assignment.model;

import assignment.utils.MousePosition;
import assignment.utils.PixelGrid;

import java.util.List;
import java.util.Map;

public interface Model {

    List<String> getPlayers();

    Map<String, MousePosition> getPlayersMouse();

    PixelGrid getGrid();

    void setGrid(PixelGrid grid);

    void addClient(String clientId);

    void removeClient(String clientId);

    void updateMousePosition(String clientId, int x, int y);

    void updatePixel(int x, int y, int color);
}
