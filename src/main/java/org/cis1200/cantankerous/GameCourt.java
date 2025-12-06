package org.cis1200.cantankerous;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * GameCourt
 *
 * This class holds the primary game logic for how different objects interact
 * with one another. Take time to understand how the timer interacts with the
 * different methods and how it repaints the GUI on every tick().
 */
public class GameCourt extends JPanel {

    //CAMERA!!
    private double camX = 0;
    private double camY = 0;
    private double camSpeed = 0.1; //interpolate

    //UI variable
    private UI ui = new UI();
    // the state of the game logic
    //private Square square; // the Black Square, keyboard control
    private Tank tank;
    private Circle snitch; // the Golden Snitch, bounces
   //private Poison poison; // the Poison Mushroom, doesn't move

    private boolean playing = false; // whether the game is running
    private final JLabel status; // Current status text, i.e. "Running..."

    // Game constants
    public static final int COURT_WIDTH = 5000;
    public static final int COURT_HEIGHT = 3000;
    public static final int WINDOW_WIDTH = 960;
    public static final int WINDOW_HEIGHT = 540;
    //public static final int TANK_VELOCITY = 4;

    // Update interval for timer, in milliseconds
    public static final int INTERVAL = 35;

    //sussy input fix
    private boolean wDown = false;
    private boolean aDown = false;
    private boolean sDown = false;
    private boolean dDown = false;

    //physics
    double strength = 0.2; // acceleration strength
    double force = 2; // smooth acceleration strength; movementSpeed in object class, might need to be careful

    private int mouseX;
    private int mouseY;

    //spawned objects
    private java.util.List<Square> squares = new java.util.ArrayList<>();
    private java.util.List<Bullet> bullets = new java.util.ArrayList<>();

    //firing
    private boolean mouseDown = false;
    private int fireCooldown = 0;      // counts down each tick
    public double recoilStrength = 0.5; /** should be CHANGED PER TANK!!! */

    /** \/\/\/\/\/\/\/\/\/[    UPGRADES!!!!!    ]/\/\/\/\/\/\/\/\//\/\\/\/\/\/\*/
    // XP AND LEVELS
    private int xp = 0;
    private int xpToLevel = 100;
    private int upgradePoints = 0;    // points you can spend upgrading
    private int level = 1;            // player level
    //ITEM XP GAINS
    private int squareXP = 8000;
    //UPGRADE BAR
    private boolean showUpgradeBar = true;   // Q toggles this
    private static final int MAX_UPGRADE_LEVEL = 8;

    private String format(double d) {
        return String.format("%.2f", d);
    }



    /**
    //STATS
    public int healthRegen = 1;
    public int tankMaxHealth = 750;
    public double bodyDamage = 1;
    public double bulletSpeed = 5;
    public int bulletDamage = 30;
    public int bulletPenetration = 1;
    //public int fireRate = 10;           // ticks between bullets (≈350ms)
    // public int movementSpeed = 1; (this variable is unused as movement speed is upgraded in


    // Upgrade multipliers
    private final double healthRegenMultiplier = 1.2;
    private final double tankMaxHealthMultiplier = 1.3;
    private final double bodyDamageMultiplier = 1.1;
    private final double bulletSpeedMultiplier = 1.2;
    private final double bulletDamageMultiplier = 1.3;
    private final double bulletPenetrationMultiplier = 1.1;
    private final double fireRateMultiplier = 0.9; // lower is faster
    private final double movementSpeedMultiplier = 2;

     */
    //level trackers:
    private int lvlHealthRegen = 0;
    private int lvlTankMaxHealth = 0;
    private int lvlBodyDamage = 0;
    private int lvlBulletSpeed = 0;
    private int lvlBulletDamage = 0;
    private int lvlBulletPenetration = 0;
    private int lvlFireRate = 0;
    private int lvlMovementSpeed = 0;


