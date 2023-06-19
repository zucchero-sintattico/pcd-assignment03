package assignment.client;

import assignment.model.Model;
import assignment.utils.MousePosition;
import assignment.utils.PixelInfo;

import java.util.Map;
import java.util.function.Consumer;

public interface ObservableClient extends Client {

    void setOnModelReadyListener(Consumer<Model> onModelReadyListener);

    void setOnUserJoinListener(Consumer<Map.Entry<String, Integer>> onUserJoinListener);

    void setOnUserLeaveListener(Consumer<String> onUserLeaveListener);

    void setOnUserColorUpdateListener(Consumer<Map.Entry<String, Integer>> onUserColorUpdateListener);

    void setOnNewMousePositionListener(Consumer<MousePosition> onNewMousePositionListener);

    void setOnPixelUpdatedListener(Consumer<PixelInfo> onPixelUpdatedListener);
}
