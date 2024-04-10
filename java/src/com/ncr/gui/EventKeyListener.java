package com.ncr.gui;

import com.ncr.*;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;

public class EventKeyListener implements KeyListener {

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public synchronized void keyPressed(KeyEvent e) {
		UtilLog4j.logInformation(this.getClass(), e + "; consumed=" + e.isConsumed());
		if (e.isControlDown() && e.isShiftDown()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_V:
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				Transferable contents = clipboard.getContents(null);

				try {
					Action.input.reset((String) contents.getTransferData(DataFlavor.stringFlavor));
				} catch (UnsupportedFlavorException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				break;

			case KeyEvent.VK_L:
				// Action.input.keyLock(1); //MMS-JUNIT
				DevIo.postInput("LCK1", null); // MMS-JUNIT
				break;
			case KeyEvent.VK_R:
				// Action.input.keyLock(2); //MMS-JUNIT
				DevIo.postInput("LCK2", null); // MMS-JUNIT
				break;
			case KeyEvent.VK_S:
				// Action.input.keyLock(3); //MMS-JUNIT
				DevIo.postInput("LCK3", null); // MMS-JUNIT
				break;
			case KeyEvent.VK_X:
				// Action.input.keyLock(4); //MMS-JUNIT
				DevIo.postInput("LCK4", null); // MMS-JUNIT
				break;

			case KeyEvent.VK_1:
			case KeyEvent.VK_2:
			case KeyEvent.VK_3:
			case KeyEvent.VK_4:
				 Action.input.keyLock(e.getKeyCode() - KeyEvent.VK_1 + 1);
				break;
			// Close cashier and exit from POS (if SUPERVISOR)
			case KeyEvent.VK_END:
				if (ConIo.posLock == 3) {
					DevIo.postInput("CODE00A2", null);
					DevIo.postInput("CODE001B", null);
				}
				break;
			}
			return;
		}

		if (e.getKeyCode() == KeyEvent.VK_SHIFT || e.getKeyCode() == KeyEvent.VK_ALT
				|| e.getKeyCode() == KeyEvent.VK_ALT_GRAPH || e.getKeyCode() == KeyEvent.VK_CONTROL) {
			UtilLog4j.logInformation(this.getClass(), "Skip Modifiers Only");
			e.consume();
			return;
		}

		if (GdPos.panel.modal != null) {
			UtilLog4j.logInformation(this.getClass(),
					"Abort Event Waiting cause a Modal has Shown. Event aborted: " + e);
			e.consume();
			return;
		}

		// FLM-THREADS#D BEGIN
		EventMainThread eventMainThread = new EventMainThread(e);
		GdPos.panel.executorCompletionService.submit(eventMainThread);
		// FLM-THREADS#A END

	}

}
