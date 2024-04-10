package com.ncr;

import javax.swing.*;

public class GoToPosManager extends Action {
	private static GoToPosManager instance;
	private boolean posHybrid;

	// Singleton
	public static GoToPosManager getInstance() {
		if (instance == null) {
			instance = new GoToPosManager();
		}
		return instance;
	}

	public boolean isPoshybrid() {
		return posHybrid;
	}

	public void setPoshybrid(boolean poshybrid) {
		UtilLog4j.logInformation(this.getClass(), "setPoshybrid " + poshybrid);
		this.posHybrid = poshybrid;
	}

	// gotopos in
	public int action1(int spec) {
		UtilLog4j.logInformation(this.getClass(), "action1");
		posHybrid = true;
		UtilLog4j.logInformation(this.getClass(), "set state normal");
		GdPos.f.setState(JFrame.NORMAL);

		if (Struc.options[Struc.O_Autho] < 2) {
			UtilLog4j.logInformation(this.getClass(), "forzo la chiave virtuale");
			Struc.options[Struc.O_Autho] = 2;
			ConIo.optAuth = 2;
		}

		GdPos.panel.refreshModals();
		GdPos.panel.requestFocus();
		return 0;
	}

	// gotopos out
	public int action2(int spec) {
		UtilLog4j.logInformation(this.getClass(), "action2");

		posHybrid = false;
		UtilLog4j.logInformation(this.getClass(), "set state ICONIFIED");
		GdPos.f.setState(JFrame.ICONIFIED);

		if (Struc.options[Struc.O_Autho] == 2) {
			UtilLog4j.logInformation(this.getClass(), "forzo la chiave fisica");
			Struc.options[Struc.O_Autho] = 0;
			ConIo.optAuth = 0;
		}

		GdPos.panel.refreshModals();
		return 0;
	}

	public boolean isFLAndTouch() {
		if (GdPos.panel.keyPadDialog != null && false) { // Se � il Pos Touch e FL
			UtilLog4j.logDebug(this.getClass(), "true");
			return true;
		}
		UtilLog4j.logDebug(this.getClass(), "false");
		return false;
	}
}
