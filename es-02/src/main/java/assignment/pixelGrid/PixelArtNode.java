package assignment.pixelGrid;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class PixelArtNode {
    private BrushManager brushManager;
    private BrushManager.Brush localBrush;
    private PixelGrid grid;

    private final PixelArtConnection connection = new PixelArtConnection(this);
    private final UUID uuid = UUID.randomUUID();
    private final StartMenuView startMenuView = new StartMenuView();
    private PixelGridView gridView;

    public PixelArtNode() throws IOException, TimeoutException {
    }


    public PixelGrid getGrid(){
        return this.grid;
    }
    public PixelGridView getView(){
        return this.gridView;
    }

    public BrushManager getBrushManager() {
        return brushManager;
    }

    public static int randomColor() {
        Random rand = new Random();
        return rand.nextInt(256 * 256 * 256);
    }

    public void start() {
        this.brushManager = new BrushManager();
        this.localBrush = new BrushManager.Brush(0, 0, randomColor());
        this.brushManager.addBrush(this.uuid, localBrush);
    }

    private void setUpMatch(String sessionId) throws IOException, TimeoutException {
        this.gridView = setUpGrid();
        this.connection.setUpConnection(sessionId, this.uuid.toString());
        this.setUpGridViewListeners();
        gridView.display();
    }

    private void setUpGridViewListeners() {
        gridView.addMouseMovedListener((x, y) -> {
            localBrush.updatePosition(x, y);
            this.connection.sendNewBrushPositionToBroker(this.uuid, x, y, localBrush.getColor());
           // System.out.println(localBrush.getX() + " " + localBrush.getY());
            gridView.refresh();
        });

        gridView.addPixelGridEventListener((x, y) -> {
            grid.set(x, y, localBrush.getColor());
            System.out.println("---> sending color to broker");
            this.connection.sendPixelUpdateToBroker(this.uuid, x, y, localBrush.getColor());
            gridView.refresh();
            System.out.println("---> done");
        });

        gridView.addColorChangedListener((int color) -> {
            this.localBrush.setColor(color);
            // TODO: send color change to broker
        });
        // add listener for closing the window
        gridView.addWindowClosedListener(() -> {
            // TODO: send disconnect message to broker
        });
        gridView.addColorChangedListener(localBrush::setColor);
    }

    public void createSession(){
        // TODO
    }

    public void joinSession(int sessionId){
        // TODO
    }

    public void setGrid(PixelGrid grid) {
        this.grid = grid;
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
