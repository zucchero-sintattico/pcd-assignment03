package assignment.client;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Client interface for the RMI server.
 */

public interface RemoteClient extends Remote {
    void onGrid(int[][] grid) throws RemoteException;

    void onNewClient(String clientId) throws RemoteException;

    void onClientLeft(String clientId) throws RemoteException;

    void onNewPosition(String clientId, int x, int y) throws RemoteException;

    void onPixelUpdated(int x, int y, int color) throws RemoteException;
}
