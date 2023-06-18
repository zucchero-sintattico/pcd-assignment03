package assignment.session;

import assignment.client.RemoteClient;
import assignment.model.Model;
import assignment.model.ModelImpl;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.Map;

import static java.rmi.registry.LocateRegistry.getRegistry;

public class PixelArtSession implements Session {

    private final String sessionId;
    private final Model model = new ModelImpl();
    private final Registry registry;
    private final Map<String, RemoteClient> clients = new HashMap<>();

    public PixelArtSession(String sessionId) {
        try {
            registry = getRegistry();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        this.sessionId = sessionId;
    }

    private void log(String message) {
        System.out.println("[Server] " + message);
    }

    @Override
    public String getId() throws RemoteException {
        return sessionId;
    }

    @Override
    public synchronized void registerClient(String clientId) throws RemoteException {
        this.log("Registering new client: " + clientId);
        try {
            RemoteClient remoteClient = (RemoteClient) registry.lookup(clientId);
            clients.values().forEach(x -> {
                try {
                    x.onNewClient(clientId);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            });
            clients.put(clientId, remoteClient);
        } catch (NotBoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public synchronized void unregisterClient(String clientId) throws RemoteException {
        this.log("Unregistering client: " + clientId);
        clients.remove(clientId);
        clients.values().forEach(x -> {
            try {
                x.onClientLeft(clientId);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public synchronized void updateMousePosition(String clientId, int x, int y) throws RemoteException {
        this.log("Updating mouse position for client: " + clientId);
        clients.values().forEach(client -> {
            try {
                client.onNewPosition(clientId, x, y);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public synchronized void updatePixel(int x, int y, int color) throws RemoteException {
        this.log("Updating pixel at (" + x + ", " + y + ") to color " + color);
        clients.values().forEach(client -> {
            try {
                client.onPixelUpdated(x, y, color);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

}
