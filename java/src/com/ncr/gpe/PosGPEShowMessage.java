package com.ncr.gpe;

import com.ncr.Action;
import com.ncr.GdPos;
import com.ncr.gpe.std.GpeMessagePosDisplayInterface;

public class PosGPEShowMessage extends Action implements GpeMessagePosDisplayInterface {

	public void showMessage(int messageNumber, String messageText) {

		GdPos.panel.display(1, messageText);
		GdPos.panel.display(2, "");
	}
}
