package com.ncr;

/**
 * Title: Italian BackGround Color Description: This class load the color option
 * from IBKCn line on P_REGPAR.DAT file Copyright: Copyright (c) 2001 Company:
 * 
 * @author Marco Di Carne
 * @version 1.0
 */

import java.awt.*;

public class ItalianBackGroundColor {
	private static final int M_ONLINE = 0; // it is referred to the online
											// background color line of P_REGPAR
											// IBKC0;
	private static final int M_OFFLINE = 1; // it is referred to the offline
											// background color line of P_REGPAR
											// IBKC1;
	private static final int M_OFFLINEREVERSE = 2; // it is referred to the
													// offline reverse
													// background color line of
													// P_REGPAR IBKC2;
													// MMS-LASER-VARIANTE4


	private static final int M_RED1 = 0; // it is referred to the Red component
											// of the color 1;
	private static final int M_GREEN1 = 1; // it is referred to the Green
											// component of the color 1;
	private static final int M_BLUE1 = 2; // it is referred to the Blue
											// component of the color 1;
	private static final int M_RED2 = 3; // it is referred to the Red component
											// of the color 2;
	private static final int M_GREEN2 = 4; // it is referred to the Green
											// component of the color 2;
	private static final int M_BLUE2 = 5; // it is referred to the Blue
											// component of the color 2;

	private static final int M_COLOR1 = 0; // it is referred to the Color 1;
	private static final int M_COLOR2 = 1; // it is referred to the Color 2;


	private static int italianBKColor[][] = new int[5][6];


	/**
	 * This method is used to load the Electronic Journal Backgorund color (IBKC - P_REGPAR.dat)
	 *
	 * @param lineIBKC
	 *            string of params read
	 * @since JDK 1.0.3
	 */
	public void italianBackGroundColorLoadParameter(String lineIBKC, int numberIBKC) {

		// Load parameter on ItalianBKColor
		italianBKColor[numberIBKC][M_RED1] = Integer.parseInt(lineIBKC.substring(0, 3));
		italianBKColor[numberIBKC][M_GREEN1] = Integer.parseInt(lineIBKC.substring(3, 6));
		italianBKColor[numberIBKC][M_BLUE1] = Integer.parseInt(lineIBKC.substring(6, 9));
		italianBKColor[numberIBKC][M_RED2] = Integer.parseInt(lineIBKC.substring(9, 12));
		italianBKColor[numberIBKC][M_GREEN2] = Integer.parseInt(lineIBKC.substring(12, 15));
		italianBKColor[numberIBKC][M_BLUE2] = Integer.parseInt(lineIBKC.substring(15, 18));

	}

	public Color[] getBackGroundColor(int status) {
		if ((status != M_OFFLINE) && (status != M_OFFLINEREVERSE)) {
			status = M_ONLINE;
		}
		Color iBKColor[] = new Color[2];

		iBKColor[M_COLOR1] = new Color(italianBKColor[status][M_RED1], italianBKColor[status][M_GREEN1], italianBKColor[status][M_BLUE1]);
		iBKColor[M_COLOR2] = new Color(italianBKColor[status][M_RED2], italianBKColor[status][M_GREEN2], italianBKColor[status][M_BLUE2]);


		return iBKColor;
	}
}
