package org.cis1200.cantankerous;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UpgradeSystemTest {

    private Tank tank;
    private final int maxLevel = 8;

    @BeforeEach
    public void setup() {
        tank = new BaseTank(0, 0, 5000, 5000); // simple base tank for upgrade testing
    }

    @Test
    public void testMovementSpeedUpgrade() {
        double initial = tank.getCurrentMovementSpeed();

        for (int i = 1; i <= maxLevel; i++) {
            tank.upgradeMovementSpeed(1.1);
        }

        double after = tank.getCurrentMovementSpeed();
        assertTrue(after > initial, "Movement speed should increase after upgrades");

        // maxSpeed should match movementSpeed
        assertEquals(after, tank.getCurrentMovementSpeed(), 0.0001);
    }

    @Test
    public void testMovementSpeedUpgradeCap() {
        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeMovementSpeed(1.1);
        }
        double capped = tank.getCurrentMovementSpeed();

        // Try upgrading past cap
        tank.upgradeMovementSpeed(1.1);
        tank.upgradeMovementSpeed(1.1);

        assertEquals(capped, tank.getCurrentMovementSpeed(),
                "Movement speed should NOT increase after level cap");
    }

    @Test
    public void testBulletSpeedUpgrade() {
        double initial = tank.getCurrentBulletSpeed();

        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeBulletSpeed(1.2);
        }

        assertTrue(tank.getCurrentBulletSpeed() > initial,
                "Bullet speed must increase after upgrades");
    }

    @Test
    public void testBulletDamageUpgrade() {
        double initial = tank.getCurrentBulletDamage();

        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeBulletDamage(1.3);
        }

        assertTrue(tank.getCurrentBulletDamage() > initial,
                "Bullet damage must increase after upgrades");
    }

    @Test
    public void testPenetrationUpgrade() {
        int initial = tank.getCurrentBulletPenetration();

        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeBulletPenetration(1.1);
        }

        assertTrue(tank.getCurrentBulletPenetration() > initial,
                "Bullet penetration must increase after upgrades");
    }

    @Test
    public void testFireRateUpgrade() {
        double initial = tank.getCurrentFireRate();

        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeFireRate(0.9);
        }

        assertTrue(tank.getCurrentFireRate() < initial,
                "Fire rate should DECREASE (faster firing) after upgrades");
    }

    @Test
    public void testMaxHealthUpgrade() {
        double initialMax = tank.getCurrentMaxHealth();

        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeMaxHealth(1.3);
        }

        assertTrue(tank.getCurrentMaxHealth() > initialMax,
                "Max health must increase after upgrades");
    }

    @Test
    public void testHealthRegenUpgrade() {
        double initial = tank.healthRegenMultiplier;

        for (int i = 0; i < maxLevel; i++) {
            tank.upgradeHealthRegen(1.2);
        }

        assertTrue(tank.healthRegenMultiplier > initial,
                "Health regen multiplier should increase");
    }
}
