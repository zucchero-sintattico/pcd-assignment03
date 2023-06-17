package assignment.pixelGrid;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class PixelArtNode {
    private BrushManager brushManager;
    private BrushManager.Brush localBrush;
    private PixelGrid grid;

    private PixelGridView view;
    private final PixelArtConnection connection = new PixelArtConnection(this);
    private final UUID uuid = UUID.randomUUID();

    public PixelArtNode() throws IOException, TimeoutException {
    }


    public PixelGrid getGrid(){
        return this.grid;
    }
    public PixelGridView getView(){
        return this.view;
    }

    public BrushManager getBrushManager() {
        return brushManager;
    }

    public static int randomColor() {
        Random rand = new Random();
        return rand.nextInt(256 * 256 * 256);
    }

    public void start() throws IOException, TimeoutException {
        this.brushManager = new BrushManager();
        this.localBrush = new BrushManager.Brush(0, 0, randomColor());
        this.brushManager.addBrush(this.uuid, localBrush);
        this.view = setUpGrid();
        this.connection.setUpConnection();
        this.connection.defineCallbacks();

        view.addMouseMovedListener((x, y) -> {
            localBrush.updatePosition(x, y);
            this.connection.sendNewPositionToBroker(this.uuid, x, y, localBrush.getColor());
           // System.out.println(localBrush.getX() + " " + localBrush.getY());
            view.refresh();
        });

        view.addPixelGridEventListener((x, y) -> {
            grid.set(x, y, localBrush.getColor());
            System.out.println("---> sending color to broker");
            this.connection.sendNewColorToBroker(this.uuid, x, y, localBrush.getColor());
            view.refresh();
            System.out.println("---> done");
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
