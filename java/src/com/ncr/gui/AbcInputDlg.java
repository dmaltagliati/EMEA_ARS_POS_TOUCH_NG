package com.ncr.gui;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AbcInputDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4252226765923442615L;
	private JPanel abcInputPanel = new JPanel();
	private GdLabel info;
	private GdLabel inputLetter;
	private GdLabel inputString;
	private StringBuffer input = new StringBuffer("");
	private StringBuffer validChars = new StringBuffer("");
	private int inputIndex = 0;
	private int validCharsIndex = 0;

	public AbcInputDlg(String title, final StringBuffer validChars) {
		super(title);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("AbcInputDlg", "Bounds"));
		abcInputPanel.setLayout(new BorderLayout());

		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("AbcInputDlg", "Label");
		info = (GdLabel) labelsMap.get("Info");
		inputLetter = (GdLabel) labelsMap.get("InputLetter");
		inputString = (GdLabel) labelsMap.get("InputString");

		String s = "<html><p><table>";
		s += "<tr><td align='right'>        <b>SU</b>:    </td><td>Lettera successiva</td></tr>";
		s += "<tr><td align='right'>        <b>GIU</b>:    </td><td>Lettera precedente</td></tr";
		s += "<tr><td align='right'>        <b>DESTRA</b>:    </td><td>Vai al carattere successivo</td></tr";
		s += "<tr><td align='right'>        <b>SINISTRA</b>:    </td><td>Torna al carattere precedente</td></tr";
		s += "<tr><td align='right'>        <b>INVIO</b>:    </td><td>Conferma il codice inserito</td></tr";
		s += "</table><br></p></html>";
		info.setText(s);

		if (validChars != null) {
			this.validChars = validChars;
		} else {
			this.validChars = new StringBuffer("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
		}

		abcInputPanel.add(info, BorderLayout.NORTH);
		abcInputPanel.add(inputLetter, BorderLayout.CENTER);
		abcInputPanel.add(inputString, BorderLayout.SOUTH);
		add(abcInputPanel);

		input.append(this.validChars.charAt(validCharsIndex));
		inputLetter.setText(String.valueOf(this.validChars.charAt(validCharsIndex)));
		setInputStringText();
	}

	public void modalMain(int sts) {

		UtilLog4j.logInformation(this.getClass(),
				"Called with " + sts + " and Looking for 0x" + Integer.toHexString(Action.input.key));

		if (Action.input.key == ConIo.CLEAR) {
			if (Action.input.max > 0 && sts > 0) {
				Action.input.reset("");
				setInput("");
				return;
			}
		}

		if (sts > 0) {
			super.modalMain(sts);
			return;
		}

		if (validCharsIndex < 0) {
			validCharsIndex = 0;
		}

		if (Action.input.key == ConIo.NORTH) {
			if (++validCharsIndex >= validChars.length()) {
				validCharsIndex = 0;
			}
			input.setCharAt(inputIndex, validChars.charAt(validCharsIndex));
		} else if (Action.input.key == ConIo.SOUTH) {
			if (--validCharsIndex < 0) {
				validCharsIndex = validChars.length() - 1;
			}
			input.setCharAt(inputIndex, validChars.charAt(validCharsIndex));
		}

		if (Action.input.key == ConIo.RIGHT) {
			if (inputIndex == input.length() - 1) {
				if (Action.input.num < Action.input.max) {
					input.append(validChars.charAt(validCharsIndex = 0));
					inputIndex++;
				}
			} else {
				validCharsIndex = validChars.toString().indexOf(String.valueOf(input.charAt(++inputIndex)));
			}
		} else if (Action.input.key == ConIo.LEFT) {
			if (inputIndex > 0) {
				validCharsIndex = validChars.toString().indexOf(String.valueOf(input.charAt(--inputIndex)));
			}
		}

		inputLetter.setText(String.valueOf(validChars.charAt(validCharsIndex)));

		if (Action.input.key == ConIo.NORTH || Action.input.key == ConIo.SOUTH || Action.input.key == ConIo.LEFT
				|| Action.input.key == ConIo.RIGHT) {
			Action.input.pb = input.toString();
			Action.input.num = input.length();
			Action.input.echo();
			setInputStringText();
			return;
		}

		if (Action.input.key == ConIo.CLEAR) {
			sts = 0;
			// FRG-TRANSAZ.RESO#A BEGIN
			if (Action.input.num > 0) {
				Action.input.reset("");
			} else {
				super.modalMain(sts = 0);
			}
			return;
			// FRG-TRANSAZ.RESO#A END
		}

		UtilLog4j.logInformation(this.getClass(), "SCANNED <" + Action.input.pb + ">");

		super.modalMain(sts);

	}

	public void setInput(String pb) {
		input = new StringBuffer(pb);
		// MMS-LOTTERY-VAR1#A BEGIN
		if (pb.length() == 0) {
			input.append(validChars.charAt(0));
		}
		inputIndex = input.length() - 1;
		inputLetter.setText(String.valueOf(input.charAt(inputIndex)));
		// MMS-LOTTERY-VAR1#A END
		validCharsIndex = validChars.toString().indexOf(String.valueOf(input.charAt(inputIndex)));
		setInputStringText();
	}

	private void setInputStringText() {
		inputString.setText("<html>" + input.substring(0, inputIndex) + "<span style=\"background-color: #FFFF00\">"
				+ input.charAt(inputIndex) + "</span>" + input.substring(inputIndex + 1) + "</html>");
	}
}
