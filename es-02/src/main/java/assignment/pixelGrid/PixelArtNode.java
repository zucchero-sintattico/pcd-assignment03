package assignment.pixelGrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeoutException;


public class PixelArtNode {
    private BrushManager brushManager;
    private BrushManager.Brush localBrush;
    private PixelGrid grid;
    private PixelArtConnection connection = new PixelArtConnection();

    public static int randomColor() {
        Random rand = new Random();
        return rand.nextInt(256 * 256 * 256);
    }

    public void Start() throws IOException, TimeoutException {
        this.brushManager = new BrushManager();
        this.localBrush = new BrushManager.Brush(0, 0, randomColor());
        brushManager.addBrush(localBrush);
        PixelGridView view = setUpGrid();
        this.connection.setUpConnection();

        view.addMouseMovedListener((x, y) -> {
            localBrush.updatePosition(x, y);
            this.connection.sendNewPositionToBroker(x, y);
            view.refresh();
        });

        view.addPixelGridEventListener((x, y) -> {
            grid.set(x, y, localBrush.getColor());
            this.connection.sendNewColorToBroker(x, y, localBrush.getColor());
            view.refresh();
        });

        view.addColorChangedListener(localBrush::setColor);

        view.display();
    }

    private PixelGridView setUpGrid() throws IOException, TimeoutException {
        this.grid = new PixelGrid(40,40);
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            grid.set(rand.nextInt(40), rand.nextInt(40), randomColor());
        }
        PixelGridView view = new PixelGridView(grid, brushManager, 800, 800);
        return view;
    }
}
