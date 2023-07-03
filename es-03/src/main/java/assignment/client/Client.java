package assignment.client;

import assignment.model.Model;

public interface Client {

    String getID();

    Model getModel();

    void join(String sessionId, int color);

    String create(String sessionId);

    void leave();

    void updateUserColor(int color);

    void updateMousePosition(int x, int y);

    void updatePixel(int x, int y, int color);

}
