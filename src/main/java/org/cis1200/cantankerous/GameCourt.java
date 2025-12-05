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
    public static final int COURT_WIDTH = 1000;
    public static final int COURT_HEIGHT = 1000;
    public static final int TANK_VELOCITY = 4;

    // Update interval for timer, in milliseconds
    public static final int INTERVAL = 35;

    //sussy input fix
    private boolean wDown = false;
    private boolean aDown = false;
    private boolean sDown = false;
    private boolean dDown = false;

    //physics
    double strength = 0.2; // repel strength
    double force = 2; // smooth acceleration strength; movementSpeed in object class, might need to be careful

    private int mouseX;
    private int mouseY;

    //spawned objects
    private java.util.List<Square> squares = new java.util.ArrayList<>();
    private java.util.List<Bullet> bullets = new java.util.ArrayList<>();

    //firing
    private boolean mouseDown = false;
    private int fireCooldown = 0;      // counts down each tick

    //upgrades:
    public int bulletSpeed = 3;
    public int fireRate = 10;           // ticks between bullets (≈350ms)

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
                    case KeyEvent.VK_W -> wDown = true;
                    case KeyEvent.VK_A -> aDown = true;
                    case KeyEvent.VK_S -> sDown = true;
                    case KeyEvent.VK_D -> dDown = true;
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
        tank = new Tank(150, 150, COURT_WIDTH, COURT_HEIGHT);
        tank.setHealth(100);

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
        if (wDown) tank.applyForce(0, -force);
        if (sDown) tank.applyForce(0, force);
        if (aDown) tank.applyForce(-force, 0);
        if (dDown) tank.applyForce(force, 0);
    }

    public void applyRepulsion(GameObj a, GameObj b) {
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
        double targetCamX = tank.getPx() - COURT_WIDTH / 2.0 + tank.getWidth() / 2.0;
        double targetCamY = tank.getPy() - COURT_HEIGHT / 2.0 + tank.getHeight() / 2.0;
        camX += (targetCamX - camX) * camSpeed;
        camY += (targetCamY - camY) * camSpeed;

        // --- Movement & input ---
        applyMovementForces();
        tank.move(strength);
        tank.trackMouse(mouseX, mouseY, camX, camY); // mouse in world coords
        snitch.move(0);

        // --- Shooting ---
        if (mouseDown && fireCooldown <= 0) {
            fireBullet();
            fireCooldown = fireRate;
        }
        if (fireCooldown > 0) fireCooldown--;

        for (Bullet bullet : bullets) bullet.move(0);

        // --- Squares interactions ---
        for (GameObj a : squares) {
            for (GameObj b : squares) {
                if (a != b && a.intersects(b)) applyRepulsion(a, b);
            }
        }

        // --- Bullet interactions ---
        java.util.List<Bullet> toRemoveBullets = new java.util.ArrayList<>();
        java.util.List<Square> toRemoveSquares = new java.util.ArrayList<>();

        for (Bullet bullet : bullets) {
            for (Square sq : squares) {
                if (bullet.intersects(sq)) {
                    applyRepulsion(bullet, sq);
                    sq.takeDamage(20);
                    toRemoveBullets.add(bullet);

                    if (sq.isDead()) {
                        HealthBar hb = ui.getHealthBarFor(sq);
                        if (hb != null) ui.removeHealthBar(hb);
                        toRemoveSquares.add(sq);
                    }
                }
            }


        }

        bullets.removeAll(toRemoveBullets);
        squares.removeAll(toRemoveSquares);

        // --- Snitch bounces ---
        snitch.bounce(snitch.hitWall());
        for (Square sq : squares) snitch.bounce(snitch.hitObj(sq));

        // --- Tank collisions ---
        for (GameObj sq : squares) {
            if (tank.intersects(sq)) applyRepulsion(sq, tank);
            sq.bounce(sq.hitWall());
            sq.move(0.01);
        }

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
    }



    @Override
    public Dimension getPreferredSize() {
        return new Dimension(COURT_WIDTH, COURT_HEIGHT);
    }

    private void fireBullet() {
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
                Color.blue
        );
        // Apply recoil to tank
        double recoilStrength = 0.5;
        tank.applyForce(-bulletVelX * recoilStrength, -bulletVelY * recoilStrength);

        bullets.add(bullet);
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
        for (int x = (int)(-camX % gridSize); x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }

        // horizontal lines
        for (int y = (int)(-camY % gridSize); y < height; y += gridSize) {
            g.drawLine(0, y, width, y);
        }
    }



}