package assignment.view;

import assignment.controller.Controller;

import javax.swing.*;
import java.util.function.Consumer;

public class StartMenuView extends JFrame {

    private final Controller controller;

    private final JTextField sessionIdField = new JTextField("Session ID");

    public StartMenuView(final Controller controller, Consumer<String> onSessionJoin) {
        this.controller = controller;
        this.setTitle("Pixel Art");
        this.setSize(300, 300);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        JButton createSessionButton = new JButton("Create Session");
        this.add(createSessionButton);
        JButton joinSessionButton = new JButton("Join Session");
        this.add(joinSessionButton);
        this.add(sessionIdField);
        // check if when join session is clicked, the session id is inserted
        joinSessionButton.addActionListener(e -> {
            if (sessionIdField.getText().equals("Session ID")) {
                JOptionPane.showMessageDialog(this, "Please insert a session ID");
            } else {
                this.controller.join(sessionIdField.getText());
                onSessionJoin.accept(sessionIdField.getText());
            }
        });
        createSessionButton.addActionListener(e -> {
            String sessionId = this.controller.create(sessionIdField.getText());
            onSessionJoin.accept(sessionId);
        });
    }

}
