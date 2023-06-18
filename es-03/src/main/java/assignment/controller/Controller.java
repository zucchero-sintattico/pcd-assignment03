package assignment.controller;

import assignment.view.View;

public interface Controller {

    void setView(View view);

    void join(String sessionId);

    String create();

    void leave();

    void updateMousePosition(int x, int y);

    void updatePixel(int x, int y, int color);

}
