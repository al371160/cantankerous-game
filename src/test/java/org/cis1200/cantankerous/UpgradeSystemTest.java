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

    // -----------------------------------------------------------
    // MULTIPLICATIVE BEHAVIOR
    // -----------------------------------------------------------

    @Test
    public void testMultiplicativeMovementSpeedUpgrade() {
        double base = tank.getCurrentMovementSpeed();

        tank.upgradeMovementSpeed(1.5); // 1.5x
        tank.upgradeMovementSpeed(1.5); // another 1.5x

        // Expected = base * 1.5 * 1.5
        double expected = base * 2.25;

        assertEquals(expected, tank.getCurrentMovementSpeed(), 0.0001);
    }

    @Test
    public void testMultiplicativeBulletDamageUpgrade() {
        double base = tank.getCurrentBulletDamage(); // integer

        tank.upgradeBulletDamage(2.0);
        tank.upgradeBulletDamage(2.0);

        // integer output, so cast expected
        int expected = (int)(base * 4);

        assertEquals(expected, tank.getCurrentBulletDamage());
    }

    // -----------------------------------------------------------
    // FIRE RATE FLOORING (minimum 1)
    // -----------------------------------------------------------

    @Test
    public void testFireRateDoesNotGoBelowOne() {
        tank.upgradeFireRate(0.01); // extremely fast firing
        tank.upgradeFireRate(0.01);

        assertEquals(1, tank.getCurrentFireRate(),
                "Fire rate should never drop below 1 tick");
    }

    // -----------------------------------------------------------
    // MAX HEALTH UPGRADE DOES NOT CHANGE CURRENT HEALTH
    // -----------------------------------------------------------

    @Test
    public void testIncreasingMaxHealthDoesNotHealTank() {
        tank.setHealth(200); // lower the health

        int before = tank.getHealth();
        int maxBefore = tank.getCurrentMaxHealth();

        tank.upgradeMaxHealth(2.0);

        // maxHealth increased
        assertTrue(tank.getCurrentMaxHealth() > maxBefore);

        // BUT current health stays the same
        assertEquals(before, tank.getHealth(),
                "Current HP should NOT be healed by max health upgrades");
    }

    // -----------------------------------------------------------
    // PENETRATION UPGRADES WORK
    // -----------------------------------------------------------

    @Test
    public void testPenetrationUpgradeScaling() {
        int base = tank.getCurrentBulletPenetration();

        tank.upgradeBulletPenetration(3.0); // x3
        tank.upgradeBulletPenetration(2.0); // x2

        int expected = (int)(base * 3 * 2);

        assertEquals(expected, tank.getCurrentBulletPenetration());
    }

    @Test
    public void testBodyDamageUpgrade() {
        double base = tank.getCurrentBodyDamage();

        tank.upgradeBodyDamage(1.25);
        tank.upgradeBodyDamage(1.25);

        double expected = base * 1.25 * 1.25;

        assertEquals(expected, tank.getCurrentBodyDamage(), 0.0001);
    }

    // -----------------------------------------------------------
    // HEALTH REGEN MULTIPLIER
    // -----------------------------------------------------------

    @Test
    public void testHealthRegenUpgradeAffectsRegen() {
        double base = tank.getCurrentHealthRegen(); // = healthRegen * multiplier

        tank.upgradeHealthRegen(1.5);
        tank.upgradeHealthRegen(2.0);

        double expected = base * 1.5 * 2.0;

        assertEquals(expected, tank.getCurrentHealthRegen(), 0.0001);
    }

    @Test
    public void testMovementSpeedSynchronizesWithMaxSpeed() {
        double before = tank.maxSpeed;

        tank.upgradeMovementSpeed(2.0);

        assertEquals(tank.getCurrentMovementSpeed(), tank.maxSpeed,
                0.0001, "maxSpeed must always equal current movement speed");
    }


    @Test
    public void testCopyStateFromCopiesUpgrades() {
        Tank t1 = new BaseTank(0, 0, 5000, 5000);
        Tank t2 = new BaseTank(0, 0, 5000, 5000);

        // Apply upgrades to t1
        t1.upgradeBulletDamage(2.0);
        t1.upgradeMovementSpeed(1.5);
        t1.upgradeHealthRegen(3.0);
        t1.upgradeFireRate(0.5);

        // Copy to t2
        t2.copyStateFrom(t1);

        assertEquals(t1.getCurrentBulletDamage(), t2.getCurrentBulletDamage());
        assertEquals(t1.getCurrentMovementSpeed(), t2.getCurrentMovementSpeed());
        assertEquals(t1.getCurrentHealthRegen(), t2.getCurrentHealthRegen());
        assertEquals(t1.getCurrentFireRate(), t2.getCurrentFireRate());
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
