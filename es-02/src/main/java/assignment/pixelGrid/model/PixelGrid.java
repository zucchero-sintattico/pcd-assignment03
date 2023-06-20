package assignment.pixelGrid.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeoutException;

public class PixelGrid {
    private final int nRows;
    private final int nColumns;
    private final int[][] grid;

    public PixelGrid(final int nRows, final int nColumns) throws IOException, TimeoutException {
        // MQTT data
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

    public static PixelGrid createFromString(final String gridString) throws IOException, TimeoutException {
        final String[] rows = gridString.split("\n");
        final int nRows = rows.length;
        final int nColumns = rows[0].split(",").length;
        final PixelGrid grid = new PixelGrid(nRows, nColumns);
        for (int y = 0; y < nRows; y++) {
            final String[] row = rows[y].split(",");
            for (int x = 0; x < nColumns; x++) {
                grid.set(x, y, Integer.parseInt(row[x]));
            }
        }
        return grid;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (int y = 0; y < nRows; y++) {
            for (int x = 0; x < nColumns; x++) {
                sb.append(grid[y][x]);
                if (x < nColumns - 1) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
