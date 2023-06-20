package assignment.pixelGrid.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;

import java.util.concurrent.TimeoutException;

public class SessionImpl implements Session {
    private static final String NEW_BRUSH_POSITION_EXCHANGE_POSTFIX = "NewBrushPosition";
    private static final String NEW_PIXEL_POSITION_EXCHANGE_POSTFIX = "NewPixelUpdate";
    private static final String USER_DISCONNECTION_EXCHANGE_POSTFIX = "UserDisconnection";
    private final String sessionId;
    private String newBrushPositionExch;
    private String userId;
    private String brushPositionUpdate;
    private String pixelUpdate;
    private String userDisconnectUpdate;
    private Channel channel;
    private Connection connection;
    private int delayTicks = 0;

    public SessionImpl(String uuid, String sessionId) {
        this.userId = uuid;
        this.sessionId = sessionId;
    }

    public void setUpConnection() {
        System.out.println("Setting up connection");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
            System.out.println("declaring queues");
            this.declareQueues();
            System.out.println("defining callbacks");

        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }


    private void declareQueues() {
        this.newBrushPositionExch = this.sessionId + "_" + NEW_BRUSH_POSITION_EXCHANGE_POSTFIX;
        String newPixelUpdateExch = this.sessionId + "_" + NEW_PIXEL_POSITION_EXCHANGE_POSTFIX;
        String userDisconnectionExch = this.sessionId + "_" + USER_DISCONNECTION_EXCHANGE_POSTFIX;
        System.out.println("--> Declaring exchanges:\n\t" + this.newBrushPositionExch + "\n\t" + newPixelUpdateExch + "\n\t" + userDisconnectionExch);
        try {
            channel.exchangeDeclare(newPixelUpdateExch, "fanout");
            this.pixelUpdate = channel.queueDeclare().getQueue();
            channel.queueBind(this.pixelUpdate, newPixelUpdateExch, "");

            channel.exchangeDeclare(this.newBrushPositionExch, "fanout");
            this.brushPositionUpdate = channel.queueDeclare().getQueue();
            channel.queueBind(this.brushPositionUpdate, this.newBrushPositionExch, "");

            channel.exchangeDeclare(userDisconnectionExch, "fanout");
            this.userDisconnectUpdate = channel.queueDeclare().getQueue();
            channel.queueBind(this.userDisconnectUpdate, userDisconnectionExch, "");

            System.out.println("\tDeclaring SessionID queue");
            this.channel.queueDeclare(this.sessionId, false, false, false, null);

            System.out.println("--> Done");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public String getPixelUpdateExchName() {
        return this.sessionId + "_" + NEW_PIXEL_POSITION_EXCHANGE_POSTFIX;
    }

    public String getBrushPositionExchName() {
        return this.sessionId + "_" + NEW_BRUSH_POSITION_EXCHANGE_POSTFIX;
    }

    public String getUserDisconnectionExchName() {
        return this.sessionId + "_" + USER_DISCONNECTION_EXCHANGE_POSTFIX;
    }

    public void declareQueue(String queueName) {
        try {
            this.channel.queueDeclare(queueName, false, false, false, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void publishOnQueue(String queueName, String message) {
        try {
            this.channel.basicPublish("", queueName, null, message.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void consume(String queueName, DeliverCallback callback) {
        try {
            this.channel.basicConsume(queueName, true, callback, consumerTag -> {
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void closeConnection(String uuid) {
        try {
            this.sendUserDisconnectionToBroker(uuid);
            this.channel.close();
            this.connection.close();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public String getBrushPositionUpdateQueue() {
        return this.brushPositionUpdate;
    }

    public String getPixelUpdateQueue() {
        return this.pixelUpdate;
    }

    public String getUserDisconnectUpdateQueue() {
        return this.userDisconnectUpdate;
    }

    public void sendPixelUpdateToBroker(String id, int x, int y, int color) {
        try {
            System.out.println("Sending pixel update to broker");
            String message = id + " " + x + " " + y + " " + color;
            System.out.println("Sending message: " + message);
            channel.basicPublish(this.getPixelUpdateExchName(), "", null, message.getBytes("UTF-8"));
            System.out.println(" [*] Sent COLOR '" + message + "'");
        } catch (Exception e) {
            System.out.println("[!!]-> Error sending pixel update to broker");
            throw new RuntimeException(e);
        }
    }

    public void sendBrushPositionToBroker(String id, int x, int y, int color) {
        String message = id + " " + x + " " + y + " " + color;
        try {
            channel.basicPublish(this.getBrushPositionExchName(), "", null, message.getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(" [*] Sent POSITION '" + message + "'");
    }

    public void sendUserDisconnectionToBroker(String uuid) {
        try {
            String message = uuid.toString();
            channel.basicPublish(this.getUserDisconnectionExchName(), "", null, message.getBytes("UTF-8"));
            System.out.println(" [*] Sent DISCONNECT'" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
