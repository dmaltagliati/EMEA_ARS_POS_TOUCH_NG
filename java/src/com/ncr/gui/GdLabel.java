package com.ncr.gui;

import com.ncr.Config;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener; //MMS-JUNIT
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import java.awt.*;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;

import com.ncr.DatIo;
import com.ncr.UtilLog4j;

public class GdLabel extends JLabel {

	private static final long serialVersionUID = -3167514893256905740L;
	public static final int STYLE_NONE = 0;
	public static final int STYLE_RAISED = 1;
	public static final int STYLE_STATUS = 2;
	public static final int STYLE_WINDOW = 3;
	public static final int STYLE_HEADER = 4;

	public String text;
	private Point pad = new Point(7, 3);
	private int style = 0;
	private String prefix = null;
	private String suffix = null;

	public static Color colorMenu = Color.getColor("COLOR_MENU", SystemColor.menu);
	public static Color colorMenuText = Color.getColor("COLOR_MENUTEXT", SystemColor.menuText);
	public static Color colorWindow = Color.getColor("COLOR_WINDOW", SystemColor.window);
	public static Color colorWindowText = Color.getColor("COLOR_WINDOWTEXT", SystemColor.windowText);
	public static Color colorControl = Color.getColor("COLOR_CONTROL", SystemColor.control);
	public static Color colorControlText = Color.getColor("COLOR_CONTROLTEXT", SystemColor.controlText);
	public static Color colorActiveCaption = Color.getColor("COLOR_ACTIVECAPTION", SystemColor.activeCaption);
	public static Color colorActiveCaptionText = Color.getColor("COLOR_ACTIVECAPTIONTEXT", SystemColor.activeCaptionText);
	public static Color colorInactiveCaption = Color.getColor("COLOR_INACTIVECAPTION", SystemColor.inactiveCaption);
	public static Color colorInactiveCaptionText = Color.getColor("COLOR_INACTIVECAPTIONTEXT", SystemColor.inactiveCaptionText);
	private String currentState = "Normal";

	private HashMap stateMap = new HashMap();

	public void setStateMap(HashMap stateMap) {
		this.stateMap.putAll(stateMap);
	}

	public GdLabel(String text) {
		this(text, STYLE_NONE);
	}

	public GdLabel(String text, int style) {
		super();
		setText(text);
		setStyle(style);
		setAlerted(false);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		if (stateMap != null && !stateMap.isEmpty()) {
			if (enabled) {
				setBackground((Color) ((HashMap) stateMap.get(currentState)).get("BackgroundColor"));
			} else {
				setBackground((Color) ((HashMap) stateMap.get("Disable")).get("BackgroundColor"));
			}
		}
	}

	public void setText(String text) {
		if (text == null || text.length() == 0) {
			super.setText("");
		} else {
			super.setText((prefix == null ? "" : prefix) + (text == null ? "" : text) + (suffix == null ? "" : suffix));
		}
	}
	public int getStyle() {
		return style;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	public static BufferedImage getThreeWayGradient(Rectangle rectangle, Color primaryLeft, Color primaryRight,
			Color shadeColor) {
		BufferedImage image = new BufferedImage(rectangle.width, rectangle.height, BufferedImage.TYPE_4BYTE_ABGR);

		Graphics2D g = image.createGraphics();
		GradientPaint primary = new GradientPaint(0f, 0f, primaryLeft, rectangle.width, 0f, primaryRight);
		int rC = shadeColor.getRed();
		int gC = shadeColor.getGreen();
		int bC = shadeColor.getBlue();
		GradientPaint shade = new GradientPaint(0f, 0f, new Color(rC, gC, bC, 0), 0f, rectangle.height, shadeColor);

		g.setPaint(primary);
		g.fillRect(0, 0, rectangle.width, rectangle.height);
		g.setPaint(shade);
		g.fillRect(0, 0, rectangle.width, rectangle.height);

		g.dispose();
		return image;
	}

	public boolean isInState(String state) {
		return currentState.equals(state);
	}

	public boolean isAlerted() {
		return currentState.equals("Alerted");
	}

	public void setState(String state) {
		Color color;
		BufferedImage image;
		Font font;
		currentState = state;
		HashMap stateParameterMap = (HashMap) stateMap.get(state);
		if (stateParameterMap != null) {
			color = (Color) stateParameterMap.get("BackgroundColor");
			if (color != null) {
				setBackground(color);
			}
			color = (Color) stateParameterMap.get("ForegroundColor");
			if (color != null) {
				setForeground(color);
			}
			font = (Font) stateParameterMap.get("Font");
			if (font != null) {
				setFont(font);
			}
			image = (BufferedImage) stateParameterMap.get("Image");
			if (image != null) {
				setImage(image);
			}
		} else if (state.equals("Alerted")) {
			switch (style) {
				case STYLE_RAISED:
					image = getThreeWayGradient(new Rectangle(0, 0, 100, 100), Color.red, Color.white, Color.red);
					setImage(image);
					break;
				case STYLE_STATUS:
					image = getThreeWayGradient(new Rectangle(0, 0, 100, 100), Color.yellow, Color.white, Color.yellow);
					setImage(image);
					break;
				case STYLE_WINDOW:
					image = getThreeWayGradient(new Rectangle(0, 0, 100, 100), Color.green, Color.white, Color.green);
					setImage(image);
					break;
			}
		}
	}

//	public Dimension getPreferredSize() {
//		Dimension d = getCharSize();
//		if (text != null)
//			d.width *= text.length();
//		d.height += d.height >> 3;
//		d.width += pad.x << 1;
//		d.height += pad.y << 1;
//		return d;
//	}

	public void setAlerted(boolean state) {
		if (state) {
			setState("Alerted");
		} else {
			setState("Normal");
		}
	}


//	public void setPicture(String name) {
//		if (name == null)
//			setImage(null);
//		else
//			setImage(Config.localFile("gif", name + ".GIF"));
//	}

	public BufferedImage getBufferedImage() {
		if (getIcon() == null) {
			return null;
		}
		// Conversione da Icon a BufferedImage
		BufferedImage image = new BufferedImage(getIcon().getIconWidth(), getIcon().getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.createGraphics();
		getIcon().paintIcon(null, g, 0, 0);
		g.dispose();
		return image;
	}

	public void setImage(BufferedImage image) {
		if (image == null) {
			setIcon(null);
			return;
		}
		ImageIcon icon = new ImageIcon(image);
		if (getWidth() > 0 && getHeight() > 0) {
			Image scaledImage = icon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
			icon = new ImageIcon(scaledImage);
		}
		setIcon(icon);
	}

	public void setImageFromUrl(String imageUrl) {
		if (imageUrl == null || imageUrl.trim().length() == 0) {
			if (getIcon() == null) {
				return;
			}
			setIcon(null);
			repaint();
		}
		try {
			URL url = new URL(imageUrl);
			ImageIcon icon = new ImageIcon(url);
			Image scaledImage = icon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);

			icon = new ImageIcon(scaledImage);
			setIcon(icon);
		} catch (Exception exception) {
			UtilLog4j.logWarning(this.getClass(), "Immagine non valida.", exception);
			setIcon(null);
		}
		repaint();

	}

	public void setImage(String path, String name) {
		UtilLog4j.logDebug(this.getClass(), path + ", " + name);
		if (path == null && name == null) {
			if (getIcon() == null) {
				return;
			}
			setIcon(null);
			return;
		}
		try {
			File f = DatIo.local(path, name);
			if (f.exists()) {
				ImageIcon icon = new ImageIcon(f.getAbsolutePath());
				if (getWidth() > 0 && getHeight() > 0) {
					Image scaledImage = icon.getImage().getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT);
					icon = new ImageIcon(scaledImage);
				}
				setIcon(icon);
			} else {
				setIcon(null);
			}
		} catch (Exception exception) {
			UtilLog4j.logWarning(this.getClass(), "Immagine non valida.", exception);
			setIcon(null);
		}
		repaint();
	}

