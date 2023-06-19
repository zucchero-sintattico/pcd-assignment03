package assignment.pixelGrid;

import assignment.pixelGrid.BrushManager;
import assignment.pixelGrid.view.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;

import java.util.UUID;

public interface Model {
    void start();
    void setGrid(PixelGrid grid);
    void onBrushPosition(UUID uuid, int x, int y, int color);
    void onPixelUpdate(int x, int y, int color);
    void onDisconnect(UUID uuid);
    PixelGrid getGrid();
    PixelGridView getView();
    BrushManager getBrushManager();


}
