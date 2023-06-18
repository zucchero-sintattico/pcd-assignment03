package assignment.view;

import assignment.controller.Controller;
import assignment.model.Model;

import javax.swing.*;

public class PixArtView implements View {

    private Controller controller;

    private JFrame startMenuView;
    private JFrame gameFrame;

    public PixArtView(final Controller controller) {
        this.controller = controller;
        this.startMenuView = new StartMenuView(this.controller, (sessionId) -> {
            this.gameFrame = new GameView(sessionId, this.controller);
            this.startMenuView.setVisible(false);
            this.gameFrame.setVisible(true);
        });
    }

    @Override
    public void onModelReady(Model model) {

    }

    @Override
    public void onNewPlayer(String clientId) {

    }

    @Override
    public void onPlayerLeave(String clientId) {

    }

    @Override
    public void onNewMousePosition(String clientId, int x, int y) {

    }

    @Override
    public void onPixelUpdated(int x, int y, int color) {

    }


    @Override
    public void show() {
        this.startMenuView.setVisible(true);
    }
}
