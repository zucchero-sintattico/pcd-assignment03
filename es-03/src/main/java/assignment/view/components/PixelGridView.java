package assignment.view.components;

import assignment.controller.Controller;
import assignment.utils.PixelGrid;
import assignment.view.listeners.ColorChangeListener;
import assignment.view.listeners.MouseMovedListener;
import assignment.view.listeners.PixelGridEventListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class PixelGridView extends JFrame {

	private final Controller controller;
	private final BrushManager brushManager;
    private final VisualiserPanel panel;
    private final PixelGrid grid;
    private final int w, h;
    
    public PixelGridView(Controller controller, BrushManager brushManager, int w, int h){
		this.controller = controller;
		this.brushManager = brushManager;
		this.grid = controller.getGrid();
		this.w = w;
		this.h = h;
		setTitle(".:: PixelArt ::.");
		setResizable(false);
        panel = new VisualiserPanel(grid, brushManager, w, h);
        panel.addMouseListener(createMouseListener());
		panel.addMouseMotionListener(createMotionListener());
		var colorChangeButton = new JButton("Change color");
		colorChangeButton.addActionListener(e -> {
			var color = JColorChooser.showDialog(this, "Choose a color", Color.BLACK);
			if (color != null) {
				controller.updateUserColor(color.getRGB());
			}
		});
		// add panel and a button to the button to change color
		add(panel, BorderLayout.CENTER);
		add(colorChangeButton, BorderLayout.SOUTH);
        getContentPane().add(panel);
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				controller.leave();
			}
		});
		hideCursor();
    }
    
    public void refresh(){
        panel.repaint();
    }
        
    public void display() {
		SwingUtilities.invokeLater(() -> {
			this.pack();
			this.setVisible(true);
		});
    }

	public void setGrid(PixelGrid grid) {
		panel.setGrid(grid);
	}

	private void hideCursor() {
		var cursorImage = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		var blankCursor = Toolkit.getDefaultToolkit()
				.createCustomCursor(cursorImage, new Point(0, 0), "blank cursor");
		// Set the blank cursor to the JFrame.
		this.getContentPane().setCursor(blankCursor);
	}


	private MouseListener createMouseListener () {
		return new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int dx = w / grid.getNumColumns();
				int dy = h / grid.getNumRows();
				int col = e.getX() / dx;
				int row = e.getY() / dy;
				final int color = controller.getUserColor();
				controller.updatePixel(col, row, color);
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
		};
	}

	private MouseMotionListener createMotionListener() {
		return new MouseMotionListener() {

			int amountOfEvents = 0;
			int debounce = 1;

			@Override
			public void mouseDragged(MouseEvent e) {}

			@Override
			public void mouseMoved(MouseEvent e) {
				amountOfEvents = (amountOfEvents + 1) % debounce;
				if (amountOfEvents == 0) {
					controller.updateMousePosition(e.getX(), e.getY());
				}
			}
		};
	}

}
