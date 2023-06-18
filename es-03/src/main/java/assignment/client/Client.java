package assignment.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Client {

    String getID();
    void join(String sessionId);
    String create();
    void leave();
    void updateMousePosition(int x, int y);
    void updatePixel(int x, int y, int color);

}
