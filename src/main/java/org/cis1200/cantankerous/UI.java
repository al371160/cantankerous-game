package org.cis1200.cantankerous;

import java.awt.*;
import java.util.*;

public class UI {

    private java.util.List<HealthBar> healthBars = new ArrayList<>();

    public void addHealthBar(HealthBar hb) {
        healthBars.add(hb);
    }

    public void removeHealthBar(HealthBar hb) {
        healthBars.remove(hb);
    }

    public HealthBar getHealthBarFor(GameObj obj) {
        for (HealthBar hb : healthBars) {
            if (hb.getTarget() == obj) {
                return hb;
            }
        }
        return null;
    }

    public void draw(Graphics g, double camX, double camY) {
        for (HealthBar hb : healthBars) {
            hb.draw(g, camX, camY);
        }
    }
}
