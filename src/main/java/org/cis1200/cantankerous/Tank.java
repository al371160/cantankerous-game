package org.cis1200.cantankerous;

import java.awt.*;
import java.util.List;

public abstract class Tank extends GameObj {

    protected double angleDeg = 0;
    protected double angleRad = 0;

    protected int health = 100;
    protected int level = 1;
    protected int xp = 0;
    protected int upgradePoints = 0;

    protected int maxHealth = 750;
    protected int healthRegen = 1;
    protected double bulletSpeed = 3;
    protected double bodyDamage = 1;
    protected double bulletDamage = 20;
    protected double recoilStrength = 0.3;
    protected int bulletPenetration = 1;

    protected double movementSpeed = 3;

    protected double bulletSpread = 0;
    protected int fireRate = 18;

    // Multipliers for upgrades
    protected double bulletSpeedMultiplier = 1.0;
    protected double bulletDamageMultiplier = 1.0;
    protected double bodyDamageMultiplier = 1.0;
    protected double movementSpeedMultiplier = 1.0;
    protected double healthRegenMultiplier = 1.0;
    protected double maxHealthMultiplier = 1.0;
    protected double bulletPenetrationMultiplier = 1.0;
    protected double fireRateMultiplier = 1.0;

    public Tank(int px, int py, int courtWidth, int courtHeight) {
        super(0, 0, px, py, 30, 30, courtWidth, courtHeight);
        updateFireRate();
    }

    // ===== Mouse Tracking =====
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
        double turretLength = getWidth() / 2.0 + 10;
        double tipX = cx + turretLength * Math.cos(angleRad);
        double tipY = cy + turretLength * Math.sin(angleRad);
        return new Point((int) tipX, (int) tipY);
    }

    //parent classes
    public abstract void draw(Graphics g, double camX, double camY);
    public abstract void fire(List<Bullet> bullets);
    public abstract String getName();
    public abstract Tank[] getEvolutionOptions();

    // ===== Copy State =====
    public void copyStateFrom(Tank oldTank) {

        this.setHealth(oldTank.getHealth());
        this.level = oldTank.level;
        this.xp = oldTank.xp;
        this.upgradePoints = oldTank.upgradePoints;
        this.bulletSpeed = oldTank.bulletSpeed;
        this.bodyDamage = oldTank.bodyDamage;
        this.bulletDamage = oldTank.bulletDamage;
        this.recoilStrength = oldTank.recoilStrength;
        this.bulletPenetration = oldTank.bulletPenetration;

        this.bulletSpeedMultiplier = oldTank.bulletSpeedMultiplier;
        this.bodyDamageMultiplier = oldTank.bodyDamageMultiplier;
        this.bulletDamageMultiplier = oldTank.bulletDamageMultiplier;
        this.movementSpeedMultiplier = oldTank.movementSpeedMultiplier;
        this.healthRegenMultiplier = oldTank.healthRegenMultiplier;
        this.maxHealthMultiplier = oldTank.maxHealthMultiplier;
        this.bulletPenetrationMultiplier = oldTank.bulletPenetrationMultiplier;
        this.fireRateMultiplier = oldTank.fireRateMultiplier;
    }




    public boolean isDead() {
        return health <= 0;
    }

    // ===== Upgrade Methods =====
    public void upgradeMovementSpeed(double factor) {
        movementSpeedMultiplier *= factor;
        this.maxSpeed = getCurrentMovementSpeed();
    }

    public void upgradeBulletSpeed(double factor) {
        bulletSpeedMultiplier *= factor;
    }

    public void upgradeBulletDamage(double factor) {
        bulletDamageMultiplier *= factor;
    }

    public void upgradeBodyDamage(double factor) {
        bodyDamageMultiplier *= factor;
    }

    public void upgradeMaxHealth(double factor) {
        maxHealthMultiplier *= factor;
    }

    public void upgradeBulletPenetration(double factor) {
        bulletPenetrationMultiplier *= factor;
    }

    public void upgradeFireRate(double factor) {
        fireRateMultiplier *= factor;
        updateFireRate();
    }

    public void upgradeHealthRegen(double factor) {
        healthRegenMultiplier *= factor;
    }

    // ===== Get Current Values =====
    public double getCurrentHealthRegen() {
        return healthRegen*healthRegenMultiplier;
    }
    public double getCurrentMovementSpeed() {
        return movementSpeed * movementSpeedMultiplier;
    }

    public double getCurrentBulletSpeed() {
        return bulletSpeed * bulletSpeedMultiplier;
    }

    public int getCurrentBulletDamage() {
        return (int)(bulletDamage * bulletDamageMultiplier);
    }

    public double getCurrentBodyDamage() {
        return bodyDamage * bodyDamageMultiplier;
    }

    public int getCurrentMaxHealth() {
        return (int)(maxHealth * maxHealthMultiplier);
    }

    public int getCurrentBulletPenetration() {
        return (int)(bulletPenetration * bulletPenetrationMultiplier);
    }

    public int getCurrentFireRate() {
        return Math.max(1, (int)(fireRate * fireRateMultiplier));
    }

    public void updateFireRate() {
        fireRate = getCurrentFireRate();
    }
}
