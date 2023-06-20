package assignment.pixelGrid.model;

import assignment.pixelGrid.BrushManager;
import assignment.pixelGrid.view.PixelGrid;

import java.util.Map;

public interface Model {

    BrushManager getBrushManager();
    Map<String, BrushManager.Brush> getBrushes();
    PixelGrid getGrid();
    void setGrid(PixelGrid grid);
    String getUuid();
    void updateBrush(String userId, int x, int y, int color);
    void removeBrush(String userId);
    void addBrush(String userId, int x, int y, int color);
    void updateGridPixel(int x, int y, int color);
}
