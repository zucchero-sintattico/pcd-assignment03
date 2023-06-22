package assignment.pixelArt.view;

import assignment.pixelArt.controller.ControllerImpl;

import javax.swing.*;

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
            if (sessionIdField.getText().equals("Session ID")) {
                JOptionPane.showMessageDialog(this, "Please insert a session ID");
            } else {
                new ControllerImpl(true, sessionIdField.getText()).start();
                this.setVisible(false);
            }
        });

        // check if when join session is clicked, the session id is inserted
        this.joinSessionButton.addActionListener(e -> {
            if (sessionIdField.getText().equals("Session ID")) {
                JOptionPane.showMessageDialog(this, "Please insert a session ID");
            } else {
                new ControllerImpl(false, sessionIdField.getText()).start();
                this.setVisible(false);
            }
        });

        this.setVisible(true);
    }

}
