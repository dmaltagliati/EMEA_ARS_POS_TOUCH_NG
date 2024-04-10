package com.ncr.gui;

import com.ncr.Config;
import com.ncr.Device;
import com.ncr.FmtIo;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class DynakeyGroup extends Panel {
    public Font dble;
    public Component kbrd;
    public Image image = null;
    public Ground ground = null;
    public Dynakey keys[] = new Dynakey[8];
    public GdLabel chooser = new GdLabel(null, GdLabel.STYLE_WINDOW);
    public int downKey, substate = 0;

    public DynakeyGroup(Dimension d) {
        setSize(d);
        setLayout(new GridLayout(0, 1));
        for (int ind = keys.length; ind-- > 0; add(new Dynakey(this))) ;
        chooser.setVisible(false);
        chooser.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if ((e.getModifiers() & e.BUTTON1_MASK) == 0) return;
                Dimension d = chooser.getSize();
                Dimension grid = new Dimension(5, 4);
                int x = e.getX() / (d.width / grid.width);
                int y = e.getY() / (d.height / grid.height);
                x += y * grid.width + 1;
                Device.postInput(chooser.getName() + ":" + x, null);
                e.consume();
                getToolkit().beep();
            }
        });
    }

    public Dimension getPreferredSize() {
        return getSize();
    }

    public void showTouch(boolean visible) {
        chooser.setVisible(visible);
    }

    public void setTouch(String list) {
        chooser.setName("LIST" + list);
        chooser.setImage(Config.localFile("cafe", "TCH_" + list + ".GIF"));
        showTouch(chooser.image != null);
    }

    public void select(int ind) {
        String name = keys[ind].text;
        if (name == null) return;
        if (downKey >= 0) keys[downKey].repaint();
        keys[downKey = ind].repaint();
        setTouch(name.substring(12));
    }

    public void setState(int state) {
        String name = substate > 0 ? "POS_DS" : "POS_DK";

        File f = Config.localFile("d800", name + FmtIo.editNum(state, 2) + ".GIF");
        if (f.exists()) {
            image = getToolkit().getImage(f.getAbsolutePath());
            prepareImage(image, null);
        } else image = null;
        downKey = -1;
        if (kbrd == null) {
            setEnabled(false);
            for (int ind = keys.length; ind-- > 0; keys[ind].setEnabled(true)) ;
        }
    }
}
