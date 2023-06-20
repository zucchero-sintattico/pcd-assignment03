package assignment.pixelGrid.view;

import assignment.pixelGrid.model.BrushManager;
import assignment.pixelGrid.model.PixelGrid;

import javax.swing.*;
import java.awt.*;

public class VisualiserPanel extends JPanel {
    private static final int STROKE_SIZE = 1;
    private BrushManager brushManager;
    private PixelGrid grid;
    private final int w, h;

    public VisualiserPanel(PixelGrid grid, BrushManager brushManager, int w, int h) {
        setSize(w, h);
        this.grid = grid;
        this.w = w;
        this.h = h;
        this.brushManager = brushManager;
        this.setPreferredSize(new Dimension(w, h));
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.clearRect(0, 0, this.getWidth(), this.getHeight());

        int dx = w / grid.getNumColumns();
        int dy = h / grid.getNumRows();
        g2.setStroke(new BasicStroke(STROKE_SIZE));
        for (int i = 0; i < grid.getNumRows(); i++) {
            int y = i * dy;
            g2.drawLine(0, y, w, y);
        }

        for (int i = 0; i < grid.getNumColumns(); i++) {
            int x = i * dx;
            g2.drawLine(x, 0, x, h);
        }

        for (int row = 0; row < grid.getNumRows(); row++) {
            int y = row * dy;
            for (int column = 0; column < grid.getNumColumns(); column++) {
                int x = column * dx;
                int color = grid.get(column, row);
                if (color != 0) {
                    g2.setColor(new Color(color));
                    g2.fillRect(x + STROKE_SIZE, y + STROKE_SIZE, dx - STROKE_SIZE, dy - STROKE_SIZE);
                }
            }
        }

        brushManager.draw(g2);
    }

    public void setGrid(PixelGrid grid) {
        for (int x = 0; x < grid.getNumColumns(); x++) {
            for (int y = 0; y < grid.getNumRows(); y++) {
                this.grid.set(x, y, grid.get(x, y));
            }
        }
    }

    public void setBrushManager(BrushManager brushManager) {
        this.brushManager = brushManager;
    }
}
