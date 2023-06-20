package assignment.pixelGrid.model;

import java.util.function.Consumer;

public interface ObservableModel extends Model {

    void setPixelGridEventListener(Consumer<PixelGrid> listener);

    void setBrushManagerEventListener(Consumer<BrushManager> listener);

    void setDisconnectEventListener(Consumer<String> listener);


}
