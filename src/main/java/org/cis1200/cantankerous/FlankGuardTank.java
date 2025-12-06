package org.cis1200.cantankerous;

import java.awt.*;
import java.util.List;

public class FlankGuardTank extends Tank {
    private Color blue = new Color(0, 178, 225);
    private Color darkBlue = new Color(2, 133, 167);
    private Color lightGray = new Color(153, 153, 153);
    private Color darkGray = new Color(114, 114, 114);

    private boolean fireLeftTurret = true; // toggle for alternating shots

    public FlankGuardTank(int px, int py, int courtWidth, int courtHeight) {
        super(px, py, courtWidth, courtHeight);
        this.bulletPenetration = 1;
        this.recoilStrength = 0.6;
        this.bulletSpeed = 6;
    }

    @Override
    public void draw(Graphics g, double camX, double camY) {
        Graphics2D g2 = (Graphics2D) g;

        int baseX = (int)(getPx() - camX);
        int baseY = (int)(getPy() - camY);

        int cx = baseX + getWidth() / 2;
        int cy = baseY + getHeight() / 2;

        var old = g2.getTransform();

        // rotate whole tank
        g2.rotate(angleRad, cx, cy);

        // === LOCAL TURRET OFFSETS ===
        double turretSpacing = 12; // distance left/right from center
        int turretLength = getWidth() - 5;
        int turretHeight = getHeight() - 16;

        // LOCAL positions relative to tank center
        double leftLocalX = -turretSpacing;
        double rightLocalX = +turretSpacing;
        double turretLocalY = -turretHeight / 2.0;

        // convert local → world coords AFTER rotation (but before drawing)
        int leftDrawX = (int)(cx + leftLocalX - turretLength/2.0);
        int rightDrawX = (int)(cx + rightLocalX - turretLength/2.0);
        int drawY = (int)(cy + turretLocalY);

        // ---- Draw turrets ----
        g2.setColor(lightGray);
        g2.fillRect(leftDrawX, drawY, turretLength, turretHeight);
        g2.fillRect(rightDrawX, drawY, turretLength, turretHeight);

        g2.setColor(darkGray);
        g2.setStroke(new BasicStroke(2));
        g2.drawRect(leftDrawX, drawY, turretLength, turretHeight);
        g2.drawRect(rightDrawX, drawY, turretLength, turretHeight);

        // ---- Draw body ----
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
        double perpY =  Math.cos(angleRad);

        double spacing = 6;
        double barrelDistance = getWidth()/2.0 + 10;

        // --- turret tips ---
        Point leftTip = new Point(
                (int)(getPx() + getWidth()/2  + perpX * -spacing + dirX * barrelDistance),
                (int)(getPy() + getHeight()/2 + perpY * -spacing + dirY * barrelDistance)
        );

        Point rightTip = new Point(
                (int)(getPx() + getWidth()/2  + perpX * spacing - dirX * barrelDistance),
                (int)(getPy() + getHeight()/2 + perpY * spacing - dirY * barrelDistance)
        );

        // which turret fires
        if (fireLeftTurret) {
            // LEFT → forward
            bullets.add(new Bullet(
                    bulletSpeed * dirX,
                    bulletSpeed * dirY,
                    leftTip.x - Bullet.SIZE/2.0,
                    leftTip.y - Bullet.SIZE/2.0,
                    GameCourt.COURT_WIDTH,
                    GameCourt.COURT_HEIGHT,
                    Color.GREEN,
                    this.bulletPenetration
            ));
        } else {
            // RIGHT → backward (reverse direction)
            bullets.add(new Bullet(
                    bulletSpeed * -dirX,
                    bulletSpeed * -dirY,
                    rightTip.x - Bullet.SIZE/2.0,
                    rightTip.y - Bullet.SIZE/2.0,
                    GameCourt.COURT_WIDTH,
                    GameCourt.COURT_HEIGHT,
                    Color.GREEN,
                    this.bulletPenetration
            ));
        }

        fireLeftTurret = !fireLeftTurret;
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
