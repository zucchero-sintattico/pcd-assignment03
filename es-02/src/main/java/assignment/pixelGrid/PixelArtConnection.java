package assignment.pixelGrid;

import assignment.pixelGrid.view.PixelGrid;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PixelArtConnection implements Controller{
    private static final String NEW_BRUSH_POSITION_EXCHANGE_POSTFIX = "NewBrushPosition";
    private static final String NEW_PIXEL_POSITION_EXCHANGE_POSTFIX = "NewPixelUpdate";
    private static final String USER_DISCONNECTION_EXCHANGE_POSTFIX = "UserDisconnection";

    private String newBrushPositionExch;
    private String sessionId;
    private String userId;
    private String newBrushPositionQueueName;
    private String newPixelUpdateQueueName;
    private String userDisconnectionQueueName;
    private Channel channel;
    private Connection connection;
    private int delayTicks = 0;
    private final PixelArtNode model;
    private Boolean isSync = false;
    private final List<PixelInfo> pixelInfoBuffer = new ArrayList<>();

    public PixelArtConnection(PixelArtNode node) {
        this.model = node;
    }

    public void setUpConnection(String sessionId, String userId){
        this.userId = userId;
        this.sessionId = sessionId;
        System.out.println("Setting up connection");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            System.out.println("declaring queues");
            this.declareQueues();
            System.out.println("defining callbacks");
            this.defineCallbacks();

            // If it's a join, send a message to the server
            this.obtainGridState();
            this.defineSendGridCallback();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    private void obtainGridState() {
        if(!this.model.newSession){
            System.out.println("[JOIN] - waiting for session grid");
            this.waitSessionGrid();
        }
    }

    private void defineSendGridCallback() throws IOException {
        System.out.println("Enabling sending grid");
        // Start to listen to new requests
        this.channel.basicConsume(this.sessionId, true, (c, d) ->{
            System.out.println("[ inSYNC NODE ] - Sending grid");
            String userId = new String(d.getBody(), "UTF-8");
            this.channel.queueDeclare(userId, false, false, false, null);
            this.channel.basicPublish("", userId, null, this.model.getGrid().toString().getBytes("UTF-8"));
        }, consumerTag1 -> {});


    }

    private void waitSessionGrid() {
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
                // Refresh the view with the new grid
                this.model.getView().setGrid(this.model.getGrid());
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        };
        try {
            // a new queue is created for the actual user
            this.channel.basicPublish("", this.sessionId, null, this.userId.getBytes("UTF-8"));
            System.out.println("\t-->Defining queues & callbacks for session grid");
            // the user sends a message to the server, so he can receive the grid
            this.getGridState(onGrid);
            System.out.println("\t-->Done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void getGridState(DeliverCallback onGrid) throws IOException {
        this.channel.queueDeclare(this.userId, false, false, false, null);
        // the user is subscribed to the queue, so he can receive the grid
        this.channel.basicConsume(this.userId, true, onGrid, consumerTag -> {});
    }

    private void declareQueues() throws IOException {
        this.newBrushPositionExch = this.sessionId +"_"+ NEW_BRUSH_POSITION_EXCHANGE_POSTFIX;
        String newPixelUpdateExch = this.sessionId + "_" + NEW_PIXEL_POSITION_EXCHANGE_POSTFIX;
        String userDisconnectionExch = this.sessionId + "_" + USER_DISCONNECTION_EXCHANGE_POSTFIX;
        System.out.println("--> Declaring exchanges:\n\t" + this.newBrushPositionExch + "\n\t" + newPixelUpdateExch + "\n\t" + userDisconnectionExch);
        try {
            channel.exchangeDeclare(newPixelUpdateExch, "fanout");
            this.newPixelUpdateQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.newPixelUpdateQueueName, newPixelUpdateExch, "");

            channel.exchangeDeclare(this.newBrushPositionExch, "fanout");
            this.newBrushPositionQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.newBrushPositionQueueName, this.newBrushPositionExch, "");

            channel.exchangeDeclare(userDisconnectionExch, "fanout");
            this.userDisconnectionQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.userDisconnectionQueueName, userDisconnectionExch, "");

            System.out.println("\tDeclaring SessionID queue");
            this.channel.queueDeclare(this.sessionId, false, false, false, null);

            System.out.println("--> Done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection() {
        this.sendUserDisconnectionToBroker(UUID.fromString(this.userId));
        try {
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     *  Subscriber Methods
     */

    private void defineCallbacks() throws IOException {
        this.defineNewBrushPositionCallback();
        this.definePixelUpdateCallback();
        this.defineUserDisconnectedCallback();
    }

    private void definePixelUpdateCallback() {
        DeliverCallback newColorCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received C '" + message);
            String[] parts = message.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int color = Integer.parseInt(parts[3]);
            this.model.onPixelUpdate(x, y, color);
            System.out.println("New-Color: "+ this.model.getGrid().get(x, y));
            this.model.getView().refresh();
        };
        try {
            this.channel.basicConsume(this.newPixelUpdateQueueName, true, newColorCallback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void defineNewBrushPositionCallback() throws IOException {
        DeliverCallback newBrushPositionCallback = (consumerTag, delivery) -> {
            // while not inSync, add messages to buffer
            if (!isSync && !this.model.newSession) {
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
            UUID brushId = UUID.fromString(parts[0]);
            int newX = Integer.parseInt(parts[1]);
            int newY = Integer.parseInt(parts[2]);
            int newColor = Integer.parseInt(parts[3]);
            this.model.onBrushPosition(brushId, newX, newY, newColor);
            this.model.getView().refresh();
        };

        this.channel.basicConsume(this.newBrushPositionQueueName, true, newBrushPositionCallback, consumerTag -> {});
    }

    private void defineUserDisconnectedCallback() throws IOException {
        DeliverCallback disconnectCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" -Disconnected '" + message);
            String[] parts = message.split(" ");
            // Message: brushId
            UUID uuid = UUID.fromString(parts[0]);
            this.model.onDisconnect(uuid);
        };
        this.channel.basicConsume(this.userDisconnectionQueueName, true, disconnectCallback, consumerTag -> {});
    }

    /*
     * Publisher methods
     */
    public void sendPixelUpdateToBroker(UUID id, int x, int y, int color) {
        try {
            System.out.println("Sending pixel update to broker");
            String message = id + " " + x + " " + y + " " + color;
            System.out.println("Sending message: "+message);
            channel.basicPublish(this.sessionId+"_"+NEW_PIXEL_POSITION_EXCHANGE_POSTFIX, "", null, message.getBytes("UTF-8"));
            System.out.println(" [*] Sent COLOR '" + message + "'");
        } catch (Exception e) {
            System.out.println("[!!]-> Error sending pixel update to broker");
            throw new RuntimeException(e);
        }
    }

    public void sendBrushPositionToBroker(UUID id, int x, int y, int color) {
        delayTicks++;
        delayTicks %= 25;
        try {
            if (delayTicks == 0) {
                String message = id + " " + x + " " + y + " " + color;
                channel.basicPublish(newBrushPositionExch, "", null, message.getBytes("UTF-8"));
                System.out.println(" [*] Sent POSITION '" + message + "'");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserDisconnectionToBroker(UUID uuid) {
        try {
            String message = uuid.toString();
            channel.basicPublish(this.sessionId+"_"+USER_DISCONNECTION_EXCHANGE_POSTFIX, "", null, message.getBytes("UTF-8"));
            System.out.println(" [*] Sent DISCONNECT'" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
