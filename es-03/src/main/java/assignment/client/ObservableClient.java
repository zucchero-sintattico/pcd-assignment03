package assignment.client;

import assignment.utils.MousePosition;
import assignment.utils.PixelInfo;

import java.util.function.Consumer;

public interface ObservableClient extends Client {
    void setOnUserJoinListener(Consumer<String> onUserJoinListener);
    void setOnUserLeaveListener(Consumer<String> onUserLeaveListener);
    void setOnNewMousePositionListener(Consumer<MousePosition> onNewMousePositionListener);
    void setOnPixelUpdatedListener(Consumer<PixelInfo> onPixelUpdatedListener);
}
