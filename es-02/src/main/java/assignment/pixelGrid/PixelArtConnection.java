package assignment.pixelGrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PixelArtConnection {
    private static final String NEW_BRUSH_POSITION_EXCHANGE_POSTFIX = "NewBrushPosition";
    private static final String NEW_PIXEL_POSITION_EXCHANGE_POSTFIX = "NewPixelUpdate";
    private static final String USER_DISCONNECTION_EXCHANGE_POSTFIX = "User Disconnection";

    private String newBrushPositionExchangeName;
    private String newPixelUpdateExchangeName;
    private String userDisconnectionExchangeName;
    private String sessionId;
    private String userId;
    private String newBrushPositionQueueName;
    private String newPixelUpdateQueueName;
    private String userDisconnectionQueueName;
    private Channel channel;
    private Connection connection;
    private int delayTicks = 0;
    private final PixelArtNode node;

    private Boolean inSync = false;

    private List<PixelInfo> pixelInfoBuffer;

    public PixelArtConnection(PixelArtNode node) {
        this.node = node;
    }

    public void setUpConnection(String sessionId, String userId){
        this.userId = userId;
        this.sessionId = sessionId;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            this.declareQueues();
            this.defineCallbacks();
            this.waitSessionGrid();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }

    }

    private void waitSessionGrid() {
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String gridState = new String(delivery.getBody(), "UTF-8");
            try {
                // Get the grid from the server
                this.node.setGrid(PixelGrid.createFromString(gridState));
                this.pixelInfoBuffer.forEach(pixelInfo -> this.node.getGrid().set(pixelInfo.getX(), pixelInfo.getY(), pixelInfo.getColor()));
                this.inSync = true;
                // Start to listen to new requests
                this.channel.queueDeclare(this.sessionId, false, false, false, null);
                this.channel.basicConsume(this.sessionId, true, (c, d) ->{
                    String userId = new String(d.getBody(), "UTF-8");
                    this.channel.queueDeclare(userId, false, false, false, null);
                    this.channel.basicPublish("", userId, null, this.node.getGrid().toString().getBytes("UTF-8"));
                }, consumerTag1 -> {});
            } catch (TimeoutException e) {
                throw new RuntimeException(e);
            }
        };
        try {
            this.channel.queueDeclare(this.userId, false, false, false, null);
            this.channel.basicConsume(this.userId, true, deliverCallback, consumerTag -> {});
            this.channel.basicPublish("", this.sessionId, null, this.userId.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private void declareQueues() throws IOException {
        this.newBrushPositionExchangeName = this.sessionId + NEW_BRUSH_POSITION_EXCHANGE_POSTFIX;
        this.newPixelUpdateExchangeName = this.sessionId + NEW_PIXEL_POSITION_EXCHANGE_POSTFIX;
        this.userDisconnectionExchangeName = this.sessionId + USER_DISCONNECTION_EXCHANGE_POSTFIX;
        try {
            channel.exchangeDeclare(this.newPixelUpdateQueueName, "fanout");
            this.newPixelUpdateQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.newPixelUpdateQueueName, this.newPixelUpdateExchangeName, "");
            channel.exchangeDeclare(this.newBrushPositionExchangeName, "fanout");
            this.newBrushPositionQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.newBrushPositionQueueName, this.newBrushPositionExchangeName, "");
            channel.exchangeDeclare(userDisconnectionExchangeName, "fanout");
            this.userDisconnectionQueueName = channel.queueDeclare().getQueue();
            channel.queueBind(this.userDisconnectionQueueName, userDisconnectionExchangeName, "");
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
        this.defineNewBrushPositionCallback(this.node.getBrushManager());
        this.definePixelUpdateCallback(this.node.getGrid(), this.node.getView());
        this.defineUserDisconnectedCallback(this.node.getBrushManager());
    }


    private void definePixelUpdateCallback(PixelGrid grid, PixelGridView view) {
        DeliverCallback newColorCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received C '" + message);
            String[] parts = message.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int color = Integer.parseInt(parts[3]);
            grid.set(x, y, color);
            System.out.println("New-Color: "+grid.get(x, y));
            view.refresh();
        };
        try {
            this.channel.basicConsume(this.newPixelUpdateQueueName, true, newColorCallback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void defineNewBrushPositionCallback(BrushManager brushManager) throws IOException {
        DeliverCallback newBrushPositionCallback = (consumerTag, delivery) -> {

            // while not inSync, add messages to buffer
            if (!inSync) {
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
            //System.out.println(" [x] Received POSITION '" + message);
            String[] parts = message.split(" ");
            // Message: brushId x y color
            UUID brushId = UUID.fromString(parts[0]);
            int newX = Integer.parseInt(parts[1]);
            int newY = Integer.parseInt(parts[2]);
            int newColor = Integer.parseInt(parts[3]);
            var brush = brushManager.getBrushMap().keySet().stream().filter(b -> b.equals(brushId)).findFirst();
            brush.ifPresentOrElse(b -> {
                                        brushManager.getBrushMap().get(brushId).updatePosition(newX, newY);
                                        brushManager.getBrushMap().get(brushId).setColor(newColor);
                    },
                    () -> { var newBrush = new BrushManager.Brush(newX, newY, newColor);
                            brushManager.getBrushMap().put(brushId, newBrush);
                    });
        };

        this.channel.basicConsume(this.newBrushPositionQueueName, true, newBrushPositionCallback, consumerTag -> {});
    }

    private void defineUserDisconnectedCallback(BrushManager brushManager) throws IOException {
        DeliverCallback disconnectCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" -Disconnected '" + message);
            String[] parts = message.split(" ");
            // Message: brushId
            UUID brushId = UUID.fromString(parts[0]);
            brushManager.getBrushMap().remove(brushId);
        };
        this.channel.basicConsume(this.userDisconnectionQueueName, true, disconnectCallback, consumerTag -> {});
    }

    /*
     * Publisher methods
     */
    public void sendPixelUpdateToBroker(UUID id, int x, int y, int color) {
        try {
            String message = id + " " + x + " " + y + " " + color;
            channel.basicPublish(NEW_PIXEL_POSITION_EXCHANGE_POSTFIX, "", null, message.getBytes("UTF-8"));
            System.out.println(" [*] Sent COLOR '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNewBrushPositionToBroker(UUID id, int x, int y, int color) {
        delayTicks++;
        delayTicks %= 50;
        try {
            if (delayTicks == 0) {
                String message = id + " " + x + " " + y + " " + color;
                channel.basicPublish(NEW_BRUSH_POSITION_EXCHANGE_POSTFIX, "", null, message.getBytes("UTF-8"));
                System.out.println(" [*] Sent POSITION '" + message + "'");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUserDisconnectionToBroker(UUID uuid) {
        try {
            String message = uuid.toString();
            channel.basicPublish(USER_DISCONNECTION_EXCHANGE_POSTFIX, "", null, message.getBytes("UTF-8"));
            System.out.println(" [*] Sent DISCONNECT'" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void defineBufferSyncCallback(List[] buffer) {
        // consume from buffer list and apply to grid



    }
}
