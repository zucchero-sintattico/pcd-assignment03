package assignment.pixelGrid;

import assignment.pixelGrid.BrushManager;
import assignment.pixelGrid.view.PixelGrid;
import assignment.pixelGrid.view.PixelGridView;

public interface Model {
    void setNodeSession(String sessionId, Boolean newSession);
    void start();
    void setGrid(PixelGrid grid);
    PixelGrid getGrid();
    PixelGridView getView();
    BrushManager getBrushManager();

}
