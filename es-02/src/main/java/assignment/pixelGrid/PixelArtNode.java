package assignment.pixelGrid;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class PixelArtNode {
    private BrushManager brushManager;
    private BrushManager.Brush localBrush;
    private PixelGrid grid;
    private PixelArtConnection connection = new PixelArtConnection();
    private final UUID uuid = UUID.randomUUID();

    public static int randomColor() {
        Random rand = new Random();
        return rand.nextInt(256 * 256 * 256);
    }

    public void Start() throws IOException, TimeoutException {
        this.brushManager = new BrushManager();
        this.localBrush = new BrushManager.Brush(0, 0, randomColor());

        brushManager.addBrush(this.uuid, localBrush);
        PixelGridView view = setUpGrid();
        this.connection.setUpConnection();
        this.connection.defineCallbacks(this.grid, this.brushManager);

        view.addMouseMovedListener((x, y) -> {
            localBrush.updatePosition(x, y);
            this.connection.sendNewPositionToBroker(this.uuid, x, y);
           // System.out.println(localBrush.getX() + " " + localBrush.getY());
            view.refresh();
        });

        view.addPixelGridEventListener((x, y) -> {
            grid.set(x, y, localBrush.getColor());
            this.connection.sendNewColorToBroker(this.uuid, x, y, localBrush.getColor());
            view.refresh();
        });

        view.addColorChangedListener(localBrush::setColor);
        // add listener for closing the window
        view.addWindowClosedListener(() -> {
            try {
                this.connection.sendDisconnectMessageToBroker(this.uuid);
                this.connection.closeConnection();
            } catch (IOException | TimeoutException e) {
                e.printStackTrace();
            }
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
        return new PixelGridView(grid, brushManager, 800, 800);
    }
}
