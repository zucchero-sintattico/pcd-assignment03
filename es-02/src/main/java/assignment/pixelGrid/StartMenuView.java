package assignment.pixelGrid;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class StartMenuView extends JFrame {
    private final JButton createSessionButton = new JButton("Create Session");
    private final JButton joinSessionButton = new JButton("Join Session");
    private final JTextField sessionIdField = new JTextField("Session ID");

    public StartMenuView() {
        this.setTitle("Pixel Art");
        this.setSize(300, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        this.add(createSessionButton);
        this.add(joinSessionButton);
        this.add(sessionIdField);

        //create new session
        this.createSessionButton.addActionListener(e -> {
            try {
                if (sessionIdField.getText().equals("Session ID")) {
                    JOptionPane.showMessageDialog(this, "Please insert a session ID");
                } else {
                    new PixelArtNode(sessionIdField.getText(), true);
                }
            } catch (IOException | TimeoutException ioException) {
                ioException.printStackTrace();
            }
        });

        // check if when join session is clicked, the session id is inserted
        this.joinSessionButton.addActionListener(e -> {
            if (sessionIdField.getText().equals("Session ID")) {
                JOptionPane.showMessageDialog(this, "Please insert a session ID");
            } else {
                //
            }
        });

        this.setVisible(true);
    }

}
