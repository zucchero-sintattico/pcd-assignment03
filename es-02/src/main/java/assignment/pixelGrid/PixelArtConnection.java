package assignment.pixelGrid;

import assignment.pixelGrid.view.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PixelArtConnection {
    private static final String NEW_BRUSH_POSITION_EXCHANGE_POSTFIX = "NewBrushPosition";
    private static final String NEW_PIXEL_POSITION_EXCHANGE_POSTFIX = "NewPixelUpdate";
    private static final String USER_DISCONNECTION_EXCHANGE_POSTFIX = "UserDisconnection";

    private String newBrushPositionExch;
    private String newPixelUpdateExch;
    private String userDisconnectionExch;
    private String sessionId;
    private String userId;
    private String newBrushPositionQueueName;
    private String newPixelUpdateQueueName;
    private String userDisconnectionQueueName;
    private Channel channel;
    private Connection connection;
    private int delayTicks = 0;
    private final PixelArtNode node;
    private Boolean isSync = false;
    private final List<PixelInfo> pixelInfoBuffer = new ArrayList<>();

    public PixelArtConnection(PixelArtNode node) {
        this.node = node;
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
            if(!this.node.newSession){
                System.out.println("[JOIN] - waiting for session grid");
                this.waitSessionGrid();
            }
            this.defineSendGridCallback();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    private void defineSendGridCallback() throws IOException {
        System.out.println("Enabling sending grid");
        // Start to listen to new requests
        this.channel.basicConsume(this.sessionId, true, (c, d) ->{
            System.out.println("[ inSYNC NODE ] - Sending grid");
            String userId = new String(d.getBody(), "UTF-8");
            this.channel.queueDeclare(userId, false, false, false, null);
            this.channel.basicPublish("", userId, null, this.node.getGrid().toString().getBytes("UTF-8"));
        }, consumerTag1 -> {});


    }

    private void waitSessionGrid() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String gridState = new String(delivery.getBody(), "UTF-8");
            try {
                // Get the grid from the server
                System.out.println("Got session grid");
                System.out.println(gridState);
                this.node.setGrid(PixelGrid.createFromString(gridState));

                // Apply the incoming events
                this.pixelInfoBuffer.forEach(pixelInfo -> this.node.getGrid().set(pixelInfo.getX(), pixelInfo.getY(), pixelInfo.getColor()));
                this.isSync = true;

                // Refresh the view with the new grid
                this.node.getView().setGrid(this.node.getGrid());


            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        };
        try {
            this.channel.basicPublish("", this.sessionId, null, this.userId.getBytes("UTF-8"));

            System.out.println("\t-->Defining queues & callbacks for session grid");
            // a new queue is created for the actual user
            this.channel.queueDeclare(this.userId, false, false, false, null);
            // the user is subscribed to the queue, so he can receive the grid
            this.channel.basicConsume(this.userId, true, deliverCallback, consumerTag -> {});
            // the user sends a message to the server, so he can receive the grid

            //this.channel.basicPublish("", this.sessionId, null, this.userId.getBytes("UTF-8"));
            System.out.println("\t-->Done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void declareQueues() throws IOException {
        this.newBrushPositionExch = this.sessionId +"_"+ NEW_BRUSH_POSITION_EXCHANGE_POSTFIX;
        this.newPixelUpdateExch = this.sessionId +"_"+ NEW_PIXEL_POSITION_EXCHANGE_POSTFIX;
        this.userDisconnectionExch = this.sessionId +"_"+ USER_DISCONNECTION_EXCHANGE_POSTFIX;

        System.out.println("--> Declaring exchanges:\n\t" + this.newBrushPositionExch + "\n\t" + this.newPixelUpdateExch + "\n\t" + this.userDisconnectionExch);
        try {
            channel.exchangeDeclare(this.newPixelUpdateExch, "fanout");
            this.newPixelUpdateQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.newPixelUpdateQueueName, this.newPixelUpdateExch, "");

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

    public void closeConnection() throws IOException, TimeoutException {
        this.channel.close();
        this.connection.close();
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
            this.node.getGrid().set(x, y, color);
            System.out.println("New-Color: "+ this.node.getGrid().get(x, y));
            this.node.getView().refresh();
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
            if (!isSync && !this.node.newSession) {
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
            var brush = this.node.getBrushManager().getBrushMap().keySet().stream().filter(b -> b.equals(brushId)).findFirst();
            brush.ifPresentOrElse(b -> {
                        this.node.getBrushManager().getBrushMap().get(brushId).updatePosition(newX, newY);
                        this.node.getBrushManager().getBrushMap().get(brushId).setColor(newColor);
                    },
                    () -> { var newBrush = new BrushManager.Brush(newX, newY, newColor);
                        this.node.getBrushManager().getBrushMap().put(brushId, newBrush);
                    });
        };

        this.channel.basicConsume(this.newBrushPositionQueueName, true, newBrushPositionCallback, consumerTag -> {});
    }

    private void defineUserDisconnectedCallback() throws IOException {
        DeliverCallback disconnectCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" -Disconnected '" + message);
            String[] parts = message.split(" ");
            // Message: brushId
            UUID brushId = UUID.fromString(parts[0]);
            this.node.getBrushManager().getBrushMap().remove(brushId);
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

    public void sendNewBrushPositionToBroker(UUID id, int x, int y, int color) {
        delayTicks++;
        delayTicks %= 50;
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
