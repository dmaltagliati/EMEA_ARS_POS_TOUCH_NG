//MMS-JUNIT

package com.ncr.gui;

import com.ncr.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class TestDlg extends Modal implements ItemListener, ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 7870699139032778736L;
    public List list = null;
    List sublist = new List(3);

    public TestDlg(String title) {
        super(title);
        Init(title, 0);
    }

    private void Init(String title, long timeout) {
        setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("SelDlg", "Bounds"));
        add(new GdLabel(null, GdLabel.STYLE_STATUS), BorderLayout.CENTER);

        list = new List(File.separatorChar == '/' ? 9 : 10);

        Panel p = new Panel();
        p.setLayout(new BorderLayout(0, 0));
        p.add(list, BorderLayout.CENTER);
        p.add(sublist, BorderLayout.SOUTH);
        add(p);
        list.addItemListener(this);
        list.addActionListener(this);
        sublist.setBackground(Color.getColor("COLOR_SCROLLBAR", SystemColor.scrollbar));
        sublist.setForeground(Color.darkGray);
        list.setFont(GdPos.panel.journalTable.getFont());
        sublist.setFont(GdPos.panel.journalTable.getFont());
        hashMap.clear();
        ind = 0;
    }

    public void show(String sin) {
        super.show(sin, false, false);
    }

    public void modalMain(int sts) {

        if (Action.input.key == 0x2f2f) {
            Action.input.reset("");
            return;
        }
        int ind = list.getSelectedIndex();

        if (Action.input.key == ConIo.CLEAR) {
            if (ind >= 0) {
                sts = 0;
            }
        }
        if (Action.input.key == ConIo.NORTH) {
            if (ind < 0) {
                ind = list.getItemCount();
            }
            itemEcho(--ind);
            return;
        }
        if (Action.input.key == ConIo.SOUTH) {
            itemEcho(++ind);
            return;
        }
        super.modalMain(sts);

    }

    public void add(int pos, String key, String text) {
        String s;

        if (key.length() != 0) { // EMEA-02017-SBE#C
            s = FmtIo.editTxt(key, pos) + ".";
        } else {
            s = FmtIo.editTxt(key, pos);
        }

        list.add(text == null ? s : s + text);

    }

    public void itemEcho(int index) {
        if (index < 0 || index >= hashMap.keySet().size()) {
            return;
        }

        sublist.removeAll();
        LinkedList linkedList = (LinkedList) hashMap.get(String.valueOf(index));

        if (linkedList != null) {
            Iterator iterator = linkedList.iterator();

            while (iterator.hasNext()) {
                String s = (String) iterator.next();

                s = s.substring(0, Math.min(s.length(), 40));
                sublist.add(s);
            }
        }

        super.itemEcho(index);
    }

    HashMap hashMap = new HashMap();
    private static int ind = 0;

    public void addSublist(LinkedList sublist) {
        String key = String.valueOf(ind++);
        LinkedList strings = (LinkedList) hashMap.get(key);

        if (strings == null) {
            strings = new LinkedList();
        }
        strings.addAll(sublist);
        hashMap.put(key, strings);
    }

    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            itemEcho(-1);
        }
    }

    public void actionPerformed(ActionEvent e) {
        Action.input.key = getCode();
        modalMain(0);
    }

}
