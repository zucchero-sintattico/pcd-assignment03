package assignment.utils;

import java.io.Serializable;

public class PixelInfo implements Serializable {
    public final int x;
    public final int y;
    public final int color;

    public PixelInfo(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }
}
