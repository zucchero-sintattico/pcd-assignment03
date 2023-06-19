package assignment.controller;

import assignment.utils.MousePosition;
import assignment.utils.PixelGrid;
import assignment.view.View;

import java.util.Map;

public interface Controller {

    void setView(View view);

    String getClientId();

    PixelGrid getGrid();

    void join(String sessionId);

    String create();

    void leave();

    void updateMousePosition(int x, int y);

    void updatePixel(int x, int y, int color);

    int getUserColor();

    Map<String, Integer> getPlayersColor();

    Map<String, MousePosition> getPlayersMouse();

    void updateUserColor(int rgb);
}
