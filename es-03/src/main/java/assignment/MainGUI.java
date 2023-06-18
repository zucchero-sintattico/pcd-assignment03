package assignment;

import assignment.controller.Controller;
import assignment.controller.PixelArtController;
import assignment.view.View;
import assignment.view.PixArtView;

public class MainGUI {
    public static void main(String[] args) {
        System.out.println("AAAAAAAAAAAAAAAAAAAAAa");
        final int numberOfInstances = args.length > 0 ? Integer.parseInt(args[0]) : 1;

        for (int i = 0; i < numberOfInstances; i++) {
            final Controller controller = new PixelArtController();
            final View view = new PixArtView(controller);
            controller.setView(view);
            view.show();
        }

    }
}
