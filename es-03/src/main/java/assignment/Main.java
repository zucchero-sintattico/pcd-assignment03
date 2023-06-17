package assignment;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import assignment.Hello;
import assignment.Server;

import static java.rmi.registry.LocateRegistry.*;

public class Main {

    private static void serverThread() {
        final Server server = new Server();
        try {
            Hello stub = (Hello) UnicastRemoteObject.exportObject(server, 0);
            // Bind the remote object's stub in the registry
            Registry registry = getRegistry();
            registry.rebind("Hello", stub);
            System.out.println("Server ready");

        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    private static void clientThread() {
        try {
            Registry registry = getRegistry();
            Hello stubClient = (Hello) registry.lookup("Hello");
            String response = stubClient.sayHello();
            System.out.println("response: " + response);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) throws InterruptedException {

        Thread serverThread = new Thread(Main::serverThread);
        serverThread.start();
        serverThread.join();
        Thread clientThread = new Thread(Main::clientThread);
        clientThread.start();

    }
}