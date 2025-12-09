package org.cis1200.cantankerous;

import java.awt.*;

public record HealthBar(GameObj target, int width) {

    public void draw(Graphics g, double camX, double camY) {
        if (target.maxHealth == 0) return;

        int x = (int) (target.getPx() - camX + (double) target.getWidth() / 2 - (double) width / 2); // center above object
        int y = (int) (target.getPy() - camY - 15); // 10px above

        double hpPercent = target.getHealth() / (double) target.maxHealth;
        int filledWidth = (int) (width * hpPercent);

        g.setColor(Color.DARK_GRAY);
        int height = 6;
        g.fillRect(x, y, width, height);

        g.setColor(Color.GREEN);
        g.fillRect(x, y, filledWidth, height);

        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, width, height);
    }
}
