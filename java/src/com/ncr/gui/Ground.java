package com.ncr.gui;

import java.awt.*;
import java.io.File;

public class Ground {
    Image image;
    Container base;

    public Ground(Container c, File f) {
        base = c;
        image = c.getToolkit().getImage(f.getAbsolutePath());
        c.prepareImage(image, null);
        while ((c.checkImage(image, null) & c.ALLBITS) == 0)
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
    }

    public synchronized void paintOn(Component c, Graphics g) {
        if (!c.isShowing())
            return;
        Dimension d = c.getSize();
        Insets i = base.getInsets();
        Point src = c.getLocationOnScreen();
        Point org = base.getLocationOnScreen();
        Color bg = c.getBackground();
        bg = new Color(bg.getRGB()); // EVM can't handle SystemColor
        src.translate(i.left - org.x, i.top - org.y);
        g.drawImage(image, 0, 0, d.width, d.height, src.x, src.y, src.x + d.width, src.y + d.height, bg, null);
    }
}
