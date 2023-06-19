package assignment.pixelGrid;

import assignment.pixelGrid.view.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class PixelArtNode {
    private BrushManager brushManager;
    private BrushManager.Brush localBrush;
    private PixelGrid grid;
    private String sessionId;
    Boolean newSession;

    private final PixelArtConnection connection = new PixelArtConnection(this);
    private final UUID uuid = UUID.randomUUID();
    private PixelGridView gridView;


    public void setNodeSession(String sessionId, Boolean newSession){
        this.sessionId = sessionId;
        this.newSession = newSession;
    }
    public PixelArtNode(String sessionId,Boolean newSession) throws IOException, TimeoutException {
        this.setNodeSession(sessionId, newSession);
        this.start(sessionId);
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

    public void start(final String sessionId) throws IOException, TimeoutException {
        this.brushManager = new BrushManager();
        this.localBrush = new BrushManager.Brush(0, 0, randomColor());
        this.brushManager.addBrush(this.uuid, localBrush);
        this.setUpSession(sessionId);
        this.setUpGridViewListeners();
    }

    private void setUpSession(String sessionId) throws IOException, TimeoutException {
        this.gridView = setUpGrid();
        this.connection.setUpConnection(sessionId, this.uuid.toString());
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

        // add listener for closing the window
        gridView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    connection.closeConnection();
                } catch (IOException | TimeoutException ex) {
                    throw new RuntimeException(ex);
                }
                super.windowClosing(e);
            }

        });
        gridView.addColorChangedListener(localBrush::setColor);
    }


    public void setGrid(PixelGrid grid) {
        this.grid = grid;
    }

    private PixelGridView setUpGrid() throws IOException, TimeoutException {
        this.grid = new PixelGrid(40,40);
        Random rand = new Random();
        if(this.newSession){
            for (int i = 0; i < 10; i++) {
                grid.set(rand.nextInt(40), rand.nextInt(40), randomColor());
            }
        }
        return new PixelGridView(grid, brushManager, 800, 800);
    }
}
