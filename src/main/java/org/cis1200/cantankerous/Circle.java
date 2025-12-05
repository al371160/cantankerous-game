package org.cis1200.cantankerous;

import java.awt.*;

public class Circle extends GameObj {
    public static final int SIZE = 20;
    public static final int INIT_POS_X = 170;
    public static final int INIT_POS_Y = 170;
    public static final int INIT_VEL_X = 2;
    public static final int INIT_VEL_Y = 3;

    private final Color color;


    public Circle(int courtWidth, int courtHeight, Color color) {
        super(INIT_VEL_X, INIT_VEL_Y, INIT_POS_X, INIT_POS_Y, SIZE, SIZE, courtWidth, courtHeight);
        this.color = color;
    }

    @Override
    public void draw(Graphics g, double camX, double camY) {
        g.setColor(this.color);
        g.fillOval((int) (this.getPx() - camX), (int) (this.getPy() - camY), this.getWidth(), this.getHeight());
    }
}
