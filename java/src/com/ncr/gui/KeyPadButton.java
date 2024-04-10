package com.ncr.gui;

import com.ncr.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

public class KeyPadButton extends GdLabel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -305101861651247343L;
	private Color pressedColor = new Color(100, 100, 100, 150);
	private Color normalColor = new Color(0, 0, 0, 0);

	private BufferedImage image = null;
	private BufferedImage imageFlipped = null;

	private int keyCode = 0;
	private char keyChar = 0; // MMS-LOTTERY-VAR1#A
	private boolean ignoreEvents = false;

	private MouseListener mouseListener = new MouseListener() {
		public void mouseReleased(MouseEvent e) {
			Struc.tra.testStarted = true;
			setBackground(normalColor);
			if (GdPos.panel.waitPanel.isVisible()) {
				UtilLog4j.logInformation(this.getClass(),
						"Clessidra in corso... Scarto la pressione del bottone: " + e.getSource());
				e.consume();
				return;
			}

			if (keyCode == 0x00) {
				e.consume();
				return;
			}

			// Delay di un secondo prima di premere nuovamente il tasto per evitare
			// di fare un doppio storno o doppia vendita
			if (keyCode == ConIo.VOIDCURRENT || keyCode == ConIo.SELLCURRENT) {
				if (ignoreEvents) {
					UtilLog4j.logInformation(this.getClass(),
							"KeyCode Ignored (" + Integer.parseInt("" + keyCode, 16) + ")");
					e.consume();
					return;
				}
				ignoreEvents = true;
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
						}
						ignoreEvents = false;
					}
				}).start();
			}

			if (keyCode == ConIo.VOIDCURRENT) {
				DevIo.postInput("VOIDCURRENT", null); // MMS-JUNIT-20190917#A
			} else if (keyCode == ConIo.SELLCURRENT) {
				UtilLog4j.logInformation(this.getClass(), "Sell Selected Element");
				JournalAndDetail.getInstance().sellCurrent();
			} else {
				KeyEvent kPressed = new KeyEvent(
						GdPos.panel.modal == null ? GdPos.panel.kbrd : GdPos.panel.modal.getKbrd(),
						KeyEvent.KEY_PRESSED, e.getWhen(), e.getModifiers(), keyCode, keyChar);

				UtilLog4j.logInformation(this.getClass(), "Add KeyEvent to Queue: " + kPressed);
				GdPos.panel.queue.postEvent(kPressed);

			}

			// Eliminare il test se si vuole lasciare la barra estesa aperta
			// alla pressione di un tasto dell'Extender
			if (keyCode != KeyEvent.VK_HOME) {
				GdPos.panel.bottomBarDialog.closeExtener();
			}

			GdPos.playSound("touch/click.wav");
		}

		public void mousePressed(MouseEvent e) {
			setBackground(pressedColor);
		}

		public void mouseExited(MouseEvent e) {
			setBackground(normalColor);
		}

		public void mouseEntered(MouseEvent e) {
			setBackground(normalColor);
		}

		public void mouseClicked(MouseEvent e) {
		}
	};

	public KeyPadButton() {
		this(null);
	}

	public KeyPadButton(String text) {
		this(text, 0);
	}

	public KeyPadButton(String text, int keyCode) {
		super(text, STYLE_NONE);

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		this.keyCode = keyCode;
		setOpaque(false);
		setBackground(normalColor);
		addMouseListener(mouseListener);
	}

	static BufferedImage deepCopy(BufferedImage bi) {
		ColorModel cm = bi.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = bi.copyData(null);

		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
	}

	// TODO MMS-R10 Dovrebbe essere usata la setIcon(...) e non la vecchia
	// modalit� con "image" e paintComponent
	protected void paintComponent(Graphics g) {
		try {
			if (image != null) {
				if (getName().equals("KeyOpen") && GdPos.panel.bottomBarDialog.isOpened()) {
					g.drawImage(imageFlipped, 0, 0, getWidth(), getHeight(), null);
				} else {
					g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
				}
			}
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		} catch (Exception exception) {
		}

		super.paintComponent(g);
	}

	public void setImage(BufferedImage image) {
		if (image != null) {
			imageFlipped = deepCopy(image);
			AffineTransform tx = AffineTransform.getScaleInstance(1, -1);

			tx.translate(0, -imageFlipped.getHeight(null));
			AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);

			imageFlipped = op.filter(imageFlipped, null);
		}
		this.image = image;

	}

	public void setPressedColor(Color pressedColor) {
		this.pressedColor = pressedColor;
	}

	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	// MMS-LOTTERY-VAR1#A BEGIN
	public void setKeyChar(char keyChar) {
		this.keyChar = keyChar;
	}
	// MMS-LOTTERY-VAR1#A END
}
