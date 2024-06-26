package com.ncr.gui;

import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JDialog;
import javax.swing.JProgressBar;

import com.ncr.Action;
import com.ncr.ConIo;
import com.ncr.DevIo;
import com.ncr.GdPos;
import com.ncr.TouchMenuParameters;
import com.ncr.UtilLog4j;

import jpos.JposException;

public class Modal extends JDialog {

	private static final long serialVersionUID = 3396876640262724L;
	public ConIo input = Action.input;
	//public GdPos panel = Action.panel;
	public Component kbrd = this;
	public Component bounds = GdPos.panel.pnlRoll;
	public int code = ConIo.ENTER;
	protected int max;
	protected int flg;
	protected int msk;
	protected int pnt;
	protected boolean forceTimer = false;
	public boolean block = true, touchy;
	private static long createTime = 0; // MAL-MANTIS-17691#A
	public static final int AUTOCLOSE = 0;
	public static final int CLEAR = 1;
	public static final int CONFIRM = 2;
	public IdleLoopThread idleLoopThread = new IdleLoopThread(new Object());
	private boolean enableScanner = false;


	protected JProgressBar automaticClosingProgressBar = new JProgressBar();
	public Thread automaticClosingThread = new Thread(new Runnable() {
		public void run() {
			long startTime = System.currentTimeMillis();
			automaticClosingProgressBar.setValue(0);
			automaticClosingProgressBar.setMinimum(0);
			automaticClosingProgressBar.setMaximum(TouchMenuParameters.getInstance().getCloseModalAfterMs());

			automaticClosingProgressBar.setVisible(true);
			UtilLog4j.logInformation(this.getClass(), "Started...");
			while (System.currentTimeMillis() < startTime + automaticClosingProgressBar.getMaximum()) {
				automaticClosingProgressBar.setValue((int) (System.currentTimeMillis() - startTime));
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					UtilLog4j.logInformation(this.getClass(), "Interrupted!");
					return;
				}
				if (Thread.currentThread().isInterrupted()) {
					UtilLog4j.logDebug(this.getClass(), "Interrupted!");
					return;
				}
			}
			UtilLog4j.logDebug(this.getClass(), "Sending CLEAR...");
			GdPos.panel.innerVoice(ConIo.CLEAR);
			UtilLog4j.logDebug(this.getClass(), "End");
		}
	});

	// MMS-JUNIT
	public GdLabel errorLabel = null;

	public GdLabel getErrorLabel() {
		return errorLabel;
	}

	protected void setErrorLabel(GdLabel errorLabel) {
		this.errorLabel = errorLabel;
	}

	// MAL-MANTIS-17691#A BEGIN
	public long getCreateTime() {
		return createTime;
	}
	// MAL-MANTIS-17691#A END // MMS-JUNIT

	public Modal(String title) {
		//super(GdPos.panel.touchMenuFrame != null ? GdPos.panel.touchMenuFrame : GdPos.panel.frame, title, true);
		super(GdPos.panel.frame, title, true);
		UtilLog4j.logInformation(this.getClass(), getName() + "; title=" + title);

		flg = Action.input.flg;
		max = Action.input.max;
		msk = Action.input.msk;
		pnt = Action.input.pnt;
		UtilLog4j.logInformation(this.getClass(),
				"Saving ConIo Status: flg=" + flg + "; max=" + max + "; msk=" + msk + "; pnt=" + pnt);

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		if (GdPos.arsGraphicInterface != null) {
			GdPos.arsGraphicInterface.setUndecorated(this, true);
		}
		getRootPane().setOpaque(false);
		setResizable(false);

		addWindowListener(new WindowAdapter() {
			public void windowActivated(WindowEvent e) {
				try {
					DevIo.wdge.keyb.setDeviceEnabled(true);
				} catch (JposException ex) {
				}
				UtilLog4j.logInformation(this.getClass(), getKbrd().getName() + " is requesting the focus");
				getKbrd().requestFocus();
			}

			public void windowClosing(WindowEvent e) {
				modalMain(Action.input.key = 0);
			}
		});

	}

	public void show(String sin) {
		show(sin, false, false);
	}

	public void show(String sin, boolean hidden, boolean enableScanner) {

		//if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) { //MMS-DIGITALRECEIPT#D
		if (forceTimer || (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService())) { //MMS-DIGITALRECEIPT#A

			try {
				automaticClosingThread.interrupt();
				automaticClosingThread.join(500);
			} catch (InterruptedException e1) {
			}
			automaticClosingThread.start();
		}

		UtilLog4j.logInformation(this.getClass(), "Interrupting WaitThread because Modal is going to be shown");
		GdPos.panel.interruptWaitTread();
		this.enableScanner = enableScanner;
		GdPos.panel.modal = this;

		UtilLog4j.logInformation(this.getClass(), getKbrd().getName() + " adding focus listener.");
		getKbrd().addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				if (System.getProperty("DISABLEHACKFOCUS", "false").equalsIgnoreCase("false")) {
					UtilLog4j.logInformation(this.getClass(), getKbrd().getName());
					if (!e.isTemporary()) {
						UtilLog4j.logInformation(this.getClass(), getKbrd().getName() + " is requesting the focus");
						getKbrd().requestFocus();
					}
				}
			}
		});

		KeyListener[] keyListeners = (KeyListener[]) getListeners(KeyListener.class);
		for (int i = 0; i < keyListeners.length; i++) {
			getKbrd().removeKeyListener(keyListeners[i]);
		}
		UtilLog4j.logInformation(this.getClass(), "Adding modalKeyListener");
		getKbrd().addKeyListener(GdPos.panel.modalKeyListener);

		UtilLog4j.logInformation(this.getClass(), getKbrd().getName() + " has modalKeyListener");

		GdPos.panel.setKeyListeners();

		if (!hidden) {
			GdPos.panel.splashThread.interrupt();
		}

		Action.showShort(sin, 0);

		if (this instanceof ClrDlg ) {
			GdPos.panel.smoke(true);
		}

		if (hidden) {
			setSize(1, 1);
		}

		UtilLog4j.logInformation(this.getClass(), "before setEnabled " + enableScanner);
		DevIo.setEnabled(enableScanner);
		UtilLog4j.logInformation(this.getClass(), "after setEnabled " + enableScanner);

		GdPos.playBeep();

		// Il display operatore deve essere scritto appena prima
		// che appaia la modale e sicuramente dopo che siano stati
		// settati i listener della keyboard.
		// Succedeva che la Fastlane premesse un correttore ancora
		// prima che la modale potesse intercettarlo.
		if (this instanceof ClrDlg) {
			DevIo.oplDisplay(0, getTitle());
		}

		// MMS-R10-MODAL#A BEGIN
		// necessario perch� il ciclo di Idle principale � stato accodato dall'executor e bloccato dal thread della modale
		idleLoopThread.start();
		// MMS-R10-MODAL#A END
		UtilLog4j.logInformation(this.getClass(), "Modal Show!");
		createTime = System.currentTimeMillis(); // MAL-MANTIS-17691#A

		// if (GdPos.existSecondScreen(1)) {
		// GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice[] gd = ge.getScreenDevices();
		// this.setLocation(gd[1].getDefaultConfiguration().getBounds().x + getX(), getY());
		// }

		super.setVisible(true);

		//if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) { //MMS-DIGITALRECEIPT#D
		if (forceTimer || (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService())) { //MMS-DIGITALRECEIPT#A
			try {
				automaticClosingThread.interrupt();
				automaticClosingThread.join(500);
			} catch (InterruptedException e1) {
			}
			TouchMenuParameters.getInstance().restart();
		}

		UtilLog4j.logInformation(this.getClass(), "Modal Closed!");
		// Spostati qui perch� al termine del ModalThread la modale deve essere null
		// per permettere all'eventuale pressione tasto successiva di essere catturata
		// dall'EventMainThread. Modifica fatta per i tasti da Fastlane.
		GdPos.panel.modal = null;
		GdPos.panel.setKeyListeners();
		GdPos.panel.refreshModals();

		UtilLog4j.logInformation(this.getClass(), "Enabling Scanners!");
		DevIo.setEnabled(true);

		if (this instanceof ClrDlg ) {
			GdPos.panel.smoke(false);
		}

		// MMS-R10-MODAL#A BEGIN
		try {
			idleLoopThread.interrupt();
			idleLoopThread.join(500);
		} catch (InterruptedException e) {
		}

		UtilLog4j.logInformation(this.getClass(), "Modal Hide!");
		// MMS-R10-MODAL#A END

	}

	public boolean isEnableScanner() {
		return enableScanner;
	}

	public void modalMain(int sts) {

		UtilLog4j.logInformation(this.getClass(),
				"Called with " + sts + " and Looking for 0x" + Integer.toHexString(Action.input.key));
		if (Action.input.key == 0x2f2f) {
			Action.input.reset("");

			return;
		}
		if (Action.input.key == ConIo.CLEAR) {
			if (Action.input.max > 0 && sts > 0) {
				if (this instanceof ClrDlg ) {
					UtilLog4j.logInformation(this.getClass(), "");
				} else {
					Action.input.reset("");

					return;
				}
			}
		}
		setCode(sts);

		// Restoring ConIo status
		UtilLog4j.logInformation(this.getClass(),
				"Restoring ConIo Status: flg=" + flg + "; max=" + max + "; msk=" + msk + "; pnt=" + pnt);
		Action.input.flg = flg;
		Action.input.max = max;
		Action.input.msk = msk;
		Action.input.pnt = pnt;

		UtilLog4j.logInformation(this.getClass(), "Modal Unloded!");
		dispose();
		Action.showShort("DLG", 0);

	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public Component getKbrd() {
		return kbrd;
	}

	public void add(int foo, String key, Object object) {
	}

	public void add(int foo, String key, String text) {
	}

	public void setEcho(boolean echo) {
	}

	public void itemEcho(int i) {
	}

	public void press(int key) {
		UtilLog4j.logInformation(this.getClass(), "key=" + key);
		char c = (char) (key);

		KeyEvent ke = new KeyEvent(GdPos.panel.modal == null ? GdPos.panel.kbrd : GdPos.panel.modal.getKbrd(),
				KeyEvent.KEY_PRESSED, System.currentTimeMillis(), 0, key, c);
		GdPos.panel.queue.postEvent(ke);

	}
}
