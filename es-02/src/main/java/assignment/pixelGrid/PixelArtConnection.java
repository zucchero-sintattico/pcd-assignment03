package assignment.pixelGrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class PixelArtConnection {
    private static final String QUEUE_NAME = "Test";
    private Channel channel;
    private Connection connection;
    private int delayTicks = 0;

    public void sendNewColorToBroker(int x, int y, int color) {
        try {
            channel.queueDeclare("Pixels", false, false, false, null);
            String message = "Grid[" + y +"][" + x + "]: " + color;
            channel.basicPublish("", "Pixels", null, message.getBytes());
            System.out.println(" [*] Sent '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNewPositionToBroker(int x, int y) {
        delayTicks++;
        delayTicks %= 50;
        try {
            if (delayTicks == 0) {
                channel.queueDeclare("Positions", false, false, false, null);
                String message = "x = " + x + " y = " + y;
                channel.basicPublish("", "Positions", null, message.getBytes());
                System.out.println(" [*] Sent '" + message + "'");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setUpConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received A '" + message + "' by thread "+Thread.currentThread().getName());
            try {
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        };

        channel.basicConsume(QUEUE_NAME, true, deliverCallback1, consumerTag -> {});
    }
}
