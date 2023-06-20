package assignment.pixelGrid.model;

import assignment.pixelGrid.BrushManager;
import assignment.pixelGrid.view.PixelGrid;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ModelImpl implements ObservableModel {
    private final BrushManager brushManager = new BrushManager();
    private final Map<String, BrushManager.Brush> brushes = new HashMap<>();
    private PixelGrid grid;
    private Consumer<PixelGrid> pixelGridEventListener = (p) -> {};
    private Consumer<BrushManager> brushManagerEventListener = (b) -> {};
    private Consumer<String> disconnectEventListener = (u) -> {};
    private final String userId = UUID.randomUUID().toString();

    public ModelImpl() {
        try {
            this.grid = new PixelGrid(10, 10);
            this.brushes.put(this.userId, new BrushManager.Brush(0, 0, 0));
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    public ModelImpl(PixelGrid grid) {
        this.grid = grid;
        this.brushes.put(UUID.randomUUID().toString(), new BrushManager.Brush(0, 0, 0));
    }

    @Override
    public BrushManager getBrushManager() {
        return this.brushManager;
    }

    @Override
    public Map<String, BrushManager.Brush> getBrushes() {
        return this.brushes;
    }

    @Override
    public PixelGrid getGrid() {
        return this.grid;
    }

    @Override
    public void setGrid(PixelGrid grid) {
        this.grid = grid;
    }

    @Override
    public String getUuid() {
        return this.userId;
    }

    @Override
    public void updateBrush(String userId, int x, int y, int color) {
        this.brushes.put(userId, new BrushManager.Brush(x, y, color));
        this.brushManagerEventListener.accept(this.brushManager);
    }

    @Override
    public void removeBrush(String userId) {
        this.brushes.remove(userId);
        this.brushManagerEventListener.accept(this.brushManager);
    }

    @Override
    public void addBrush(String userId, int x, int y, int color) {
        this.brushes.put(userId, new BrushManager.Brush(x, y, color));
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
}
