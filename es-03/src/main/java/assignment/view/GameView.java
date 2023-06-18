package assignment.view;

import assignment.controller.Controller;

import javax.swing.*;

public class GameView extends JFrame {
    public GameView(final String sessionId, final Controller controller) {
        super("Pixel Painter");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(800, 600);
        this.setLocationRelativeTo(null);
        this.setTitle("Game : " + sessionId);
    }
}
