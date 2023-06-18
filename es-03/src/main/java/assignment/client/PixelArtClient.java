package assignment.client;

import assignment.session.Session;
import assignment.session.PixelArtSession;
import assignment.model.Model;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;

import static java.rmi.registry.LocateRegistry.getRegistry;

public class PixelArtClient implements Client, RemoteClient {
    private final Registry registry;
    private final String id;
    private final Model model;
    private Session session;

    public PixelArtClient(final Model model) {
        this.id = UUID.randomUUID().toString();
        this.model = model;
        try {
            registry = getRegistry();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public void join(String sessionId) {
        try {
            // Register client
            RemoteClient stub = (RemoteClient) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(this.getID(), stub);

            // Register client with server
            session = (Session) registry.lookup(sessionId);
            session.registerClient(this.getID());
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String create() {
        // Create and register the real server instance
        final String sessionId = UUID.randomUUID().toString();
        final Session realSession = new PixelArtSession(sessionId);
        try {
            Session realSessionStub = (Session) UnicastRemoteObject.exportObject(realSession, 0);
            registry.rebind(sessionId, realSessionStub);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        join(sessionId);
        return sessionId;
    }

    @Override
    public void leave() {
        try {
            this.session.unregisterClient(this.getID());
            this.registry.unbind(this.getID());
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMousePosition(int x, int y) {
        try {
            this.session.updateMousePosition(this.getID(), x, y);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updatePixel(int x, int y, int color) {
        try {
            this.session.updatePixel(x, y, color);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onGrid(int[][] grid) throws RemoteException {
        this.log("Received grid");
        this.model.setGrid(grid);
    }

    @Override
    public void onNewClient(String clientId) throws RemoteException {
        this.log("Received new client: " + clientId);
        this.model.addClient(clientId);
    }

    @Override
    public void onClientLeft(String clientId) throws RemoteException {
        this.log("Received client left: " + clientId);
        this.model.removeClient(clientId);
    }

    @Override
    public void onNewPosition(String clientId, int x, int y) throws RemoteException {
        this.log("Received new position for client: " + clientId);
        this.model.updateMousePosition(clientId, x, y);
    }

    @Override
    public void onPixelUpdated(int x, int y, int color) throws RemoteException {
        this.log("Received pixel update: " + x + ", " + y + ", " + color);
        this.model.updatePixel(x, y, color);
    }

    private void log(String message) {
        System.out.println("[" + this.id + "] " + message);
    }
}
