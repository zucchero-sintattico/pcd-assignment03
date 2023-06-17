package assignment.pixelGrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class PixelArtConnection {
    private Channel channel;
    private Connection connection;
    private int delayTicks = 0;

    public void setUpConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        channel.queueDeclare("NewPosition", false, false, false, null);
        channel.queueDeclare("NewColor", false, false, false, null);
        channel.queueDeclare("Disconnect", false, false, false, null);
    }

    public void closeConnection() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }

    public void defineCallbacks(PixelGrid grid, BrushManager brushManager) throws IOException {
        defineNewPositionCallback(brushManager);
        defineNewColorCallback(grid);
        defineDisconnectCallback(brushManager);
    }


    private void defineNewColorCallback(PixelGrid grid) {
        DeliverCallback newColorCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received C '" + message + "' by thread "+Thread.currentThread().getName());
            String[] parts = message.split(" ");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int color = Integer.parseInt(parts[3]);
            grid.set(x, y, color);
        };
        try {
            channel.basicConsume("NewColor", true, newColorCallback, consumerTag -> {});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void defineNewPositionCallback(BrushManager brushManager) throws IOException {
        DeliverCallback newBrushPositionCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received POSITION '" + message);

            String[] parts = message.split(" ");
            // Message: brushId x y color
            UUID brushId = UUID.fromString(parts[0]);
            int newX = Integer.parseInt(parts[1]);
            int newY = Integer.parseInt(parts[2]);
            var brush = brushManager.getBrushMap().keySet().stream().filter(b -> b.equals(brushId)).findFirst();
            brush.ifPresentOrElse(b -> brushManager.getBrushMap().get(brushId).updatePosition(newX, newY),
                    () -> {
                    var newBrush = new BrushManager.Brush(newX, newY, 0);
                    brushManager.getBrushMap().put(brushId, newBrush);
            });


        };
        channel.basicConsume("NewPosition", true, newBrushPositionCallback, consumerTag -> {});
    }

    private void defineDisconnectCallback(BrushManager brushManager) throws IOException {
        DeliverCallback disconnectCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" -Disconnected '" + message + "' by thread "+Thread.currentThread().getName());
            String[] parts = message.split(" ");
            // Message: brushId
            UUID brushId = UUID.fromString(parts[0]);
            brushManager.getBrushMap().remove(brushId);
        };
        channel.basicConsume("Disconnect", true, disconnectCallback, consumerTag -> {});
    }

    public void sendNewColorToBroker(UUID id, int x, int y, int color) {
        try {
            String message = id + " " + x + " " + y + " " + color;
            channel.basicPublish("", "NewColor", null, message.getBytes());
            System.out.println(" [*] Sent COLOR '" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendNewPositionToBroker(UUID id, int x, int y) {
        delayTicks++;
        delayTicks %= 50;
        try {
            if (delayTicks == 0) {

                String message = id + " " + x + " " + y;
                channel.basicPublish("", "NewPosition", null, message.getBytes());
                System.out.println(" [*] Sent POSITION '" + message + "'");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDisconnectMessageToBroker(UUID uuid) {
        try {
            String message = uuid.toString();
            channel.basicPublish("", "Disconnect", null, message.getBytes());
            System.out.println(" [*] Sent DISCONNECT'" + message + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
