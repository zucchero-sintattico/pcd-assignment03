package assignment.client;

import assignment.model.Model;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Client interface for the RMI server.
 */

public interface RemoteClient extends Remote {
    void onModel(Model model) throws RemoteException;

    void onNewClient(String clientId, int color) throws RemoteException;

    void onClientLeft(String clientId) throws RemoteException;

    void onUserColorChange(String clientId, int color) throws RemoteException;

    void onNewPosition(String clientId, int x, int y) throws RemoteException;

    void onPixelUpdated(int x, int y, int color) throws RemoteException;
}
