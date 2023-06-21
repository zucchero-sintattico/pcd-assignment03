package assignment.pixelGrid.controller;

import assignment.pixelGrid.PixelInfo;
import assignment.pixelGrid.model.ModelImpl;
import assignment.pixelGrid.model.ObservableModel;
import assignment.pixelGrid.model.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;
import com.rabbitmq.client.DeliverCallback;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class ControllerImpl implements Controller {

    private final ObservableModel model;
    private final PixelGridView view;
    private Boolean isSync = false;
    private final Boolean isNewConnection;
    private final List<PixelInfo> eventBuffer = new ArrayList<>();
    private final SessionImpl session;

    public ControllerImpl(Boolean isNewConnection, String sessionId) {
        this.model = new ModelImpl();
        this.isNewConnection = isNewConnection;
        this.session = new SessionImpl(this.model.getUuid(), sessionId);
        this.view = new PixelGridView(this.model.getGrid(), this.model.getBrushManager(), 800, 800);
        this.view.display();
    }

    @Override
    public void start() {
        this.setModelListener();
        this.setViewListeners();
        this.session.setUpConnection();
        this.defineCallbacks();
        if (this.isNewConnection) {
            this.defineOnGridRequestCallback();
        } else {
            this.waitGridState();
        }
    }

    private void setViewListeners() {
        this.view.addMouseMovedListener((x, y) -> {
            // Send update to broker
            this.session.sendBrushPositionToBroker(this.model.getUuid(), x, y, this.model.getBrushManager().getBrushMap().get(this.model.getUuid()).getColor());
        });

        this.view.addPixelGridEventListener((x, y) -> {
            System.out.println("---> sending color to broker");
            // Send update to broker
            this.session.sendPixelUpdateToBroker(this.model.getUuid(), x, y, this.model.getBrushManager().getBrushMap().get(this.model.getUuid()).getColor());

        });

        // add listener for closing the window
        this.view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                session.closeConnection();
                super.windowClosing(e);
            }

        });
        this.view.addColorChangedListener((c) -> {
            this.model.getBrushManager().getBrushMap().get(this.model.getUuid()).setColor(c);
        });
    }

    private void setModelListener() {
        this.model.setDisconnectEventListener((s) -> {
            this.view.getPanel().setBrushManager(this.model.getBrushManager());
            this.view.refresh();
        });
        this.model.setBrushManagerEventListener((b) -> {
            this.view.getPanel().setBrushManager(this.model.getBrushManager());
            this.view.refresh();
        });
        this.model.setPixelGridEventListener((p) -> {
            this.view.setGrid(this.model.getGrid());
            this.view.refresh();
        });
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
                System.out.println("Buffering message");
                String message = new String(delivery.getBody(), "UTF-8");
                String[] parts = message.split(" ");
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int color = Integer.parseInt(parts[3]);
                eventBuffer.add(new PixelInfo(x, y, color));
                return;
            }

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

        this.session.consume(this.session.getBrushPositionUpdateQueue(), newBrushPositionCallback);

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
            System.out.println("New-Color: " + this.model.getGrid().get(x, y));
        };
        this.session.consume(this.session.getPixelUpdateQueue(), newColorCallback);

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
        this.session.consume(this.session.getUserDisconnectUpdateQueue(), disconnectCallback);
    }

    private void waitGridState() {
        DeliverCallback onGridReceived = (consumerTag, delivery) -> {
            String gridState = new String(delivery.getBody(), "UTF-8");
            try {
                // Get the grid from the server
                System.out.println("Got session grid");
                this.model.setGrid(PixelGrid.createFromString(gridState));
                // Apply the incoming events
                this.eventBuffer.forEach(pixelInfo -> this.model.getGrid().set(pixelInfo.getX(), pixelInfo.getY(), pixelInfo.getColor()));
                this.isSync = true;
                this.defineOnGridRequestCallback();
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        };

        // a new queue is created for the actual user
        System.out.println("Asking for session grid");
        System.out.println("\t-->Defining queues & callbacks for session grid");
        // the user sends a message to the server, so he can receive the grid
        this.session.declareQueue(this.model.getUuid());
        this.session.consume(this.model.getUuid(), onGridReceived);
        this.session.publishOnQueue(this.session.getSessionId(), this.model.getUuid());
        System.out.println("\t-->Done");

    }

    private void defineOnGridRequestCallback() {
        System.out.println("Enabling sending grid");
        // Start to listen to new requests
        DeliverCallback onGridRequest = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received GRID REQUEST '" + message);
            // Message: userId
            String userId = message;
            this.session.declareQueue(userId);
            this.session.publishOnQueue(userId, this.model.getGrid().toString());
            System.out.println(" [x] Sent GRID '" + this.model.getGrid().toString());
        };
        this.session.consume(this.session.getSessionId(), onGridRequest);

    }
}
