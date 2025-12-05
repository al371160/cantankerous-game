package org.cis1200.cantankerous;

import java.awt.*;

/**
 * A basic game object starting in the upper left corner of the game court. It
 * is displayed as a circle of a specified color.
 */
public class Bullet extends GameObj {
    public static final int SIZE = 15;
    public static final int INIT_POS_X = 170;
    public static final int INIT_POS_Y = 170;
    public static final double INIT_VEL_X = 2;
    public static final double INIT_VEL_Y = 3;

    final private Color color;


    Color blue = new Color(0, 178, 225);
    Color darkBlue = new Color(2, 133, 167);

    public Bullet(double velX, double velY, double posX, double posY, int courtWidth, int courtHeight, Color color) {
        super(velX, velY, posX, posY, SIZE, SIZE, courtWidth, courtHeight);

        this.color = color;
    }


    public void draw(Graphics g, double camX, double camY) {
        int x = (int) (getPx() - camX);
        int y = (int) (getPy() - camY);
        int w = getWidth();
        int h = getHeight();

        // fill bullet
        g.setColor(blue);
        g.fillOval(x, y, w, h);

        // draw outline
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(darkBlue);          // outline color
        g2.setStroke(new BasicStroke(2)); // 2-pixel thick
        g2.drawOval(x, y, w, h);
    }


}