package org.cis1200.cantankerous;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Scanner;

/**
 * GameCourt
 * <p>
 * This class holds the primary game logic for how different objects interact
 * with one another. Take time to understand how the timer interacts with the
 * different methods and how it repaints the GUI on every tick().
 */
public class GameCourt extends JPanel {

    //all panels activated in one scene lol
    public enum GameState {
        START_MENU,
        LOAD_SAVE,
        PLAYING,
        PAUSED,
        DEAD
    }

    private GameState gameState = GameState.START_MENU;

    //CAMERA!!
    private double camX = 0;
    private double camY = 0;
    private final double camSpeed = 0.1; //interpolate

    //UI variable
    private UI ui = new UI();
    // the state of the game logic
    //private Square square; // the Black Square, keyboard control
    private Tank tank;
    //private Poison poison; // the Poison Mushroom, doesn't move

    private boolean playing = false; // whether the game is running
    private final JLabel status; // Current status text, i.e. "Running..."

    // Game constants
    public static final int COURT_WIDTH = 2000;
    public static final int COURT_HEIGHT = 2000;
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
    private final java.util.List<Square> squares = new java.util.ArrayList<>();
    private final java.util.List<Bullet> bullets = new java.util.ArrayList<>();

    //firing
    private boolean mouseDown = false;
    private int fireCooldown = 0;      // counts down each tick

    /**
     * \/\/\/\/\/\/\/\/\/[    UPGRADES!!!!!    ]/\/\/\/\/\/\/\/\//\/\\/\/\/\/\
     */
    // XP AND LEVELS
    private int xp = 0;
    private int xpToLevel = 100;
    private int upgradePoints = 0;    // points you can spend upgrading
    private int level = 1;            // player level

    //UPGRADE BAR
    private boolean showUpgradeBar = true;   // Q toggles this
    private static final int MAX_UPGRADE_LEVEL = 8;

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
                switch (gameState) {

                    case START_MENU -> {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER)
                            gameState = GameState.LOAD_SAVE;
                    }

                    case LOAD_SAVE -> {
                        if (e.getKeyCode() == KeyEvent.VK_1) {
                            // new game
                            gameState = GameState.PLAYING;
                            reset();
                        }
                        if (e.getKeyCode() == KeyEvent.VK_2) {
                            loadSave(1);
                            gameState = GameState.PLAYING;
                        }
                    }

                    case PLAYING -> {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                            gameState = GameState.PAUSED;
                    }

                    case PAUSED -> {
                        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                            gameState = GameState.PLAYING;
                        if (e.getKeyCode() == KeyEvent.VK_Q)
                            gameState = GameState.START_MENU;
                        if (e.getKeyCode() == KeyEvent.VK_U)
                            saveGame(1);
                    }

