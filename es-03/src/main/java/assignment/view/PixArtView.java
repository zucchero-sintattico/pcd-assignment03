package assignment.view;

import assignment.controller.Controller;
import assignment.model.Model;
import assignment.utils.MousePosition;
import assignment.view.components.BrushManager;
import assignment.view.components.PixelGridView;

import javax.swing.*;
import java.util.UUID;

public class PixArtView implements View {

    private Controller controller;

    private StartMenuView startMenuView;
    private PixelGridView pixelGridView;

    private final BrushManager brushManager = new BrushManager();

    private JFrame currentFrame;

    public PixArtView(final Controller controller) {
        this.controller = controller;
        this.pixelGridView = new PixelGridView(this.controller, this.brushManager, 800, 600);
        this.pixelGridView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.startMenuView = new StartMenuView(this.controller, (sessionId) -> {
            this.switchCurrentFrame(this.pixelGridView);
        });
        this.startMenuView.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.currentFrame = this.startMenuView;
    }

    private void switchCurrentFrame(final JFrame next) {
        this.currentFrame.setVisible(false);
        this.currentFrame = next;
        this.currentFrame.pack();
        this.currentFrame.setVisible(true);
    }

    @Override
    public void onModelReady(Model model) {
        System.out.println("AAAAAAAAAAAAA");
        controller.getPlayersColor().forEach((clientId, color) -> {
            System.out.println("AAAAAAAAAAAAA");
            MousePosition mousePosition = controller.getPlayersMouse().get(clientId);
            System.out.println(UUID.fromString(clientId));
            this.brushManager.addBrush(
                    UUID.fromString(clientId),
                    new BrushManager.Brush(mousePosition.x, mousePosition.y, color)
            );
        });
        this.pixelGridView.refresh();
    }

    @Override
    public void onNewPlayer(String clientId, int color) {
        MousePosition mousePosition = controller.getPlayersMouse().get(clientId);
        this.brushManager.addBrush(
                UUID.fromString(clientId),
                new BrushManager.Brush(mousePosition.x, mousePosition.y, color)
        );
        this.pixelGridView.refresh();
    }

    @Override
    public void onPlayerLeave(String clientId) {
        this.brushManager.removeBrush(UUID.fromString(clientId));
        this.pixelGridView.refresh();
    }

    @Override
    public void onNewMousePosition(String clientId, int x, int y) {
        this.brushManager.updateBrush(UUID.fromString(clientId), x, y);
        this.pixelGridView.refresh();
    }

    @Override
    public void onPixelUpdated(int x, int y, int color) {
        this.pixelGridView.refresh();
    }


    @Override
    public void show() {
        this.startMenuView.setVisible(true);
    }

    @Override
    public void onPlayerColorUpdate(String clientId, int color) {
        this.brushManager.getBrushMap().get(UUID.fromString(clientId)).setColor(color);
        this.pixelGridView.refresh();
    }
}