    public GameCourt(JLabel status) {
        // creates border around the court area, JComponent method
        setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // The timer is an object which triggers an action periodically with the
        // given INTERVAL. We register an ActionListener with this timer, whose
        // actionPerformed() method is called each time the timer triggers. We
        // define a helper method called tick() that actually does everything
        // that should be done in a single time step.
        Timer timer = new Timer(INTERVAL, e -> tick());
        timer.start(); // MAKE SURE TO START THE TIMER!

        // Enable keyboard focus on the court area. When this component has the
        // keyboard focus, key events are handled by its key listener.
        setFocusable(true);

        // This key listener allows the square to move as long as an arrow key
        // is pressed, by changing the square's velocity accordingly. (The tick
        // method below actually moves the square.)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_Q -> showUpgradeBar = !showUpgradeBar;
                    case KeyEvent.VK_W -> wDown = true;
                    case KeyEvent.VK_A -> aDown = true;
                    case KeyEvent.VK_S -> sDown = true;
                    case KeyEvent.VK_D -> dDown = true;
                    case KeyEvent.VK_1 -> upgrade(1);
                    case KeyEvent.VK_2 -> upgrade(2);
                    case KeyEvent.VK_3 -> upgrade(3);
                    case KeyEvent.VK_4 -> upgrade(4);
                    case KeyEvent.VK_5 -> upgrade(5);
                    case KeyEvent.VK_6 -> upgrade(6);
                    case KeyEvent.VK_7 -> upgrade(7);
                    case KeyEvent.VK_8 -> upgrade(8);
                }

            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W -> wDown = false;
                    case KeyEvent.VK_A -> aDown = false;
                    case KeyEvent.VK_S -> sDown = false;
                    case KeyEvent.VK_D -> dDown = false;
                }

            }
        });


        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (tank != null) {
                    // convert screen coords to world coords using camera offset
                    tank.trackMouse(mouseX, mouseY,camX, camY);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (tank != null) {
                    // convert screen coords to world coords using camera offset
                    tank.trackMouse(mouseX, mouseY,camX, camY);
                }
            }
        });


        // shooting
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                mouseX = e.getX();
                mouseY = e.getY();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDown = false;
            }
        });



        //shooting
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                mouseDown = true;
                //fireBullet();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDown = false;
            }
        });


        this.status = status;
    }

    /**
     * (Re-)set the game to its initial state.
     */
    public void reset() {
        // Clear bullets
        bullets.clear();

        // Create tank first
        tank = new BaseTank(2500, 1500, COURT_WIDTH, COURT_HEIGHT);
        tank.setHealth(tank.getCurrentMaxHealth());


        // Create snitch
        snitch = new Circle(COURT_WIDTH, COURT_HEIGHT, Color.YELLOW);

        // Spawn squares
        spawnObjects();

        // Reset UI
        ui = new UI(); // fresh UI each game

        // Add health bars for squares
        for (Square sq : squares) {
            sq.setHealth(50); // ensure health is set
            ui.addHealthBar(new HealthBar(sq, 30));
        }

        // Add tank health bar
        ui.addHealthBar(new HealthBar(tank, 50));

        // Game state
        playing = true;
        status.setText("Running...");

        // Make sure the panel has keyboard focus
        requestFocusInWindow();
    }

    public void spawnObjects() {
        squares.clear();

        java.util.Random rand = new java.util.Random();

        int numSquares = 70; // change this to spawn more/less

        Rectangle tankRect = new Rectangle(
                tank.getPx(),
                tank.getPy(),
                tank.getWidth(),
                tank.getHeight()
        );

        for (int i = 0; i < numSquares; i++) {
            int x, y;

            // generate a random position not overlapping the tank
            do {
                x = rand.nextInt(COURT_WIDTH - Square.SIZE);
                y = rand.nextInt(COURT_HEIGHT - Square.SIZE);
            } while (tankRect.intersects(new Rectangle(x, y, Square.SIZE, Square.SIZE)));

            Square sq = new Square(COURT_WIDTH, COURT_HEIGHT, Color.YELLOW);


            // place square manually
            sq.setPx(x);
            sq.setPy(y);
            //set square health
            sq.setHealth(50);


            squares.add(sq);

        }
    }

    public void applyMovementForces() {
        if (wDown) tank.applyForce(0, -force * tank.getCurrentMovementSpeed());
        if (sDown) tank.applyForce(0, force * tank.getCurrentMovementSpeed());
        if (aDown) tank.applyForce(-force * tank.getCurrentMovementSpeed(), 0);
        if (dDown) tank.applyForce(force * tank.getCurrentMovementSpeed(), 0);
    }


    public void applyRepulsion(GameObj a, GameObj b, double strength) {
        double ax = a.getPx() + a.getWidth() / 2.0;
        double ay = a.getPy() + a.getHeight() / 2.0;

        double bx = b.getPx() + b.getWidth() / 2.0;
        double by = b.getPy() + b.getHeight() / 2.0;

        double dx = ax - bx;
        double dy = ay - by;

        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist == 0) dist = 0.01;

        // Normalize the direction
        double nx = dx / dist;
        double ny = dy / dist;

        // Apply opposite forces
        a.applyForce(nx * strength, ny * strength);
        b.applyForce(-nx * strength, -ny * strength);
    }


    /**
     * This method is called every time the timer defined in the constructor
     * triggers.
     */
    void tick() {
        if (!playing) return;

        // --- Camera: center on tank ---
        double targetCamX = tank.getPx() - WINDOW_WIDTH / 2.0 + tank.getWidth() / 2.0;
        double targetCamY = tank.getPy() - WINDOW_HEIGHT / 2.0 + tank.getHeight() / 2.0;
        camX += (targetCamX - camX) * camSpeed;
        camY += (targetCamY - camY) * camSpeed;

        // --- Movement & input ---
        applyMovementForces();
        tank.move(strength);
        tank.trackMouse(mouseX, mouseY, camX, camY); // mouse in world coords
        snitch.move(0);

        // --- Bullet interactions ---
        java.util.List<Bullet> toRemoveBullets = new java.util.ArrayList<>();
        java.util.List<Square> toRemoveSquares = new java.util.ArrayList<>();

        // --- Shooting ---
        if (mouseDown && fireCooldown <= 0) {
            tank.fire(bullets);
            fireCooldown = tank.fireRate; // use tank-specific fire rate
        }

        if (fireCooldown > 0) fireCooldown--;

        for (Bullet bullet : bullets) {
            bullet.move(0);
            bullet.lifetime--;
            if (bullet.lifetime <= 0) {
                toRemoveBullets.add(bullet);
            }
        }

        // --- Squares interactions ---
        for (GameObj a : squares) {
            for (GameObj b : squares) {
                if (a != b && a.intersects(b)) applyRepulsion(a, b, 1);
            }
        }


        for (Bullet bullet : bullets) {
            for (Square sq : squares) {
                if (bullet.intersects(sq)) {
                    applyRepulsion(bullet, sq, 2);
                    sq.takeDamage(tank.getCurrentBulletDamage());
                    bullet.penetration--;



                    if (sq.isDead()) {
                        // remove health bar
                        HealthBar hb = ui.getHealthBarFor(sq);
                        if (hb != null) ui.removeHealthBar(hb);
                        toRemoveSquares.add(sq);

                        // award XP
                        gainXP(squareXP);   // you can tune the amount
                    }

                }
            }


        }


        // --- Snitch bounces ---
        snitch.bounce(snitch.hitWall());
        for (Square sq : squares) snitch.bounce(snitch.hitObj(sq));

        // --- Tank collisions ---
        for (Square sq : squares) {
            if (tank.intersects(sq)) {
                applyRepulsion(sq, tank, 5);
                sq.takeDamage(50);
                tank.takeDamage(50);
                if (sq.isDead()) {
                    // remove health bar
                    HealthBar hb = ui.getHealthBarFor(sq);
                    if (hb != null) ui.removeHealthBar(hb);
                    toRemoveSquares.add(sq);

                    // award XP
                    gainXP(20);   // you can tune the amount
                }

            }
            sq.bounce(sq.hitWall());
            sq.move(0.06);
        }

        bullets.removeAll(toRemoveBullets);
        squares.removeAll(toRemoveSquares);

        // --- Health bars: show if damaged, remove if healed ---
        updateHealthBars();

        // --- Render ---
        repaint();
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // draw background grid first
        drawGrid(g, camX, camY);

        //double camX = tank.getPx() - COURT_WIDTH / 2.0;
        //double camY = tank.getPy() - COURT_HEIGHT / 2.0;

        for (Square sq : squares) {
            sq.draw(g, camX, camY);
        }

        snitch.draw(g, camX, camY);

        for (Bullet bullet : bullets) {
            bullet.draw(g, camX, camY);
        }

        tank.draw(g, camX, camY);

        ui.draw(g, camX, camY); // pass camera coordinates here
        drawXPBar(g);
        drawUpgradeBars(g);
    }



    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /*private void fireBullet() {
        Point tip = tank.getTurretTip();

        double bulletVelX = bulletSpeed * Math.cos(tank.angleRad);
        double bulletVelY = bulletSpeed * Math.sin(tank.angleRad);

        Bullet bullet = new Bullet(
                bulletVelX,
                bulletVelY,
                tip.x - Bullet.SIZE / 2.0,
                tip.y - Bullet.SIZE / 2.0,
                COURT_WIDTH,
                COURT_HEIGHT,
                Color.blue, 2
        );

        bullet.maxSpeed = bulletSpeed;
        bullet.penetration = bulletPenetration;
        bullet.maxLifetime = bullet.penetration * 67; // example: base 100, +20 per penetration
        bullet.lifetime = bullet.maxLifetime;

        tank.applyForce(-bulletVelX * recoilStrength, -bulletVelY * recoilStrength);

        bullets.add(bullet);
    } */

    private void updateHealthBars() {
        // Squares
        for (Square sq : squares) {
            handleHealthBar(sq, 30);
        }
        // Tank
        handleHealthBar(tank, 50);
    }

    private void handleHealthBar(GameObj obj, int width) {
        HealthBar hb = ui.getHealthBarFor(obj);
        if (obj.isDamaged() && hb == null) {
            ui.addHealthBar(new HealthBar(obj, width));
        } else if (!obj.isDamaged() && hb != null) {
            ui.removeHealthBar(hb);
        }
    }

    private void drawGrid(Graphics g, double camX, double camY) {
        g.setColor(Color.LIGHT_GRAY); // grid color
        int gridSize = 20;             // distance between lines

        int width = getWidth();
        int height = getHeight();

        // vertical lines
        for (int x = (int)(-camX % gridSize); x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }

        // horizontal lines
        for (int y = (int)(-camY % gridSize); y < height; y += gridSize) {
            g.drawLine(0, y, width, y);
        }
    }

    private boolean canUpgrade(int slot) {
        return getLevel(slot) < MAX_UPGRADE_LEVEL;
    }

    private void upgrade(int slot) {
        if (!canUpgrade(slot)) {
            status.setText("Already at max level!");
            return;
        }

        if (upgradePoints <= 0) {
            status.setText("No upgrade points!");
            return;
        }

        upgradePoints--;

        switch (slot) {

            case 1 -> { // Health Regen
                lvlHealthRegen++;
                tank.upgradeHealthRegen(tank.healthRegenMultiplier);
            }

            case 2 -> { // Max Health
                lvlTankMaxHealth++;
                tank.upgradeMaxHealth(tank.maxHealthMultiplier);
            }

            case 3 -> { // Body Damage
                lvlBodyDamage++;
                tank.upgradeBodyDamage(tank.bodyDamageMultiplier);
            }

            case 4 -> { // Bullet Speed
                lvlBulletSpeed++;
                tank.upgradeBulletSpeed(tank.bulletSpeedMultiplier);
            }

            case 5 -> { // Bullet Damage
                lvlBulletDamage++;
                tank.upgradeBulletDamage(tank.bulletDamageMultiplier);
            }

            case 6 -> { // Bullet Penetration
                lvlBulletPenetration++;
                tank.upgradeBulletPenetration(tank.bulletPenetrationMultiplier);
            }

            case 7 -> { // Fire Rate
                lvlFireRate++;
                tank.upgradeFireRate(tank.fireRateMultiplier);
            }

            case 8 -> { // Movement Speed
                lvlMovementSpeed++;
                tank.upgradeMovementSpeed(1.1);
                System.out.println(tank.getCurrentMovementSpeed());
            }
        }

        status.setText("Upgraded #" + slot + " → Level " + getLevel(slot));
    }


    private int getLevel(int slot) {
        return switch (slot) {
            case 1 -> lvlHealthRegen;
            case 2 -> lvlTankMaxHealth;
            case 3 -> lvlBodyDamage;
            case 4 -> lvlBulletSpeed;
            case 5 -> lvlBulletDamage;
            case 6 -> lvlBulletPenetration;
            case 7 -> lvlFireRate;
            case 8 -> lvlMovementSpeed;
            default -> 0;
        };
    }

    private void gainXP(int amount) {
        xp += amount;

        // Level up loop (handles large XP jumps)
        while (xp >= xpToLevel) {
            xp -= xpToLevel;
            level++;

            // give the player a point to spend
            upgradePoints++;

            // increase next threshold (optional)
            xpToLevel = (int) (xpToLevel * 1.2);

            status.setText("LEVEL UP! Level " + level + " — Upgrade points: " + upgradePoints);

            // --- Advanced Tank Upgrade Milestones ---
            if ((level == 15 || level == 30 || level == 45)
                    && !(tank instanceof TwinTank)
                    && !(tank instanceof SniperTank)
                    && !(tank instanceof FlankGuardTank)
                    && !(tank instanceof MachineGunTank)) {

                String[] options = {"Twin", "Sniper", "Flank Guard", "Machine Gun"};

                int choice = JOptionPane.showOptionDialog(
                        this,
                        "Choose your tank upgrade!",
                        "Tank Upgrade",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                Tank newTank = null;

                if (choice == 0) {
                    newTank = new TwinTank(tank.getPx(), tank.getPy(), COURT_WIDTH, COURT_HEIGHT);
                } else if (choice == 1) {
                    newTank = new SniperTank(tank.getPx(), tank.getPy(), COURT_WIDTH, COURT_HEIGHT);
                } else if (choice == 2) {
                    newTank = new FlankGuardTank(tank.getPx(), tank.getPy(), COURT_WIDTH, COURT_HEIGHT);
                } else if (choice == 3) {
                    newTank = new MachineGunTank(tank.getPx(), tank.getPy(), COURT_WIDTH, COURT_HEIGHT);
                }

                if (newTank != null) {
                    newTank.setHealth(tank.getHealth());
                    tank = newTank;
                    ui.addHealthBar(new HealthBar(tank, 50));
                    status.setText("Upgraded to " + options[choice] + "!");
                }
            }

        }
    }

    private void drawXPBar(Graphics g) {
        int barWidth = 300;
        int barHeight = 20;
        int x = (WINDOW_WIDTH - barWidth) / 2;
        int y = WINDOW_HEIGHT - 40;

        // background
        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, barWidth, barHeight);

        // fill amount
        double percent = xp / (double) xpToLevel;
        int fillWidth = (int)(percent * barWidth);

        g.setColor(new Color(100, 200, 255)); // XP blue
        g.fillRect(x, y, fillWidth, barHeight);

        // border
        g.setColor(Color.BLACK);
        g.drawRect(x, y, barWidth, barHeight);

        // text
        g.setColor(Color.WHITE);
        g.drawString("XP: " + xp + " / " + xpToLevel, x + 10, y + 15);
    }

    private void drawUpgradeBars(Graphics g) {
        if (!showUpgradeBar) return;

        int x = 20;
        int y = WINDOW_HEIGHT - 240;   // bottom-left area
        int barWidth = 180;
        int barHeight = 20;
        int spacing = 26;

        drawUpgradeBar(g, "Health Regen", lvlHealthRegen, x, y, barWidth, barHeight);
        drawUpgradeBar(g, "Max Health", lvlTankMaxHealth, x, y + spacing, barWidth, barHeight);
        drawUpgradeBar(g, "Body Damage", lvlBodyDamage, x, y + spacing * 2, barWidth, barHeight);
        drawUpgradeBar(g, "Bullet Speed", lvlBulletSpeed, x, y + spacing * 3, barWidth, barHeight);
        drawUpgradeBar(g, "Bullet Damage", lvlBulletDamage, x, y + spacing * 4, barWidth, barHeight);
        drawUpgradeBar(g, "Penetration", lvlBulletPenetration, x, y + spacing * 5, barWidth, barHeight);
        drawUpgradeBar(g, "Fire Rate", lvlFireRate, x, y + spacing * 6, barWidth, barHeight);
        drawUpgradeBar(g, "Move Speed", lvlMovementSpeed, x, y + spacing * 7, barWidth, barHeight);
    }

    private void drawUpgradeBar(Graphics g, String name, int level,
                                int x, int y, int w, int h) {

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ---- Background Bar ----
        g2.setColor(new Color(40, 40, 40));  // dark gray
        g2.fillRoundRect(x, y, w, h, 6, 6);

        // ---- Blue Fill ----
        double percent = (double) level / MAX_UPGRADE_LEVEL;
        int fillWidth = (int) (percent * w);

        Color fillColor = new Color(80, 180, 255);
        g2.setColor(fillColor);
        g2.fillRoundRect(x, y, fillWidth, h, 6, 6);

        // ---- Border ----
        g2.setColor(Color.BLACK);
        g2.drawRoundRect(x, y, w, h, 6, 6);

        // ---- Text inside bar ----
        g2.setColor(Color.WHITE);
        g2.drawString(name, x + 6, y + h - 5);
    }



}