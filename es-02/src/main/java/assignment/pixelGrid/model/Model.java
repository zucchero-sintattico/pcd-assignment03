package assignment.pixelGrid.model;

public interface Model {

    BrushManager getBrushManager();

    PixelGrid getGrid();

    void setGrid(PixelGrid grid);

    String getUuid();

    void updateBrush(String userId, int x, int y, int color);

    void removeBrush(String userId);

    void addBrush(String userId, int x, int y, int color);

    void updateGridPixel(int x, int y, int color);
}
