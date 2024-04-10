package com.ncr;

import com.ncr.notes.Notes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class IdleThread extends Thread {
	private static int idletick = 0;
	private static long milliSec = System.currentTimeMillis();
	private static long deltaMilliSec = -10000;
	private static long eptsCheckDeltaMilliSec = 0;
	private static long msgDeltaMilliSec = 0;
	private static boolean supervisorUnlock = false;
	// MMS-TOUCHMENU#A BEGIN
	private static boolean autoLogin = true;
	// MMS-TOUCHMENU#A END

	private static long id = 0;
	private Object lock = null;

	public IdleThread(Object lock) {
		this.lock = lock;
		setName("IdleThread-" + id++);
	}

	public void run() {
		synchronized (lock) {
			try { // COP-FIX-300662-ADP #a

				GdPos.panel.idleState = 1;
				GdPos.isIdle = true; // MMS-TOUCHMENU

				Struc.ctl.setDatim();

				// MMS-R10
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss", Locale.ITALIAN);

				GdPos.panel.dspArea[5].setText(simpleDateFormat.format(new Date()));
				GdPos.panel.updateInfoLabel();
				// MMS-R10

				deltaMilliSec += (System.currentTimeMillis() - milliSec);
				eptsCheckDeltaMilliSec += (System.currentTimeMillis() - milliSec);
				msgDeltaMilliSec += (System.currentTimeMillis() - milliSec);
				milliSec = System.currentTimeMillis();

				if (idletick > 7) {
					idletick = 0;
				}

				if (msgDeltaMilliSec > 10000) {
					UtilLog4j.logInformation(Action.class, "Idleloop Attivo"); // JLOG
					msgDeltaMilliSec = 0;

					// MMS-R10
					UtilLog4j.logInformation(Action.class, "Memory Free: " + Runtime.getRuntime().freeMemory() / 1024000
							+ "Mb Total: " + Runtime.getRuntime().totalMemory() / 1024000 + "Mb");
					// MMS-R10

				}
				/**
				 * Classe ConIo! posLock: 4 is [X] posLock: 3 is [S] posLock: 2 is [N/R] posLock: 1 is [L] posLock: 0 is [---]
				 */
				if (Action.input.isSupervisor()) {
					supervisorUnlock = true;
				}

				if (Action.netio.state != Struc.ctl.lan) {
					// MMS-R10
					if (GdPos.panel.journalTable != null) {
						GdPos.panel.journalTable.repaint();
					}
					if (GdPos.panel.journalTable2Screen != null) {
						GdPos.panel.journalTable2Screen.repaint();
					}
					// MMS-R10
					// MMS-WATER

					if (Action.netio.state < 3) {
						Struc.ctl.lan = Action.netio.state;
						// MMS-R10
						GdPos.panel.updateServerStatus(Struc.ctl.lan);
						// MMS-R10
						DevIo.oplSignal(0, Struc.ctl.lan);
					}
				}

				// AMZ-ENH-20180920-004#BEG

				//Notes.monitor();
				if (Struc.mon.adv_rec >= 0) {
					Notes.advertize();
				}
				if ((++Struc.mon.tick & 3) > 0) {

					return;
				}
				Action.input.tic++;
				if (Struc.mon.clock >= 0) {
					int mm = (Struc.ctl.time / 100) % 100;

					if (mm != Struc.mon.clock) {
						Struc.hdrLine.init(' ').onto(6, FmtIo.editDate(Struc.ctl.date))
								.onto(15, FmtIo.editTime(Struc.ctl.time / 100)).show(0);
						Struc.mon.clock = mm;
					}
				}
				if (Struc.mon.total >= 0) {
					if ((Struc.mon.total += 2) > 5) {
						Action.showTotal(-1);
					}
				}
				if ((ConIo.optAuth & 2) > 0) {
					Action.input.lck &= 0xF0;
					Action.input.lck |= Struc.ctl.ckr_nbr < 800 && (Action.input.lck & 0x10) == 0 ? 1 : 4;
				}
				// if (DevIo.drwWatch(2)) { //MMS-MANTIS-19251#D
				if (DevIo.drwWatch(2) && GdPos.panel.modal != null) { // MMS-MANTIS-19251#A
					GdPos.panel.innerVoice(ConIo.CLEAR);
				}
				if ((Action.input.tic & 5) == 0) {
					if (Struc.ctl.ckr_nbr > 0 && Struc.mon.rcv_mon != null) {
						if (Struc.mon.rcv_msg.endsWith("!")) {
							DevIo.alert();
						}
					}
				}
				if (eptsCheckDeltaMilliSec > 5000) {
					eptsCheckDeltaMilliSec = 0;
					//PosGPE.checkEptsUPB(false);
				}

				// MMS-TOUCHMENU#A BEGIN
				if (autoLogin) {
					if (TouchMenuParameters.getInstance().isPriceVerifier()
							|| TouchMenuParameters.getInstance().isSelfService()) {
						TouchMenuParameters.getInstance().login();
						autoLogin = false;
					}
				}
				// MMS-TOUCHMENU#A END
				if (Action.input.isEmpty() && GdPos.panel.modal == null) {

					if (Action.event.menu == 0 && Action.event.key == 0 && Action.input.key != ConIo.CLEAR
							&& Action.event.act > 0) {
						if (Action.input.tic == 1) {
							UtilLog4j.logInformation(Action.class, Action.event.pb);
						}
						int code = Action.group[Action.event.act / 10].exec();
						if (code > 0) {
							UtilLog4j.logInformation(Action.class, "" + code);
							GdPos.panel.sequentialInnerVoice(code);
						}
					}

					if (Action.input.tic >= 1 && GdPos.panel.waitThread.isAlive()) {
						GdPos.panel.interruptWaitTread();
					}

				} else {
					if (Struc.mon.lan99 > 0) {
						if ((Action.input.tic & 3) == 0) {
							if (Table.lLAN.read(Struc.mon.lan99, 0) > 0) {
								UtilLog4j.logInformation(Action.class, "Idle Record " + Struc.mon.lan99 + ") " + Table.lLAN.pb);
								if (Table.lLAN.sts >= 90) {
									UtilLog4j.logInformation(Action.class, "Closing PopUp");
									GdPos.panel.innerVoice(ConIo.CLEAR);
								}
							}
						}
					} else {
						if (Struc.mon.image > 0) {
							if (Action.netio.isCopied(Struc.mon.image)) {
								GdPos.panel.innerVoice(ConIo.CLEAR);
							}
						} else {
							if (Struc.ctl.ckr_nbr == 0) {
								if (Action.input.tic > 30) {
									GdPos.panel.innerVoice(ConIo.CLEAR);

								}
							}
						}
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				UtilLog4j.logFatal(Action.class, "Eccezione in IDLELOOP", ex);
			} finally {
				GdPos.panel.idleState = 0;
				lock.notifyAll(); // FLM-SCANNER#A
				UtilLog4j.logDebug(this.getClass(), "Action.Idle has terminating");
				// UtilLog4j.logInformation(this.getClass(), "End");

			}
			// COP-FIX-300662-ADP #a end
		}
	}
}