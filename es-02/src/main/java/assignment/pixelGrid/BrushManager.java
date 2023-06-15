package assignment.pixelGrid;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class BrushManager {
    private static final int BRUSH_SIZE = 10;
    private static final int STROKE_SIZE = 2;
    private List<Brush> brushes = new java.util.ArrayList<>();

    private static Channel channel;
    private static Connection connection;


    public BrushManager() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
    }


    void draw(final Graphics2D g) {
        brushes.forEach(brush -> {
            g.setColor(new Color(brush.color));
            var circle = new java.awt.geom.Ellipse2D.Double(brush.x - BRUSH_SIZE / 2.0, brush.y - BRUSH_SIZE / 2.0, BRUSH_SIZE, BRUSH_SIZE);
            // draw the polygon
            g.fill(circle);
            g.setStroke(new BasicStroke(STROKE_SIZE));
            g.setColor(Color.BLACK);
            g.draw(circle);
        });
    }

    void addBrush(final Brush brush) {
        brushes.add(brush);
    }

    void removeBrush(final Brush brush) {
        brushes.remove(brush);
    }

    public static class Brush {
        private int x, y;
        private int color;
        private int lag_counter;

        public Brush(final int x, final int y, final int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void updatePosition(final int x, final int y) {
            lag_counter++;
            lag_counter %= 50;
            try {
                if (lag_counter == 0) {
                    channel.queueDeclare("Positions", false, false, false, null);
                    String message = "x = " + x + " y = " + y;
                    channel.basicPublish("", "Positions", null, message.getBytes());
                    System.out.println(" [*] Sent '" + message + "'");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            this.x = x;
            this.y = y;
        }
        // write after this getter and setters
        public int getX(){
            return this.x;
        }
        public int getY(){
            return this.y;
        }
        public int getColor(){
            return this.color;
        }
        public void setColor(int color){
            this.color = color;
        }
    }
}
