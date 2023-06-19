package assignment.client;

import assignment.model.Model;
import assignment.utils.MousePosition;
import assignment.utils.PixelInfo;

import java.util.function.Consumer;

public interface ObservableClient extends Client {

    void setOnModelReadyListener(Consumer<Model> onModelReadyListener);

    void setOnUserJoinListener(Consumer<String> onUserJoinListener);

    void setOnUserLeaveListener(Consumer<String> onUserLeaveListener);

    void setOnNewMousePositionListener(Consumer<MousePosition> onNewMousePositionListener);

    void setOnPixelUpdatedListener(Consumer<PixelInfo> onPixelUpdatedListener);
}
