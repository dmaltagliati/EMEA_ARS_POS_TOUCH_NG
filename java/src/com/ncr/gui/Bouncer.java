package com.ncr.gui;

import java.awt.*;
import java.io.File;

public class Bouncer extends Thread implements Runnable {
    Component base;
    Dimension d;
    Image image, stamp, frame;
    Graphics g, h;
    Rectangle ball = new Rectangle();
    public boolean enabled, visible;

    public void init(Component c, File f) {
        if (!f.exists())
            return;
        base = c;
        d = c.getSize();
        stamp = c.createImage(d.width, d.height);
        h = stamp.getGraphics();
        h.setColor(Color.white);
        h.setXORMode(h.getColor());
        image = c.getToolkit().getImage(f.getAbsolutePath());
        c.prepareImage(image, null);
        g = c.getGraphics();
        g.setXORMode(c.getBackground());
        setName("Bouncer:" + c.getName());
        start();
    }

    public void exit() {
        if (image == null)
            return;
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
        }
        hide();
        g.dispose();
    }

    public void hide() {
        if (!visible)
            return;
        g.drawImage(stamp, 0, 0, null);
        h.drawImage(frame, ball.x, ball.y, null);
        visible = false;
    }

    public void run() {
        int dx = 4, dy = 4;

        while (true)
            try {
                sleep(20);
                if ((base.checkImage(image, null) & base.ALLBITS + base.FRAMEBITS) == 0)
                    continue;
                if (frame == null)
                    frame = base.createImage(ball.width = image.getWidth(null), ball.height = image.getHeight(null));
                synchronized (base) {
                    if (enabled) {
                        ball.translate(dx, dy);
                        if (dx > 0 && ball.x + ball.width > d.width || dx < 0 && ball.x < 0)
                            ball.x += dx = 0 - dx;
                        if (dy > 0 && ball.y + ball.height > d.height || dy < 0 && ball.y < 0)
                            ball.y += dy = 0 - dy;
                        frame.getGraphics().drawImage(image, 0, 0, null);
                        h.drawImage(frame, ball.x, ball.y, null);
                        g.drawImage(stamp, 0, 0, null);
                        if (visible) {
                            h.clearRect(0, 0, d.width, d.height);
                            h.drawImage(frame, ball.x, ball.y, null);
                        } else
                            visible = true;
                    } else
                        hide();
                }
            } catch (InterruptedException e) {
                break;
            }
    }
}
