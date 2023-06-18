package assignment.view;

import assignment.controller.Controller;
import assignment.model.Model;

import javax.swing.*;
import java.awt.event.WindowEvent;

public class PixArtView implements View {

    private Controller controller;

    private JFrame startMenuView;
    private JFrame gameFrame;

    private JFrame currentFrame;

    public PixArtView(final Controller controller) {
        this.controller = controller;
        this.startMenuView = new StartMenuView(this.controller, (sessionId) -> {
            this.gameFrame = new GameView(sessionId, this.controller);
            this.gameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            this.gameFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    System.out.println("Closing");
                    PixArtView.this.controller.leave();
                }
            });
            this.switchCurrentFrame(this.gameFrame);
        });
        this.startMenuView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.currentFrame = this.startMenuView;
    }

    private void switchCurrentFrame(final JFrame next) {
        this.currentFrame.setVisible(false);
        this.currentFrame = next;
        this.currentFrame.setVisible(true);
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
