package org.cis1200.cantankerous;

import java.awt.*;

public class Bullet extends GameObj {
    public static final int SIZE = 15;

    public int penetration;     // how many objects it can hit
    public double lifetime;     // current remaining lifetime
    public double maxLifetime;  // calculated based on penetration

    Color blue = new Color(0, 178, 225);
    Color darkBlue = new Color(2, 133, 167);

    public Bullet(double vx, double vy, double px, double py,
                  int courtWidth, int courtHeight, Color color, int penetration) {
        super(vx, vy, px, py, SIZE, SIZE, courtWidth, courtHeight);
        this.penetration = penetration;
        this.maxLifetime = penetration * 67; // Example: more penetration = longer life
        this.lifetime = this.maxLifetime;
    }

    public void draw(Graphics g, double camX, double camY) {
        int x = (int) (getPx() - camX);
        int y = (int) (getPy() - camY);

        g.setColor(blue);
        g.fillOval(x, y, SIZE, SIZE);

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(darkBlue);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(x, y, SIZE, SIZE);
    }

}
