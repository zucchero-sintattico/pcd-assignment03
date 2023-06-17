package assignment.pixelGrid;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class PixelArtMain {



	public static void main(String[] args) throws IOException, TimeoutException {
		PixelArtNode node = new PixelArtNode();
		node.start();
	}

}