                    case DEAD -> {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            gameState = GameState.START_MENU;
                        }
                    }
                }
                if (gameState == GameState.PLAYING) {
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
                    tank.trackMouse(mouseX, mouseY, camX, camY);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                if (tank != null) {
                    // convert screen coords to world coords using camera offset
                    tank.trackMouse(mouseX, mouseY, camX, camY);
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


        this.status = status;
    }

    //RESET
    public void reset() {
        // Clear bullets
        bullets.clear();

        // Reset player stats
        xp = 0;
        level = 0;
        upgradePoints = 0;

        lvlHealthRegen = 0;
        lvlTankMaxHealth = 0;
        lvlBodyDamage = 0;
        lvlBulletSpeed = 0;
        lvlBulletDamage = 0;
        lvlBulletPenetration = 0;
        lvlFireRate = 0;
        lvlMovementSpeed = 0;

        xpToLevel = 100; // reset XP threshold

        // Create base tank at default location
        tank = new BaseTank(1000, 1000, COURT_WIDTH, COURT_HEIGHT);
        tank.setHealth(tank.getCurrentMaxHealth());
        tank.level = 0; // base level

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

        int numSquares = 400; // change this to spawn more/less

        Rectangle tankRect = new Rectangle(
                1000,
                1000,
                80,
                80
        );

        for (int i = 0; i < numSquares; i++) {
            int x, y;

            // generate a random position not overlapping the tank
            do {
                x = rand.nextInt(COURT_WIDTH - Square.SIZE);
                y = rand.nextInt(COURT_HEIGHT - Square.SIZE);
            } while (tankRect.intersects(new Rectangle(x, y, Square.SIZE, Square.SIZE)));
            //so it can finally stop displaying high levels after game start.
            Square sq = new Square(COURT_WIDTH, COURT_HEIGHT);


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

        if (gameState == GameState.PLAYING && tank.getHealth() <= 0) {
            gameState = GameState.DEAD;
        }

        // --- Camera: center on tank ---
        double targetCamX = tank.getPx() - WINDOW_WIDTH / 2.0 + tank.getWidth() / 2.0;
        double targetCamY = tank.getPy() - WINDOW_HEIGHT / 2.0 + tank.getHeight() / 2.0;
        camX += (targetCamX - camX) * camSpeed;
        camY += (targetCamY - camY) * camSpeed;

        // --- Movement & input ---
        applyMovementForces();
        tank.move(strength);
        tank.trackMouse(mouseX, mouseY, camX, camY); // mouse in world coords

        // --- Health Regen ---
        tank.regenerateHealth();

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
                        gainXP(Square.squareXP);   // you can tune the amount
                    }

                }
            }
        }

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
                    gainXP(Square.squareXP);   // you can tune the amount
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

        switch (gameState) {
            case START_MENU -> drawStartScreen(g);
            case LOAD_SAVE -> drawLoadSaveScreen(g);
            case PLAYING -> drawGame(g);
            case PAUSED -> drawPauseScreen(g);
            case DEAD -> drawDeathScreen(g);
        }
    }

    private void drawGame(Graphics g) {
        // draw background grid
        drawGrid(g, camX, camY);

        // draw squares
        for (Square sq : squares) {
            sq.draw(g, camX, camY);
        }

        // draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(g, camX, camY);
        }

        // draw tank
        if (tank != null) tank.draw(g, camX, camY);

        // draw UI
        ui.draw(g, camX, camY);

        // draw XP and upgrade bars
        drawXPBar(g);
        drawUpgradeBars(g);

    }


    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);

    }

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
        for (int x = (int) (-camX % gridSize); x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }

        // horizontal lines
        for (int y = (int) (-camY % gridSize); y < height; y += gridSize) {
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
                tank.upgradeHealthRegen(1.2);
                System.out.println(tank.getCurrentHealthRegen());
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
                tank.upgradeBulletSpeed(1.1);

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
                tank.upgradeFireRate(0.99);
            }

            case 8 -> { // Movement Speed
                lvlMovementSpeed++;
                tank.upgradeMovementSpeed(1.1);
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

                    wDown = false;
                    aDown = false;
                    sDown = false;
                    dDown = false;
                    mouseDown = false;
                    ui.ClearHealthBars();
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
        int fillWidth = (int) (percent * barWidth);

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

    private void drawStartScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        g.drawString("CANTANKEROUS", 280, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press ENTER to Start", 320, 300);

        g.setFont(new Font("Arial", Font.PLAIN, 15));
        g.drawString("This is a diep.io clone game! user any of the number keys to upgrade", 220, 400);
        g.drawString("the tank (and if you want, hit q to close the menu). WASD to move.", 220, 420);
        g.drawString("Left click to shoot. Mouse around to aim. [ESC] to pause. Other", 220, 440);
        g.drawString("actions are displayed in various menus", 220, 460);


    }

    private void drawLoadSaveScreen(Graphics g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("Load Save", 380, 150);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("1: New Game", 350, 250);
        g.drawString("2: Load Save", 350, 300);
    }


    private void drawPauseScreen(Graphics g) {
        // dim background
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("PAUSED", 400, 200);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        g.drawString("Press ESC to Resume", 340, 280);
        g.drawString("Press Q to Quit", 370, 330);
        g.drawString("Press U to Save Game", 340, 380); // <-- new line added
    }


    private void drawDeathScreen(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.drawString("YOU DIED", 380, 200);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 36));
        g.drawString("Press ENTER to Restart", 310, 300);
    }


    public void saveGame(int slot) {
        try {
            FileWriter writer = new FileWriter("save" + slot + ".txt");

            // --- Save Upgrade Levels (0-8) ---
            writer.write(lvlHealthRegen + "\n");
            writer.write(lvlTankMaxHealth + "\n");
            writer.write(lvlBodyDamage + "\n");
            writer.write(lvlBulletSpeed + "\n");
            writer.write(lvlBulletDamage + "\n");
            writer.write(lvlBulletPenetration + "\n");
            writer.write(lvlFireRate + "\n");
            writer.write(lvlMovementSpeed + "\n");

            // --- Save XP, Level, Upgrade Points ---
            writer.write(xp + "\n");
            writer.write(level + "\n");
            writer.write(upgradePoints + "\n");

            // --- Save Tank Position ---
            writer.write(tank.getPx() + "\n");
            writer.write(tank.getPy() + "\n");

            // --- Save Squares ---
            writer.write(squares.size() + "\n");
            for (Square sq : squares) {
                writer.write(sq.getPx() + "," + sq.getPy() + "\n");
            }

            writer.close();
            status.setText("Game saved in slot " + slot + "!");
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("Failed to save game!");
        }
    }

    public void loadSave(int slot) {
        try {
            FileReader reader = new FileReader("save" + slot + ".txt");
            Scanner sc = new Scanner(reader);

            // --- Load Upgrade Levels ---
            lvlHealthRegen = Integer.parseInt(sc.nextLine());
            lvlTankMaxHealth = Integer.parseInt(sc.nextLine());
            lvlBodyDamage = Integer.parseInt(sc.nextLine());
            lvlBulletSpeed = Integer.parseInt(sc.nextLine());
            lvlBulletDamage = Integer.parseInt(sc.nextLine());
            lvlBulletPenetration = Integer.parseInt(sc.nextLine());
            lvlFireRate = Integer.parseInt(sc.nextLine());
            lvlMovementSpeed = Integer.parseInt(sc.nextLine());

            // --- Load XP, Level, Upgrade Points ---
            xp = Integer.parseInt(sc.nextLine());
            level = Integer.parseInt(sc.nextLine());
            upgradePoints = Integer.parseInt(sc.nextLine());

            // ensure xpbar doesn't kill itself
            xpToLevel = 100;
            for (int i = 1; i <= level; i++) {
                xpToLevel = (int) (xpToLevel * 1.2);
            }

            // --- Load Tank Position ---
            int tankX = Integer.parseInt(sc.nextLine());
            int tankY = Integer.parseInt(sc.nextLine());
            tank = new BaseTank(tankX, tankY, COURT_WIDTH, COURT_HEIGHT);
            tank.setHealth(tank.getCurrentMaxHealth());

            // --- Apply upgrades based on saved levels ---
            for (int i = 0; i < lvlHealthRegen; i++) tank.upgradeHealthRegen(tank.healthRegenMultiplier);
            for (int i = 0; i < lvlTankMaxHealth; i++) tank.upgradeMaxHealth(tank.maxHealthMultiplier);
            for (int i = 0; i < lvlBodyDamage; i++) tank.upgradeBodyDamage(tank.bodyDamageMultiplier);
            for (int i = 0; i < lvlBulletSpeed; i++) tank.upgradeBulletSpeed(1.1);
            for (int i = 0; i < lvlBulletDamage; i++) tank.upgradeBulletDamage(tank.bulletDamageMultiplier);
            for (int i = 0; i < lvlBulletPenetration; i++)
                tank.upgradeBulletPenetration(tank.bulletPenetrationMultiplier);
            for (int i = 0; i < lvlFireRate; i++) tank.upgradeFireRate(tank.fireRateMultiplier);
            for (int i = 0; i < lvlMovementSpeed; i++) tank.upgradeMovementSpeed(1.1);

            // --- Load Squares ---
            ui.ClearHealthBars();
            squares.clear();
            int numSquares = Integer.parseInt(sc.nextLine());
            for (int i = 0; i < numSquares; i++) {
                String[] coords = sc.nextLine().split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                Square sq = new Square(COURT_WIDTH, COURT_HEIGHT);
                sq.setPx(x);
                sq.setPy(y);
                sq.setHealth(50);
                squares.add(sq);
                ui.addHealthBar(new HealthBar(sq, 30));
            }

            // --- Add Tank Health Bar ---
            ui.addHealthBar(new HealthBar(tank, 50));

            sc.close();
            status.setText("Loaded save slot " + slot + "!");
        } catch (Exception e) {
            e.printStackTrace();
            status.setText("Failed to load save.");
        }
    }


}