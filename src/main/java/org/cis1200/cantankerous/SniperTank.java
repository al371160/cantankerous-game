package org.cis1200.cantankerous;

import java.awt.*;
import java.util.List;

public class SniperTank extends Tank {
    Color blue = new Color(0, 178, 225);
    Color darkBlue = new Color(2, 133, 167);
    Color lightGray = new Color(153, 153, 153);
    Color darkGray = new Color(114, 114, 114);

    public SniperTank(int px, int py, int courtWidth, int courtHeight) {
        super(px, py, courtWidth, courtHeight);
        this.bulletSpeed = 10; // faster bullets
        this.recoilStrength = 1;
        this.bulletPenetration = 3; // can pierce more
    }

    public void draw(Graphics g, double camX, double camY) {
        Graphics2D g2 = (Graphics2D) g;

        int cx = (int) (getPx() - camX + getWidth() / 2.0);
        int cy = (int) (getPy() - camY + getHeight() / 2.0);

        var old = g2.getTransform();
        g2.rotate(Math.toRadians(angleDeg), cx, cy);

        // ---------- Draw Long Turret ----------
        int turretWidth = getHeight() - 12;
        int turretX = (int) (getPx() - camX + 15);
        int turretY = (int) (getPy() - camY + 6);
        g2.setColor(lightGray);
        g2.fillRect(turretX, turretY, getWidth() + 15, turretWidth); // longer barrel

        g2.setColor(darkGray);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(turretX, turretY, getWidth() + 15, turretWidth);

        // ---------- Draw Tank Body ----------
        int bodyX = (int) (getPx() - camX);
        int bodyY = (int) (getPy() - camY);
        g2.setColor(blue); // red body
        g2.fillOval(bodyX, bodyY, getWidth(), getHeight());

        g2.setColor(darkBlue); // outline
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(bodyX, bodyY, getWidth(), getHeight());

        g2.setTransform(old);
    }

    @Override
    public void fire(List<Bullet> bullets) {
        Point tip = getTurretTip();
        double velX = bulletSpeed * Math.cos(angleRad);
        double velY = bulletSpeed * Math.sin(angleRad);

        applyForce(-velX * recoilStrength, -velY * recoilStrength);

        Bullet b = new Bullet(
                velX, velY,
                tip.x - Bullet.SIZE/2.0,
                tip.y - Bullet.SIZE/2.0,
                GameCourt.COURT_WIDTH,
                GameCourt.COURT_HEIGHT,
                Color.RED, this.bulletPenetration);

        b.maxSpeed = getCurrentBulletSpeed();
        bullets.add(b);
    }

    @Override
    public String getName() {
        return "Sniper Tank";
    }


    @Override
    public Tank[] getEvolutionOptions() {
        return new Tank[]{}; // placeholder
    }
}
