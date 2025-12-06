package org.cis1200.cantankerous;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class MachineGunTank extends Tank {
    private final Color blue = new Color(0, 178, 225);
    private final Color darkBlue = new Color(2, 133, 167);
    private final Color lightGray = new Color(153, 153, 153);
    private final Color darkGray = new Color(114, 114, 114);

    private final Random rand = new Random();

    public MachineGunTank(int px, int py, int courtWidth, int courtHeight) {
        super(px, py, courtWidth, courtHeight);
        this.bulletSpread = 15;   // ±7.5° spread
        this.fireRate = 2;        // very high fire rate (ticks between shots)
        this.bulletSpeed = 8;     // slightly faster bullets
        this.recoilStrength = 0.5;
        this.bulletPenetration = 1;
    }

    @Override
    public void draw(Graphics g, double camX, double camY) {
        Graphics2D g2 = (Graphics2D) g;

        int cx = (int) (getPx() - camX + getWidth() / 2.0);
        int cy = (int) (getPy() - camY + getHeight() / 2.0);

        // Save old transform
        var old = g2.getTransform();
        g2.rotate(angleRad, cx, cy);

        // ---------- Draw Trapezoidal Turret ----------
        int turretLength = getWidth();
        int turretHeight = getHeight() - 12;
        int topWidth = turretLength - 8;  // top is narrower
        int bottomWidth = turretLength;   // bottom full width

        Polygon turret = new Polygon();
        turret.addPoint(cx - bottomWidth / 2+10, cy - turretHeight / 2); // bottom-left
        turret.addPoint(cx + bottomWidth / 2+10, cy - turretHeight / 2); // bottom-right
        turret.addPoint(cx + topWidth / 2+10, cy + turretHeight / 2);    // top-right
        turret.addPoint(cx - topWidth / 2+10, cy + turretHeight / 2);    // top-left

        g2.setColor(lightGray);
        g2.fillPolygon(turret);
        g2.setColor(darkGray);
        g2.setStroke(new BasicStroke(2));
        g2.drawPolygon(turret);

        // ---------- Draw Tank Body ----------
        g2.setColor(blue);
        g2.fillOval((int)(getPx() - camX), (int)(getPy() - camY), getWidth(), getHeight());
        g2.setColor(darkBlue);
        g2.setStroke(new BasicStroke(2));
        g2.drawOval((int)(getPx() - camX), (int)(getPy() - camY), getWidth(), getHeight());

        // Restore old transform
        g2.setTransform(old);
    }

    @Override
    public void fire(List<Bullet> bullets) {
        Point tip = getTurretTip();

        // Apply random spread
        double spreadRad = Math.toRadians(rand.nextDouble() * bulletSpread - bulletSpread / 2.0);
        double velX = bulletSpeed * Math.cos(angleRad + spreadRad);
        double velY = bulletSpeed * Math.sin(angleRad + spreadRad);

        Bullet b = new Bullet(
                velX,
                velY,
                tip.x - Bullet.SIZE / 2.0,
                tip.y - Bullet.SIZE / 2.0,
                GameCourt.COURT_WIDTH,
                GameCourt.COURT_HEIGHT,
                Color.BLUE,
                bulletPenetration
        );

        bullets.add(b);
        b.maxSpeed = bulletSpeed;

        // Apply recoil to tank
        this.applyForce(-velX * recoilStrength, -velY * recoilStrength);
    }

    @Override
    public String getName() {
        return "Machine Gun Tank";
    }

    @Override
    public Tank[] getEvolutionOptions() {
        return new Tank[]{

        };
    }
}
