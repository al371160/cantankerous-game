package org.cis1200.cantankerous;

import java.awt.*;

public class HealthBar {

    private GameObj target;
    private int width;
    private int height = 6;

    public HealthBar(GameObj target, int width) {
        this.target = target;
        this.width = width;
    }

    public void draw(Graphics g, double camX, double camY) {
        if (target.maxHealth == 0) return;

        int x = (int)(target.getPx() - camX + target.getWidth()/2 - width/2); // center above object
        int y = (int)(target.getPy() - camY - 10); // 10px above

        double hpPercent = target.getHealth() / (double) target.maxHealth;
        int filledWidth = (int) (width * hpPercent);

        g.setColor(Color.DARK_GRAY);
        g.fillRect(x, y, width, height);

        g.setColor(Color.GREEN);
        g.fillRect(x, y, filledWidth, height);

        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, width, height);
    }




    //getter!
    public GameObj getTarget() {
        return target;
    }
}