	public synchronized void addMouseListener(MouseListener l) {
		MouseListener[] mouseListener = (MouseListener[]) getListeners(MouseListener.class);

		for (int i = 0; i < mouseListener.length; i++) {
			super.removeMouseListener(mouseListener[i]);
		}
		super.addMouseListener(l);
	}

	private String slidingString = "";
	private int slidingLen = 0;
	private int index;
	public boolean slide = false;

	public void slide(int maxSize) {
		if (!slide) {
			return;
		}

		TimerTask timerTask = new TimerTask() {

			public void run() {

				if (!isValid()) {
					cancel();
				}

				index++;
				if (index > slidingString.length() - slidingLen) {
					index = 0;
				}
				setText(slidingString.substring(index, index + slidingLen));
				if (index == slidingString.length() / 3) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		};

		String s = StringUtils.rightPad(getText(), maxSize);
		int n = s.length();
		String sb = new String();
		for (int i = 0; i < n; i++) {
			sb += ' ';
		}
		this.slidingString = sb + s + sb;
		this.index = sb.length();
		this.slidingLen = n;
		// setSize(getPreferredSize());

		Timer timer = new Timer(true);
		timer.schedule(timerTask, 2000, 2000 / (this.slidingString.length()));

	}

	public void setSlide(boolean slide) {
		this.slide = slide;

	}

	public boolean isSlide() {
		return slide;
	}


	public int align = Label.CENTER;

	public void setAlignment(int align) {
		this.align = align;
	}

//	public void setPicture(String name) {
//		if (name == null)
//			setImage(null);
//		else
//			setImage(Config.localFile("gif", name + ".GIF"));
//	}

//	public void paint(Graphics g) {
//		Dimension d = getSize();
//		Color bg = getBackground();
//
//		if (image != null) {
//			if ((checkImage(image, this) & ALLBITS + FRAMEBITS) == 0)
//				return;
//			bg = new Color(bg.getRGB()); // EVM can't handle SystemColor
//			g.drawImage(image, 0, 0, d.width, d.height, bg, null);
//		} else if (ground != null)
//			ground.paintOn(this, g);
//		else
//			g.clearRect(0, 0, d.width, d.height);
//		if (text == null)
//			return;
//
//		Dimension chr = getCharSize();
//		int x = pad.x;
//		int y = (d.height >> 1) + (chr.height >> 2);
//		int len = d.width - chr.width * text.length();
//		if (align == Label.CENTER)
//			x = len >> 1;
//		else if (align == Label.RIGHT)
//			x = len - x;
//		g.setColor(getForeground());
//		if (!isEnabled()) {
//			g.setColor(bg.darker());
//			g.drawString(text, x + 1, y + 1);
//			g.setColor(bg.brighter());
//		}
//		g.drawString(text, x, y);
//	}
}
