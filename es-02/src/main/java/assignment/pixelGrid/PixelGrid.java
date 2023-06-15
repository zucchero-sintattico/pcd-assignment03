package assignment.pixelGrid;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class PixelGrid {
	private final int nRows;
	private final int nColumns;
	private final int[][] grid;
	private static Channel channel;
	private static Connection connection;
	
	public PixelGrid(final int nRows, final int nColumns) throws IOException, TimeoutException {
		// MQTT data
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		this.connection = factory.newConnection();
		this.channel = connection.createChannel();


		this.nRows = nRows;
		this.nColumns = nColumns;
		grid = new int[nRows][nColumns];
	}

	public void clear() {
		for (int i = 0; i < nRows; i++) {
			Arrays.fill(grid[i], 0);
		}
	}
	
	public void set(final int x, final int y, final int color) {

		grid[y][x] = color;

		try {
			channel.queueDeclare("Pixels", false, false, false, null);
			String message = "Grid[" + y +"][" + x + "]: " + color;
			channel.basicPublish("", "Pixels", null, message.getBytes());
			System.out.println(" [*] Sent '" + message + "'");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}


	}
	
	public int get(int x, int y) {
		return grid[y][x];
	}
	
	public int getNumRows() {
		return this.nRows;
	}
	

	public int getNumColumns() {
		return this.nColumns;
	}
	
}
