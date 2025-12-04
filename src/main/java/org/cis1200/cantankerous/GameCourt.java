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

    // the state of the game logic
    //private Square square; // the Black Square, keyboard control
    private Tank tank;
    private Circle snitch; // the Golden Snitch, bounces
   //private Poison poison; // the Poison Mushroom, doesn't move

    private boolean playing = false; // whether the game is running
    private final JLabel status; // Current status text, i.e. "Running..."

    // Game constants
    public static final int COURT_WIDTH = 1920;
    public static final int COURT_HEIGHT = 1080;
    public static final int TANK_VELOCITY = 4;

    // Update interval for timer, in milliseconds
    public static final int INTERVAL = 35;

    //sussy input fix
    private boolean wDown = false;
    private boolean aDown = false;
    private boolean sDown = false;
    private boolean dDown = false;

    double force = 0.7; // smooth acceleration strength

    //spawned objects
    private java.util.List<Square> squares = new java.util.ArrayList<>();


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


        //mouse movement listener:
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                tank.trackMouse(e);
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tank.shoot(e);
            }
        });

        this.status = status;
    }

    /**
     * (Re-)set the game to its initial state.
     */
    public void reset() {
        //square = new Square(COURT_WIDTH, COURT_HEIGHT, Color.BLACK);
        tank = new Tank(150,150, COURT_WIDTH, COURT_HEIGHT);
        //poison = new Poison(COURT_WIDTH, COURT_HEIGHT);
        snitch = new Circle(COURT_WIDTH, COURT_HEIGHT, Color.YELLOW);

        spawnObjects();

        playing = true;
        status.setText("Running...");

        // Make sure that this component has the keyboard focus
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

            Square sq = new Square(COURT_WIDTH, COURT_HEIGHT, Color.BLUE);

            // place square manually
            sq.setPx(x);
            sq.setPy(y);

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

        double strength = 1.5; // tweak for how strong the push is

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
        if (playing) {
            // advance the square and snitch in their current direction.
            tank.move();
            snitch.move();
            applyMovementForces();

            for (GameObj a : squares) {
                for (GameObj b : squares) {
                    if (a == b) continue;

                    if (a.intersects(b)) {
                        applyRepulsion(a, b);
                    }
                }
            }


            // make the snitch bounce off walls...
            snitch.bounce(snitch.hitWall());
            // ...and the mushroom

            for (Square sq : squares) {
                snitch.bounce(snitch.hitObj(sq));
            }

            // check for the game end conditions
           /* if (tank.intersects(poison)) {
                playing = false;
                status.setText("You lose!");
            } else if (tank.intersects(snitch)) {
                playing = false;
                status.setText("You win!");
            } */

            // collisions between tank and squares
            for (Square sq : squares) {
                if (tank.intersects(sq)) {
                    playing = false;
                    status.setText("Hit a square — game over!");
                }
            }

            // update the display
            repaint();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        tank.draw(g);
        //poison.draw(g);
        snitch.draw(g);

        for (Square sq : squares) {
            sq.draw(g);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(COURT_WIDTH, COURT_HEIGHT);
    }
}