package org.cis1200.cantankerous;

import java.awt.*;

/**
 * An object in the game.
 * <p>
 * Game objects exist in the game court. They have a position, velocity, size
 * and bounds. Their velocity controls how they move; their position should
 * always be within their bounds.
 */
public abstract class GameObj {
    /*
     * Current position of the object (in terms of graphics coordinates)
     *
     * Coordinates are given by the upper-left hand corner of the object. This
     * position should always be within bounds:
     * 0 <= px <= maxX 0 <= py <= maxY
     */
    private double px;
    private double py;

    /* Size of object, in pixels. */
    private final int width;
    private final int height;

    /* Velocity: number of pixels to move every time move() is called. */
    private double vx;
    private double vy;

    private double fx = 0; // forces applied this frame
    private double fy = 0;
    double maxSpeed = 3; //speed clamp


    //health
    protected int maxHealth = 0;
    protected int health = 0;


    /*
     * Upper bounds of the area in which the object can be positioned. Maximum
     * permissible x, y positions for the upper-left hand corner of the object.
     */
    private final int maxX;
    private final int maxY;

    /**
     * Constructor
     */
    public GameObj(
            double vx, double vy, double px, double py, int width, int height, int courtwidth,
            int courtheight
    ) {
        this.vx = vx;
        this.vy = vy;
        this.px = px;
        this.py = py;
        this.width = width;
        this.height = height;

        // take the width and height into account when setting the bounds for
        // the upper left corner of the object.
        this.maxX = courtwidth - width;
        this.maxY = courtheight - height;
    }

    // **********************************************************************************
    // * GETTERS
    // **********************************************************************************
    public int getPx() {
        return (int) this.px;
    }

    public int getPy() {
        return (int) this.py;
    }

    public double getVx() {
        return this.vx;
    }

    public double getVy() {
        return this.vy;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setHealth(int hp) {
        this.maxHealth = hp;
        this.health = hp;
    }


    // **************************************************************************
    // * SETTERS
    // **************************************************************************
    public void setPx(int px) {
        this.px = px;
        clip();
    }

    public void setPy(int py) {
        this.py = py;
        clip();
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public boolean isDamaged() {
        return getHealth() < getMaxHealth();
    }


    // **************************************************************************
    // * UPDATES AND OTHER METHODS
    // **************************************************************************

    /**
     * Prevents the object from going outside the bounds of the area
     * designated for the object (i.e. Object cannot go outside the active
     * area the user defines for it).
     */
    private void clip() {
        this.px = Math.min(Math.max(this.px, 0), this.maxX);
        this.py = Math.min(Math.max(this.py, 0), this.maxY);
    }

    /**
     * applies a force to an object. will decelerate in
     */
    public void applyForce(double fx, double fy) {
        this.fx += fx;
        this.fy += fy;
    }


    /**
     * Moves the object by its velocity. Ensures that the object does not go
     * outside its bounds by clipping.
     */
    public void move(double frictionForce) {
        // 1. acceleration from forces
        //accel
        double ax = fx;
        double ay = fy;

        // 2. update velocity
        vx += ax;
        vy += ay;

        // 3. apply friction
        vx = applyFriction(vx, frictionForce);
        vy = applyFriction(vy, frictionForce);

        // 4. clamp speed for stability
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed > maxSpeed) {
            vx = vx / speed * maxSpeed;
            vy = vy / speed * maxSpeed;
        }

        // 5. update position
        px += vx;
        py += vy;

        fx = 0;
        fy = 0;

        clip();
    }

    private double applyFriction(double v, double coeff) {
        if (v == 0) return 0;

        // friction proportional to log of speed
        double reduction = coeff * Math.log(1 + Math.abs(v)); // log(1 + |v|) avoids log(0)
        v -= Math.signum(v) * reduction;

        // clamp to 0 if we overshoot
        if (Math.signum(v) != Math.signum(v - Math.signum(v) * reduction)) {
            v = 0;
        }

        return v;
    }

    /**
     * Health stuff: healthbar ui display, etc
     *
     */
    public void takeDamage(int amount) {
        this.health -= amount;
        if (health < 0) health = 0;
    }

    public boolean isDead() {
        return health == 0;
    }

    /**
     * Determine whether this game object is currently intersecting another
     * object.
     * <p>
     * Intersection is determined by comparing bounding boxes. If the bounding
     * boxes overlap, then an intersection is considered to occur.
     *
     * @param that The other object
     * @return Whether this object intersects the other object.
     */
    public boolean intersects(GameObj that) {
        return (this.px + this.width >= that.px
                && this.py + this.height >= that.py
                && that.px + that.width >= this.px
                && that.py + that.height >= this.py);
    }


    /**
     * Update the velocity of the object in response to hitting an obstacle in
     * the given direction. If the direction is null, this method has no effect
     * on the object.
     *
     * @param d The direction in which this object hit an obstacle
     */
    public void bounce(Direction d) {
        if (d == null) {
            return;
        }

        switch (d) {
            case UP:
                this.vy = Math.abs(this.vy);
                break;
            case DOWN:
                this.vy = -Math.abs(this.vy);
                break;
            case LEFT:
                this.vx = Math.abs(this.vx);
                break;
            case RIGHT:
                this.vx = -Math.abs(this.vx);
                break;
            default:
                break;
        }
    }

    /**
     * Determine whether the game object will hit a wall in the next time step.
     * If so, return the direction of the wall in relation to this game object.
     *
     * @return Direction of impending wall, null if all clear.
     */
    public Direction hitWall() {
        if (this.px + this.vx < 0) {
            return Direction.LEFT;
        } else if (this.px + this.vx > this.maxX) {
            return Direction.RIGHT;
        }

        if (this.py + this.vy < 0) {
            return Direction.UP;
        } else if (this.py + this.vy > this.maxY) {
            return Direction.DOWN;
        } else {
            return null;
        }
    }


    public abstract void draw(Graphics g, double camX, double camY);
}