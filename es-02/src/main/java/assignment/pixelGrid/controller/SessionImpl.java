package assignment.pixelGrid.controller;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

import java.util.concurrent.TimeoutException;

public class SessionImpl {
    private static final String NEW_BRUSH_POSITION_EXCHANGE_POSTFIX = "NewBrushPosition";
    private static final String NEW_PIXEL_POSITION_EXCHANGE_POSTFIX = "NewPixelUpdate";
    private static final String USER_DISCONNECTION_EXCHANGE_POSTFIX = "UserDisconnection";
    private final String sessionId;
    private String newBrushPositionExch;
    private String userId;
    private String newBrushPositionQueueName;
    private String newPixelUpdateQueueName;
    private String userDisconnectionQueueName;
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

    public Channel getChannel() {
        return this.channel;
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
}
