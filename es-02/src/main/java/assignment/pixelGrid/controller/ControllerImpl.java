package assignment.pixelGrid.controller;

import assignment.pixelGrid.PixelInfo;
import assignment.pixelGrid.model.Model;
import assignment.pixelGrid.model.ModelImpl;
import assignment.pixelGrid.model.ObservableModel;
import assignment.pixelGrid.view.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ControllerImpl implements Controller {

    private ObservableModel model;
    private PixelGridView view;
    private Boolean isSync = false;
    private Boolean isNewConnection;
    private final List<PixelInfo> pixelInfoBuffer = new ArrayList<>();
    private SessionImpl session;

    public ControllerImpl(Boolean isNewConnection, String sessionId) {
        this.model = new ModelImpl();
        this.isNewConnection = isNewConnection;
        this.session = new SessionImpl(this.model.getUuid(), sessionId);
    }

    @Override
    public void start() {
        this.setModelListener();
        this.session.setUpConnection();
        this.defineCallbacks();
        if(this.isNewConnection) {
            this.defineOnGridRequestCallback();
        } else {
            this.waitGridState();
        }
    }

    private void setModelListener() {
        this.model.setDisconnectEventListener((s) -> this.view.refresh());
        this.model.setBrushManagerEventListener((b) -> this.view.refresh());
        this.model.setPixelGridEventListener((p) -> this.view.refresh());
    }

    private void defineCallbacks() {
        this.defineNewBrushPositionCallback();
        this.definePixelUpdateCallback();
        this.defineUserDisconnectedCallback();
    }

    private void defineNewBrushPositionCallback() {
        DeliverCallback newBrushPositionCallback = (consumerTag, delivery) -> {
            // while not inSync, add messages to buffer
            if (!isSync && !this.isNewConnection) {
                String message = new String(delivery.getBody(), "UTF-8");
                String[] parts = message.split(" ");
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int color = Integer.parseInt(parts[3]);
                pixelInfoBuffer.add(new PixelInfo(x, y, color));
                return;
            }

            //
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received POSITION '" + message);
            String[] parts = message.split(" ");
            // Message: brushId x y color
            String brushId = UUID.fromString(parts[0]).toString();
            int newX = Integer.parseInt(parts[1]);
            int newY = Integer.parseInt(parts[2]);
            int newColor = Integer.parseInt(parts[3]);
            this.model.updateBrush(brushId, newX, newY, newColor);
        };

        try {
            this.session.getChannel().basicConsume(this.session.getBrushPositionExchName(), true, newBrushPositionCallback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void definePixelUpdateCallback() {
        DeliverCallback newColorCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received C '" + message);
            String[] parts = message.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int color = Integer.parseInt(parts[3]);
            this.model.updateGridPixel(x, y, color);
            System.out.println("New-Color: "+ this.model.getGrid().get(x, y));
        };
        try {
            this.session.getChannel().basicConsume(this.session.getPixelUpdateExchName(), true, newColorCallback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void defineUserDisconnectedCallback() {
        DeliverCallback disconnectCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" -Disconnected '" + message);
            String[] parts = message.split(" ");
            // Message: brushId
            UUID uuid = UUID.fromString(parts[0]);
            this.model.removeBrush(uuid.toString());
        };
        try {
            this.session.getChannel().basicConsume(this.session.getUserDisconnectionExchName(), true, disconnectCallback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void waitGridState() {
        DeliverCallback onGrid = (consumerTag, delivery) -> {
            String gridState = new String(delivery.getBody(), "UTF-8");
            try {
                // Get the grid from the server
                System.out.println("Got session grid");
                System.out.println(gridState);
                this.model.setGrid(PixelGrid.createFromString(gridState));
                // Apply the incoming events
                this.pixelInfoBuffer.forEach(pixelInfo -> this.model.getGrid().set(pixelInfo.getX(), pixelInfo.getY(), pixelInfo.getColor()));
                this.isSync = true;
                this.defineOnGridRequestCallback();
                // Refresh the view with the new grid
                this.model.setGrid(PixelGrid.createFromString(gridState));
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        };
        try {
            // a new queue is created for the actual user
            this.session.getChannel().basicPublish("", this.session.getSessionId(), null, this.model.getUuid().getBytes("UTF-8"));
            System.out.println("\t-->Defining queues & callbacks for session grid");
            // the user sends a message to the server, so he can receive the grid
            this.session.getChannel().queueDeclare(this.session.getSessionId(), false, false, false, null);
            // the user is subscribed to the queue, so he can receive the grid
            this.session.getChannel().basicConsume(this.session.getSessionId(), true, onGrid, consumerTag -> {});
            System.out.println("\t-->Done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void defineOnGridRequestCallback() {
        System.out.println("Enabling sending grid");
        // Start to listen to new requests
        try {
            this.session.getChannel().basicConsume(this.session.getSessionId(), true, (c, d) ->{
                System.out.println("[ inSYNC NODE ] - Sending grid");
                String userId = new String(d.getBody(), "UTF-8");
                this.session.getChannel().queueDeclare(userId, false, false, false, null);
                this.session.getChannel().basicPublish("", userId, null, this.model.getGrid().toString().getBytes("UTF-8"));
            }, consumerTag1 -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public void setView(PixelGridView view) {
        this.view = view;
    }

    @Override
    public void setModel(ObservableModel model) {
        this.model = model;
    }
}
