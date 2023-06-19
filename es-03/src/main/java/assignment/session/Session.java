package assignment.session;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Session extends Remote {

    String getId() throws RemoteException;

    void registerClient(String clientId) throws RemoteException;

    void unregisterClient(String clientId) throws RemoteException;

    void updateMousePosition(String clientId, int x, int y) throws RemoteException;

    void updatePixel(int x, int y, int color) throws RemoteException;

}
