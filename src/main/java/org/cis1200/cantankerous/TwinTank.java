package org.cis1200.cantankerous;

import java.awt.*;
import java.util.List;

public class TwinTank extends Tank {
    private Color blue = new Color(0, 178, 225);
    private Color darkBlue = new Color(2, 133, 167);
    private Color lightGray = new Color(153, 153, 153);
    private Color darkGray = new Color(114, 114, 114);

    private boolean fireLeftTurret = true; // toggle for alternating shots

    public TwinTank(int px, int py, int courtWidth, int courtHeight) {
        super(px, py, courtWidth, courtHeight);
        // Only set different base stats if needed
        // e.g., if you want TwinTank to start slightly faster or stronger
        // setBaseBulletSpeed(6);
        // setBaseRecoilStrength(0.8);
        // setBaseBulletPenetration(1);
        // setBaseFireRate(10);

        updateFireRate();
    }

    @Override
    public void draw(Graphics g, double camX, double camY) {
        Graphics2D g2 = (Graphics2D) g;

        int baseX = (int)(getPx() - camX);
        int baseY = (int)(getPy() - camY);

        int cx = baseX + getWidth() / 2;
        int cy = baseY + getHeight() / 2;

        var old = g2.getTransform();
        g2.rotate(angleRad, cx, cy);

        // === LOCAL COORD SYSTEM ===
        // local X = forward
        // local Y = left/right

        double rightLocalY = 6;          // left/right
        double forwardOffset = 10;         // NEW: push turrets forward

        int turretLength = getWidth();
        int turretHeight = getHeight() - 16;

        // Local coordinates
        double leftLocalX  =  forwardOffset - turretLength / 2.0;
        double rightLocalX =  forwardOffset - turretLength / 2.0;

        double localUp = -turretHeight / 2.0;
        double leftLocalY  = -rightLocalY;

        // Convert local → world
        int leftDrawX =  (int)(cx + leftLocalX);
        int leftDrawY =  (int)(cy + localUp + leftLocalY);

        int rightDrawX = (int)(cx + rightLocalX);
        int rightDrawY = (int)(cy + localUp + rightLocalY);

        // Draw both turrets
        g2.setColor(lightGray);
        g2.fillRect(leftDrawX,  leftDrawY,  turretLength, turretHeight);
        g2.fillRect(rightDrawX, rightDrawY, turretLength, turretHeight);

        g2.setColor(darkGray);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(leftDrawX,  leftDrawY,  turretLength, turretHeight);
        g2.drawRect(rightDrawX, rightDrawY, turretLength, turretHeight);

        // Draw body
        g2.setColor(blue);
        g2.fillOval(baseX, baseY, getWidth(), getHeight());

        g2.setColor(darkBlue);
        g2.drawOval(baseX, baseY, getWidth(), getHeight());

        g2.setTransform(old);
    }


    @Override
    public void fire(List<Bullet> bullets) {
        double dirX = Math.cos(angleRad);
        double dirY = Math.sin(angleRad);

        double perpX = -Math.sin(angleRad);
        double perpY = Math.cos(angleRad);

        double spacing = 4;
        double barrelDistance = getWidth()/2.0 + 10;

        // Compute turret tips
        Point leftTip = new Point(
                (int)(getPx() + getWidth()/2  + perpX * -spacing + dirX * barrelDistance),
                (int)(getPy() + getHeight()/2 + perpY * -spacing + dirY * barrelDistance)
        );

        Point rightTip = new Point(
                (int)(getPx() + getWidth()/2  + perpX * spacing + dirX * barrelDistance),
                (int)(getPy() + getHeight()/2 + perpY * spacing + dirY * barrelDistance)
        );

        Point tip = fireLeftTurret ? leftTip : rightTip;

        // --- Apply random spread ---
        double spreadRad = Math.toRadians(bulletSpread);
        double randomOffset = (Math.random() * spreadRad) - (spreadRad / 2.0); // -spread/2 to +spread/2
        double angleWithSpread = angleRad + randomOffset;

        double finalDirX = Math.cos(angleWithSpread);
        double finalDirY = Math.sin(angleWithSpread);

        // --- Recoil ---
        double currentRecoil = recoilStrength; // can be upgraded too if you have a multiplier
        applyForce(-finalDirX * currentRecoil, -finalDirY * currentRecoil);

        double currentBulletSpeed = getCurrentBulletSpeed();
        int currentBulletPen = getCurrentBulletPenetration();

        bullets.add(new Bullet(
                currentBulletSpeed * finalDirX,
                currentBulletSpeed * finalDirY,
                tip.x - Bullet.SIZE / 2.0,
                tip.y - Bullet.SIZE / 2.0,
                GameCourt.COURT_WIDTH,
                GameCourt.COURT_HEIGHT,
                Color.GREEN,
                currentBulletPen
        ));


        fireLeftTurret = !fireLeftTurret; // alternate next shot
    }


    @Override
    public String getName() {
        return "Twin Tank";
    }

    @Override
    public Tank[] getEvolutionOptions() {
        return new Tank[]{}; // no further evolution yet
    }
}
