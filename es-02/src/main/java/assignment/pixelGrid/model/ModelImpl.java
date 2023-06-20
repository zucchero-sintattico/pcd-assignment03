package assignment.pixelGrid.model;

import java.io.IOException;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ModelImpl implements ObservableModel {
    private final BrushManager brushManager = new BrushManager();
    private PixelGrid grid;
    private Consumer<PixelGrid> pixelGridEventListener = (p) -> {
    };
    private Consumer<BrushManager> brushManagerEventListener = (b) -> {
    };
    private Consumer<String> disconnectEventListener = (u) -> {
    };
    private final String userId = UUID.randomUUID().toString();

    public ModelImpl() {
        try {
            this.grid = new PixelGrid(40, 40);
            this.setUpGrid();
            this.brushManager.getBrushMap().put(this.userId, new BrushManager.Brush(0, 0, randomColor()));
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    public ModelImpl(PixelGrid grid) {
        this.grid = grid;
        this.brushManager.getBrushMap().put(this.userId, new BrushManager.Brush(0, 0, randomColor()));
    }

    @Override
    public BrushManager getBrushManager() {
        return this.brushManager;
    }


    @Override
    public PixelGrid getGrid() {
        return this.grid;
    }

    @Override
    public void setGrid(PixelGrid grid) {
        for (int x = 0; x < grid.getNumColumns(); x++) {
            for (int y = 0; y < grid.getNumRows(); y++) {
                this.grid.set(x, y, grid.get(x, y));
            }
        }
        this.pixelGridEventListener.accept(this.grid);
    }

    @Override
    public String getUuid() {
        return this.userId;
    }

    @Override
    public void updateBrush(String userId, int x, int y, int color) {
        this.brushManager.getBrushMap().put(userId, new BrushManager.Brush(x, y, color));
        this.brushManagerEventListener.accept(this.brushManager);
    }

    @Override
    public void removeBrush(String userId) {
        this.brushManager.getBrushMap().remove(userId);
        this.brushManagerEventListener.accept(this.brushManager);
    }

    @Override
    public void addBrush(String userId, int x, int y, int color) {
        this.brushManager.getBrushMap().put(userId, new BrushManager.Brush(x, y, color));
    }

    @Override
    public void updateGridPixel(int x, int y, int color) {
        this.grid.set(x, y, color);
        this.pixelGridEventListener.accept(this.grid);
    }

    @Override
    public void setPixelGridEventListener(Consumer<PixelGrid> listener) {
        this.pixelGridEventListener = listener;
    }

    @Override
    public void setBrushManagerEventListener(Consumer<BrushManager> listener) {
        this.brushManagerEventListener = listener;
    }

    @Override
    public void setDisconnectEventListener(Consumer<String> listener) {
        this.disconnectEventListener = listener;
    }

    private void setUpGrid() throws IOException, TimeoutException {
        this.grid = new PixelGrid(40, 40);
        Random rand = new Random();
        for (int i = 0; i < 10; i++) {
            grid.set(rand.nextInt(40), rand.nextInt(40), randomColor());
        }

    }

    private int randomColor() {
        Random rand = new Random();
        return rand.nextInt(256 * 256 * 256);
    }
}
