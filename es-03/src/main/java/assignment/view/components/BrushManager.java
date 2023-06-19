package assignment.view.components;


import java.awt.*;
import java.util.List;
import java.util.*;

public class BrushManager {
    private static final int BRUSH_SIZE = 10;
    private static final int STROKE_SIZE = 2;
    private final Map<UUID, Brush> brushMap = new HashMap<>();

    public void draw(final Graphics2D g) {
        this.brushMap.values().forEach(brush -> {
            g.setColor(new Color(brush.color));
            var circle = new java.awt.geom.Ellipse2D.Double(brush.x - BRUSH_SIZE / 2.0, brush.y - BRUSH_SIZE / 2.0, BRUSH_SIZE, BRUSH_SIZE);
            // draw the polygon
            g.fill(circle);
            g.setStroke(new BasicStroke(STROKE_SIZE));
            g.setColor(Color.BLACK);
            g.draw(circle);
        });
    }

    public void addBrush(final UUID id, final Brush brush) {
        this.brushMap.put(id, brush);
        //this.brushes.add(brush);
    }

    public Map<UUID, Brush> getBrushMap() {
        return brushMap;
    }

    public void removeBrush(final UUID id) {
        this.brushMap.remove(id);
    }

    public void updateBrush(UUID uuid, int x, int y) {
        this.brushMap.get(uuid).updatePosition(x, y);
    }

    public static class Brush {
        private int x, y;
        private int color;

        public Brush(final int x, final int y, final int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        public void updatePosition(final int x, final int y) {
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
