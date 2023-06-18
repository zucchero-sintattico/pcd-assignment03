package assignment.view;

import assignment.controller.Controller;
import assignment.model.Model;

import java.util.List;

public interface View {

    void onModelReady(Model model);

    void onNewPlayer(String clientId);
    void onPlayerLeave(String clientId);
    void onNewMousePosition(String clientId, int x, int y);
    void onPixelUpdated(int x, int y, int color);

    void show();
}
