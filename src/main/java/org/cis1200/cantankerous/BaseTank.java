package org.cis1200.cantankerous;

import java.awt.*;
import java.util.List;

public class BaseTank extends Tank {
    Color blue = new Color(0, 178, 225);
    Color darkBlue = new Color(2, 133, 167);
    Color lightGray = new Color(153, 153, 153);
    Color darkGray = new Color(114, 114, 114);

    public BaseTank(int px, int py, int courtWidth, int courtHeight) {
        super(px, py, courtWidth, courtHeight);
        fireRate = 18;
        fireRateMultiplier = 1;
        updateFireRate();
    }

    @Override
    public void draw(Graphics g, double camX, double camY) {
        Graphics2D g2 = (Graphics2D) g;

        int cx = (int) (getPx() - camX + getWidth() / 2.0);
        int cy = (int) (getPy() - camY + getHeight() / 2.0);

        // Save old transform
        var old = g2.getTransform();
        g2.rotate(Math.toRadians(angleDeg), cx, cy);

        // ---------- Draw Turret ----------
        int turretWidth = getHeight() - 12;
        int turretX = (int) (getPx() - camX + 15);  // adjust for rectangle placement
        int turretY = (int) (getPy() - camY + 6);
        int turretLength = getWidth();  // can make longer for sniper tank

        g2.setColor(Color.GRAY);
        g2.fillRect(turretX, turretY, turretLength, turretWidth);

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(turretX, turretY, turretLength, turretWidth);

        // ---------- Draw Tank Body ----------
        g2.setColor(new Color(0, 178, 225));
        g2.fillOval((int)(getPx() - camX), (int)(getPy() - camY), getWidth(), getHeight());

        g2.setColor(new Color(2, 133, 167));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval((int)(getPx() - camX), (int)(getPy() - camY), getWidth(), getHeight());

        // Restore old transform
        g2.setTransform(old);
    }


    @Override
    public void fire(List<Bullet> bullets) {
        Point tip = getTurretTip();
        double velX = bulletSpeed * Math.cos(angleRad);
        double velY = bulletSpeed * Math.sin(angleRad);

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

        applyForce(-velX * recoilStrength, -velY * recoilStrength);
        bullets.add(b);
        b.maxSpeed = bulletSpeed;

        // Apply recoil to tank
        this.applyForce(-velX * recoilStrength, -velY * recoilStrength);
    }

    @Override
    public String getName() {
        return "Base Tank";
    }

    @Override
    public Tank[] getEvolutionOptions() {
        return new Tank[]{
                new TwinTank(getPx(), getPy(), GameCourt.COURT_WIDTH, GameCourt.COURT_HEIGHT),
                new SniperTank(getPx(), getPy(), GameCourt.COURT_WIDTH, GameCourt.COURT_HEIGHT),
                new MachineGunTank(getPx(), getPy(), GameCourt.COURT_WIDTH, GameCourt.COURT_HEIGHT),
                new FlankGuardTank(getPx(), getPy(), GameCourt.COURT_WIDTH, GameCourt.COURT_HEIGHT)
        };
    }
}
