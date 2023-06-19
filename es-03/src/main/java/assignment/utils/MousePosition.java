package assignment.utils;

import java.io.Serializable;

public class MousePosition implements Serializable {
    public final int x;
    public final int y;
    public String clientId;

    public MousePosition(String clientId, int x, int y) {
        this.clientId = clientId;
        this.x = x;
        this.y = y;
    }
}
