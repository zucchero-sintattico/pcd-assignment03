package assignment.client;

import assignment.model.Model;

public interface Client {

    String getID();

    Model getModel();

    void join(String sessionId);

    String create();

    void leave();

    void updateMousePosition(int x, int y);

    void updatePixel(int x, int y, int color);

}
