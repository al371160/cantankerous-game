package org.cis1200.cantankerous;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Tank extends GameObj {

    public double angleDeg = 0; // tank's current facing direction in degrees
    public double angleRad = 0;

    public double cx;
    public double cy;

    Color blue = new Color(0, 178, 225);
    Color darkBlue = new Color(2, 133, 167);
    Color lightGray = new Color(153, 153, 153);
    Color darkGray = new Color(114, 114, 114);


    public Tank(int px, int py, int courtWidth, int courtHeight) {
        super(0, 0, px, py, 30, 30, courtWidth, courtHeight);
    }


    public void trackMouse(int mx, int my, double camX, double camY) {
        double worldMouseX = mx + camX;
        double worldMouseY = my + camY;

        double cx = getPx() + getWidth() / 2.0;
        double cy = getPy() + getHeight() / 2.0;

        double dx = worldMouseX - cx;
        double dy = worldMouseY - cy;

        angleRad = Math.atan2(dy, dx);
        angleDeg = Math.toDegrees(angleRad);

        if (angleDeg < 0) angleDeg += 360;
    }


    public Point getTurretTip() {
        double cx = getPx() + getWidth() / 2.0;
        double cy = getPy() + getHeight() / 2.0;

        double turretLength = getWidth() / 2.0 + 10; // adjust to match drawing
        double tipX = cx + turretLength * Math.cos(angleRad);
        double tipY = cy + turretLength * Math.sin(angleRad);

        return new Point((int) tipX, (int) tipY);
    }

    /**
     * Draw the tank as a rotated rectangle with a turret line, camera-aware.
     */
    /**
     * Draw the tank as a rotated rectangle with a turret line, camera-aware, with outlines.
     */
    public void draw(Graphics g, double camX, double camY) {
        Graphics2D g2 = (Graphics2D) g;

        int cx = (int) (getPx() - camX + getWidth() / 2.0);
        int cy = (int) (getPy() - camY + getHeight() / 2.0);

        // save old transform
        var old = g2.getTransform();

        // rotate around tank center
        g2.rotate(Math.toRadians(angleDeg), cx, cy);

        // ---------- Draw Turret ----------
        int turretWidth = getHeight() - 12;
        int turretX = (int) (getPx() - camX + 20);
        int turretY = (int) (getPy() - camY + 6);

        g2.setColor(lightGray);
        g2.fillRect(turretX, turretY, getWidth() - 7, turretWidth);

        // outline for turret
        g2.setColor(darkGray);
        g2.setStroke(new BasicStroke(2)); // 2-pixel wide outline
        g2.drawRect(turretX, turretY, getWidth() - 7, turretWidth);

        // ---------- Draw Tank Body ----------
        int bodyX = (int) (getPx() - camX);
        int bodyY = (int) (getPy() - camY);

        g2.setColor(blue);
        g2.fillOval(bodyX, bodyY, getWidth(), getHeight());

        // outline for body
        g2.setColor(darkBlue);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(bodyX, bodyY, getWidth(), getHeight());

        // restore transform
        g2.setTransform(old);
    }
}
