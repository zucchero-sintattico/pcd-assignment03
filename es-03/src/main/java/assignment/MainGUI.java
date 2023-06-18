package assignment;

import assignment.controller.Controller;
import assignment.controller.PixelArtController;
import assignment.view.View;
import assignment.view.PixArtView;

public class MainGUI {
    public static void main(String[] args) {

        final Controller controller = new PixelArtController();
        final View view = new PixArtView(controller);

        controller.setView(view);

        view.show();
    }
}
