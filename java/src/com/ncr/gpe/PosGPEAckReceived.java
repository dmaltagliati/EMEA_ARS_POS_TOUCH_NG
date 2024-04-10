package com.ncr.gpe;

import java.util.Map;

public class PosGPEAckReceived implements GpeResultProcessorInterface {

	public void processResult(Map messageMap) {

		PosGPE.sts = 5;
	}
}
