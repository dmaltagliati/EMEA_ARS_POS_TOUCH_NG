package com.ncr;

public class TouchMenuParameters extends Action {

	private static TouchMenuParameters touchMenuManager;
	private boolean enabled = false;
	private int type = 0;
	private boolean forceNoSupervisor = false;
	private String login = "";
	private int resetAfterMs = 0;
	private int closeModalAfterMs = 0;
	private TouchMenuThread resetAfterMsThread = new TouchMenuThread();
	private boolean isFullScreen = false;

	private TouchMenuParameters() {
	}

	public static TouchMenuParameters getInstance() {
		if (touchMenuManager == null) {
			touchMenuManager = new TouchMenuParameters();
		}
		return touchMenuManager;
	}

	public void loadParameter(String text, int line) {
		switch (line) {
		case 0:
			enabled = Integer.parseInt(text.substring(0, 2)) != 0;
			type = Integer.parseInt(text.substring(2, 4));
			forceNoSupervisor = Integer.parseInt(text.substring(4, 6)) != 0;
			login = text.substring(6, 12).trim();
			isFullScreen = Integer.parseInt(text.substring(12, 14)) != 0;
			resetAfterMs = Integer.parseInt(text.substring(14, 18)) * 1000;
			closeModalAfterMs = Integer.parseInt(text.substring(18, 22)) * 1000;
			break;

		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		case 8:
		case 9:
		default:
			break;
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isSelfService() {
		return type == 1 && enabled;
	}

	public boolean isPriceVerifier() {
		return type == 2 && enabled;
	}

	public boolean isCampero() {
		return type == 3 && enabled;
	}

	public boolean isForceNoSupervisor() {
		return forceNoSupervisor && enabled;
	}

	public boolean isFullScreen() {
		return isFullScreen && enabled;
	}

	public int getResetAfterMs() {
		if (!enabled) {
			return 0;
		}
		return resetAfterMs;
	}

	public int getCloseModalAfterMs() {
		if (!enabled) {
			return 0;
		}
		return closeModalAfterMs;
	}

	public void login() {
		try {
			if (login.trim().length() > 0 && Integer.parseInt(login) != 0) {
				Action.input.reset(login);
				DevIo.postInput("CODE000d", null);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void logout() {
		try {
			Action.input.reset("");
			DevIo.postInput("CODE00a2", null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void restart() {
		resetAfterMsThread.start(getResetAfterMs());
	}

	// MMS-TOUCHMENU#A BEGIN
	public int action0(int spec) {
		if (GdPos.panel.touchMenu != null) {
			GdPos.panel.touchMenu.toggleVisible();
		}
		return 0;
	}
	// MMS-TOUCHMENU#A END

}
