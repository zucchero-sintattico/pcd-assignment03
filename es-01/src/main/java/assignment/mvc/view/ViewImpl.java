package assignment.mvc.view;

import assignment.Domain.*;
import assignment.mvc.controller.AlgorithmStatus;
import assignment.mvc.controller.Controller;
import scala.collection.immutable.Range$;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.Integer.parseInt;

public class ViewImpl extends JFrame implements View {

    private final JLabel numberOfFilesLabel = new JLabel("Founded files: 0");
    private final JLabel statusLabel = new JLabel("Status: Stopped");
    private final JList<String> topNList = new JList<>();
    private final JList<String> distributionList = new JList<>();
    private final JTextField maxLinesText = new JTextField("100");
    private final JTextField topNText = new JTextField("10");
    private final JTextField nOfRangesText = new JTextField("5");
    private final JPanel preferencesPanel = new JPanel();
    private final JPanel resultsPanel = new JPanel();
    private final JPanel statusPanel = new JPanel();
    private Controller controller;
    private Path selectedPath;
    private AlgorithmStatus status = AlgorithmStatus.IDLE;

    public ViewImpl() {
        super("My View");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setPreferencesPanel();
        setResultsPanel();
        setStatusPanel();
        // adding the panels to the frame
        add(preferencesPanel, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);
        pack(); // adapts the frame to the components
        add(preferencesPanel);
        add(resultsPanel);
        add(statusPanel);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                System.exit(0);
            }
        });
    }

    private void setStatusPanel() {
        statusPanel.setPreferredSize(new Dimension(400, 100));
        statusPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // align to the right
        statusPanel.setBorder(new TitledBorder("Status Panel"));
        statusLabel.setOpaque(true); // make it opaque to see the background color
        this.updateAlgorithmStatus(AlgorithmStatus.IDLE);
        // create start and stop buttons
        final JButton startButton = new JButton("Start");
        final JButton stopButton = new JButton("Stop");
        final Supplier<Boolean> canStart = () -> this.selectedPath != null &&
                !maxLinesText.getText().equals("") &&
                !nOfRangesText.getText().equals("") &&
                !topNText.getText().equals("") &&
                !status.equals(AlgorithmStatus.RUNNING);

        // add listeners to buttons so that they can change color
        startButton.addActionListener(e -> {
            if (canStart.get()) {
                numberOfFilesLabel.setText("Founded files: 0");
                topNList.setListData(new String[0]);
                distributionList.setListData(new String[0]);
                controller.startAlgorithm(
                        this.selectedPath,
                        parseInt(topNText.getText()),
                        parseInt(nOfRangesText.getText()),
                        parseInt(maxLinesText.getText())
                );
            }
        });

        stopButton.addActionListener(e -> {
            if (status.equals(AlgorithmStatus.RUNNING)) {
                controller.stopAlgorithm();
            }
        });

        // add components to status panel
        statusPanel.add(numberOfFilesLabel);
        statusPanel.add(statusLabel);
        statusPanel.add(startButton);
        statusPanel.add(stopButton);
    }

    private void setResultsPanel() {
        resultsPanel.setLayout(new GridLayout(0, 2));
        resultsPanel.setBorder(new TitledBorder("Results Panel"));
        resultsPanel.setPreferredSize(new Dimension(400, 100));
        resultsPanel.add(topNList);
        resultsPanel.add(distributionList);
    }

    private void setPreferencesPanel() {
        preferencesPanel.setLayout(new GridLayout(5, 2));
        final JLabel nOfRangesLabel = new JLabel("Number of ranges:");
        final JLabel maxLinesLabel = new JLabel("Max number of lines:");
        final JLabel topNLabel = new JLabel("Top N files number:");
        final JLabel filePathLabel = new JLabel("File path:");
        final JButton filePathButton = new JButton("Browse");
        filePathButton.addActionListener(e -> {
            final JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            UIManager.put("FileChooser.readOnly", Boolean.TRUE);
            fileChooser.showOpenDialog(this);
            selectedPath = fileChooser.getSelectedFile().toPath();
            filePathLabel.setText("File path: " + selectedPath);
        });

        preferencesPanel.add(filePathLabel);
        preferencesPanel.add(filePathButton);
        preferencesPanel.add(nOfRangesLabel);
        preferencesPanel.add(nOfRangesText);
        preferencesPanel.add(maxLinesLabel);
        preferencesPanel.add(maxLinesText);
        preferencesPanel.add(topNLabel);
        preferencesPanel.add(topNText);
    }

    @Override
    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void updateAlgorithmStatus(final AlgorithmStatus status) {
        this.status = status;
        SwingUtilities.invokeLater(() -> {
            switch (this.status) {
                case IDLE -> {
                    statusLabel.setText("Status: Idle");
                    statusLabel.setBackground(Color.LIGHT_GRAY);
                }
                case RUNNING -> {
                    statusLabel.setText("Status: Running");
                    statusLabel.setBackground(Color.GREEN);
                }
                case STOPPED -> {
                    statusLabel.setText("Status: Stopped");
                    statusLabel.setBackground(Color.RED);
                }
                case FINISHED -> {
                    statusLabel.setText("Status: Finished");
                    statusLabel.setBackground(Color.ORANGE);
                }
            }
        });
    }

    @Override
    public void updateTopN(List<Statistic> stats) {
        final String[] formatted = stats.stream()
                .map(x -> x.size() + " - " + x.path().toString().replace(this.selectedPath.toString(), "")).toArray(String[]::new);
        SwingUtilities.invokeLater(() -> {
            topNList.setListData(formatted);
        });
    }

    @Override
    public void updateDistribution(Map<Range, Integer> distribution) {
        SwingUtilities.invokeLater(() -> {
            distributionList.setListData(
                    distribution.entrySet().stream()
                            .map(x -> x.getKey().toString() + " : " + x.getValue())
                            .toArray(String[]::new)
            );
        });

    }

    @Override
    public void updateNumberOfFiles(int numberOfFiles) {
        SwingUtilities.invokeLater(() -> {
            numberOfFilesLabel.setText("Founded files: " + numberOfFiles);
        });
    }

    @Override
    public void start() {
        this.setVisible(true);
    }

}



