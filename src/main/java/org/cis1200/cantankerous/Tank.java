package org.cis1200.cantankerous;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Tank extends GameObj {

    private double angleDeg = 0; // tank's current facing direction in degrees

    public Tank(int px, int py, int courtWidth, int courtHeight) {
        super(0, 0, px, py, 40, 40, courtWidth, courtHeight);
    }



    /**
     * Update the tank's rotation to face the mouse.
     */
    public void trackMouse(MouseEvent e) {
        double cx = getPx() + getWidth() / 2.0;
        double cy = getPy() + getHeight() / 2.0;

        double dx = e.getX() - cx;
        double dy = e.getY() - cy;

        double angleRad = Math.atan2(dy, dx);
        angleDeg = Math.toDegrees(angleRad);

        if (angleDeg < 0) {
            angleDeg += 360;
        }
    }

    public void shoot (MouseEvent e) {
        System.out.println("shoot");
    }

    /**
     * Draw the tank as a rotated rectangle with a turret line.
     */
    @Override
    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        int cx = getPx() + getWidth() / 2;
        int cy = getPy() + getHeight() / 2;

        // save old transform
        var old = g2.getTransform();

        // rotate graphics context around tank center
        g2.rotate(Math.toRadians(angleDeg), cx, cy);

        // draw the tank body
        g2.setColor(Color.CYAN);
        g2.fillOval(getPx(), getPy(), getWidth(), getHeight());

        // draw turret (a line projecting forward)
        g2.setColor(Color.GRAY);
        g2.fillRect(getPx()+30, getPy(), getWidth(), getHeight());

        // restore transform
        g2.setTransform(old);
    }

    public double getAngle() {
        return angleDeg;
    }
}
