package assignment.view;

import assignment.model.Model;

import java.util.Map;

public interface View {

    void onModelReady(Model model);

    void onNewPlayer(String clientId, int color);

    void onPlayerLeave(String clientId);

    void onNewMousePosition(String clientId, int x, int y);

    void onPixelUpdated(int x, int y, int color);

    void show();

    void onPlayerColorUpdate(String clientId, int color);
}
