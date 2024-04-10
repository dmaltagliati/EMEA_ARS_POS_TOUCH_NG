package com.ncr.gui;

import java.awt.*;
import java.io.File;

public abstract class Picture extends Canvas {
    public String text;
    public Image image = null;
    public Ground ground = null;
    public int align = Label.CENTER;

    public int getAlignment() {
        return align;
    }

    public void setAlignment(int align) {
        this.align = align;
    }

    public void setEnabled(boolean state) {
        super.setEnabled(state);
        repaint(); /* for JView only */
    }

    public String getText() {
        return text;
    }

    public void setImage(File f) {
        if (image != null) {
            image.flush();
            image = null;
            repaint();
        }
        if (f == null)
            return;
        if (f.exists()) {
            image = getToolkit().getImage(f.getAbsolutePath());
            prepareImage(image, this);
        }
    }

    public synchronized void setText(String s) {
        if (s != null)
            if (s.equals(text))
                return;
        text = s;
        Graphics g = getGraphics();
        if (g != null) {
            paint(g);
            g.dispose();
        }
    }

    public void update(Graphics g) /* MS with imageUpdate only */ {
        paint(g);
    }
}
