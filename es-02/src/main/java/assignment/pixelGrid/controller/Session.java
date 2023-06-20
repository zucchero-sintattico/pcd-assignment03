package assignment.pixelGrid.controller;

import com.rabbitmq.client.DeliverCallback;

public interface Session {
    void setUpConnection();

    void closeConnection(String uuid);

    String getSessionId();

    String getPixelUpdateExchName();

    String getBrushPositionExchName();

    String getUserDisconnectionExchName();

    String getBrushPositionUpdateQueue();

    String getPixelUpdateQueue();

    String getUserDisconnectUpdateQueue();

    void declareQueue(String queueName);

    void publishOnQueue(String queueName, String message);

    void consume(String queueName, DeliverCallback callback);

    void sendUserDisconnectionToBroker(String uuid);

    void sendBrushPositionToBroker(String uuid, int x, int y, int color);

    void sendPixelUpdateToBroker(String id, int x, int y, int color);


}
