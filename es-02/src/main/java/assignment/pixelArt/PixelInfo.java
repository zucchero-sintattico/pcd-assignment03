package assignment.pixelArt;

public class PixelInfo {
    private final int x;
    private final int y;
    private final int color;

    public PixelInfo(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getColor() {
        return color;
    }
}
