package assignment.pixelGrid;

import assignment.pixelGrid.view.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class PixelArtModel implements Model {
    private BrushManager brushManager;
    private BrushManager.Brush localBrush;
    private PixelGrid grid;
    private String sessionId;
    Boolean newSession;
    private final PixelArtController connection = new PixelArtController(this);
    private final UUID uuid = UUID.randomUUID();
    private PixelGridView gridView;


    public void setNodeSession(String sessionId, Boolean newSession){
        this.sessionId = sessionId;
        this.newSession = newSession;
    }
    public PixelArtModel(String sessionId, Boolean newSession) throws IOException, TimeoutException {
        this.setNodeSession(sessionId, newSession);
        this.start();
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
        this.setUpSession(this.sessionId);
        this.setUpGridViewListeners();
    }

    private void setUpSession(String sessionId){
        try {
            this.gridView = setUpGrid();
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        this.connection.setUpConnection(sessionId, this.uuid.toString());
        gridView.display();
    }

    private void setUpGridViewListeners() {
        gridView.addMouseMovedListener((x, y) -> {
            localBrush.updatePosition(x, y);
            this.connection.sendBrushPositionToBroker(this.uuid, x, y, localBrush.getColor());
           // System.out.println(localBrush.getX() + " " + localBrush.getY());
            gridView.refresh();
        });

        gridView.addPixelGridEventListener((x, y) -> {
            grid.set(x, y, localBrush.getColor());
            System.out.println("---> sending color to broker");
            this.connection.sendPixelUpdateToBroker(this.uuid, x, y, localBrush.getColor());
            gridView.refresh();
        });

        // add listener for closing the window
        gridView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                connection.closeConnection();
                super.windowClosing(e);
                gridView.refresh();
            }

        });
        gridView.addColorChangedListener(localBrush::setColor);
    }


    public void setGrid(PixelGrid grid) {
        this.grid = grid;
    }

    @Override
    public void onBrushPosition(UUID uuid, int x, int y, int color) {
        var brush = this.getBrushManager().getBrushMap().keySet().stream().filter(b -> b.equals(uuid)).findFirst();
        brush.ifPresentOrElse(b -> {
                    this.getBrushManager().getBrushMap().get(uuid).updatePosition(x, y);
                    this.getBrushManager().getBrushMap().get(uuid).setColor(color);
                },
                () -> { var newBrush = new BrushManager.Brush(x, y, color);
                    this.getBrushManager().getBrushMap().put(uuid, newBrush);
                });
    }

    @Override
    public void onPixelUpdate(int x, int y, int color) {
        this.getGrid().set(x, y, color);
    }

    @Override
    public void onDisconnect(UUID uuid) {
        this.getBrushManager().getBrushMap().remove(uuid);
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
