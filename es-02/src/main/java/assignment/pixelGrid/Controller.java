package assignment.pixelGrid;

import java.util.UUID;

public interface Controller {
    void setUpConnection(String sessionId, String userId);
    void closeConnection();
    void sendPixelUpdateToBroker(UUID uuid, int x, int y, int color);
    void sendBrushPositionToBroker(UUID uuid, int x, int y, int color);
    void sendUserDisconnectionToBroker(UUID uuid);

}
