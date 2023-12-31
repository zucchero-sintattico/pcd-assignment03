package assignment.client;

import assignment.model.Model;
import assignment.model.ModelImpl;
import assignment.session.PixelArtSession;
import assignment.session.Session;
import assignment.utils.MousePosition;
import assignment.utils.PixelInfo;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static java.rmi.registry.LocateRegistry.getRegistry;

public class PixelArtClient implements ObservableClient, RemoteClient {
    private final Registry registry;
    private final String id;
    private Model model = new ModelImpl();
    private Session session;

    private Consumer<Model> modelHandler = x -> {
    };
    private Consumer<Map.Entry<String, Integer>> newClientHandler = x -> {
    };
    private Consumer<Map.Entry<String, Integer>> clientColorUpdateHandler = x -> {
    };
    private Consumer<String> clientLeftHandler = x -> {
    };
    private Consumer<MousePosition> mousePositionHandler = x -> {
    };
    private Consumer<PixelInfo> pixelInfoHandler = x -> {
    };


    public PixelArtClient() {
        this.id = UUID.randomUUID().toString();
        try {
            registry = getRegistry();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(String message) {
        System.out.println("[" + this.id + "] " + message);
    }

    @Override
    public String getID() {
        return this.id;
    }

    @Override
    public Model getModel() {
        return this.model;
    }

    @Override
    public void join(String sessionId, int color) {
        try {
            // Register client
            final RemoteClient stub = (RemoteClient) UnicastRemoteObject.exportObject(this, 0);
            registry.rebind(this.getID(), stub);

            // Register client with server
            session = (Session) registry.lookup(sessionId);
            session.registerClient(this.getID(), color);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String create(String sessionId) {
        // Create and register the real server instance
        final Session realSession = new PixelArtSession(sessionId);
        try {
            final Session realSessionStub = (Session) UnicastRemoteObject.exportObject(realSession, 0);
            registry.rebind(sessionId, realSessionStub);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
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
    public void updateUserColor(int color) {
        try {
            this.session.updateUserColor(this.getID(), color);
        } catch (RemoteException e) {
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

    // RemoteClient implementation

    @Override
    public void onModel(Model model) throws RemoteException {
        this.log("Received model");
        this.model.copyFrom(model);
        this.modelHandler.accept(model);
    }

    @Override
    public void onNewClient(String clientId, int color) throws RemoteException {
        this.log("Received new client: " + clientId);
        this.model.addClient(clientId, color);
        this.newClientHandler.accept(Map.entry(clientId, color));
    }

    @Override
    public void onClientLeft(String clientId) throws RemoteException {
        this.log("Received client left: " + clientId);
        this.model.removeClient(clientId);
        this.clientLeftHandler.accept(clientId);
    }

    @Override
    public void onUserColorChange(String clientId, int color) throws RemoteException {
        this.log("Received user color change: " + clientId + ", " + color);
        this.model.updateUserColor(clientId, color);
        this.clientColorUpdateHandler.accept(Map.entry(clientId, color));
    }

    @Override
    public void onNewPosition(String clientId, int x, int y) throws RemoteException {
        this.log("Received new position for client: " + clientId);
        this.model.updateMousePosition(clientId, x, y);
        this.mousePositionHandler.accept(new MousePosition(clientId, x, y));
    }

    @Override
    public void onPixelUpdated(int x, int y, int color) throws RemoteException {
        this.log("Received pixel update: " + x + ", " + y + ", " + color);
        this.model.updatePixel(x, y, color);
        this.pixelInfoHandler.accept(new PixelInfo(x, y, color));
    }


    // ObservableClient implementation

    @Override
    public void setOnModelReadyListener(Consumer<Model> modelListener) {
        this.modelHandler = modelListener;
    }

    @Override
    public void setOnUserJoinListener(Consumer<Map.Entry<String, Integer>> onUserJoinListener) {
        this.newClientHandler = onUserJoinListener;
    }

    @Override
    public void setOnUserLeaveListener(Consumer<String> onUserLeaveListener) {
        this.clientLeftHandler = onUserLeaveListener;
    }

    @Override
    public void setOnUserColorUpdateListener(Consumer<Map.Entry<String, Integer>> onUserColorUpdateListener) {
        this.clientColorUpdateHandler = onUserColorUpdateListener;
    }

    @Override
    public void setOnNewMousePositionListener(Consumer<MousePosition> onNewMousePositionListener) {
        this.mousePositionHandler = onNewMousePositionListener;
    }

    @Override
    public void setOnPixelUpdatedListener(Consumer<PixelInfo> onPixelUpdatedListener) {
        this.pixelInfoHandler = onPixelUpdatedListener;
    }
}
