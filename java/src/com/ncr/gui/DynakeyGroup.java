package com.ncr.gui;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.ncr.ArsXmlParser;
import com.ncr.DatIo;
import com.ncr.FmtIo;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

public class DynakeyGroup extends JPanel {

    /**
     *
     */
    private static final long serialVersionUID = -3197258470423388245L;
    private DynakeyChooser chooser = new DynakeyChooser();
    private BufferedImage image = null;
    private int downKey;
    private int state = 0;
    private int substate = 0;
    public int horizontalAlignment = SwingConstants.LEFT;

    private static final int DYNAKEY_BUTTONS = 8;

    public DynakeyGroup() {

        setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("Dynakey", "Opaque")).booleanValue());
        setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("Dynakey", "Bounds"));
        Integer verticalGap = ((Integer) ArsXmlParser.getInstance().getPanelElement("Dynakey", "VerticalGap"));
        setLayout(new GridLayout(DYNAKEY_BUTTONS, 1, 0, verticalGap == null ? 0 : verticalGap.intValue()));
        for (int i = DYNAKEY_BUTTONS; i > 0; i--) {
            Dynakey dynakey = new Dynakey(this);
            add(dynakey);
        }

    }

    public void setTouch(String list) {
        UtilLog4j.logInformation(this.getClass(), "list=" + list);
        chooser.setName("LIST" + list);
        chooser.setImage("cafe", "TCH_" + list.replace('*', 'X') + ".GIF");
        showTouch(chooser.getImage() != null);
    }

    public void showTouch(boolean show) {
        UtilLog4j.logInformation(this.getClass(), "show=" + show);
        chooser.setVisible(show);
        GdPos.panel.refreshModals();
    }

    public void select(int ind) {
        String name = key(ind).getLineTop().getText();

        if (name == null || name.trim().length() == 0) {
            return;
        }
        downKey = ind;
        name = "TCH_" + name.replace('*', 'X');
        chooser.setImage("cafe", name + ".GIF");
        showTouch(chooser.getImage() != null);
    }

    public Dynakey key(int ind) {
        return (Dynakey) (getComponent(ind));
    }

    void dkyTouch(int ind) {
        KeyEvent e = new KeyEvent(GdPos.panel.modal == null ? GdPos.panel.kbrd : GdPos.panel.modal.getKbrd(),
                KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, KeyEvent.VK_F1 + ind, KeyEvent.CHAR_UNDEFINED);

        GdPos.panel.queue.postEvent(e);
        GdPos.playSound("touch/click.wav");
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        UtilLog4j.logInformation(this.getClass(), "state=" + state);

        for (int i = 0; i < DYNAKEY_BUTTONS; i++) {
            ((Dynakey) getComponent(i)).setVisible(state != 0);
        }

        String name = (substate > 0 ? "POS_DS" : "POS_DK") + FmtIo.editNum(state, 2);
        File f = DatIo.local("b24", name + ".GIF");

        if (f.exists()) {
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics g = image.createGraphics();
            icon.paintIcon(null, g, 0, 0);
            g.dispose();
        } else {
            image = null;
        }

        this.state = state;
        downKey = -1;
        showTouch(false);
        repaint();
    }

    public int getDownKey() {
        return downKey;
    }

    public int getSubstate() {
        return substate;
    }

    public void setSubstate(int substate) {
        UtilLog4j.logInformation(this.getClass(), "substate=" + substate);
        this.substate = substate;
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }

    public String getListId() {
        try {
            return chooser.getName().substring(4, 8);
        } catch (Exception exception) {
            return "    ";
        }
    }

    public Component getChooser() {
        return this.chooser;
    }

    public BufferedImage getImage() {
        return image;
    }

}
