package com.ironsource.mobile;

import com.android.ddmlib.MultiLineReceiver;

public class CollectingOutputReceiver extends MultiLineReceiver {

	private StringBuffer mOutputBuffer = new StringBuffer();

	public String getOutput() {
		return mOutputBuffer.toString();
	}

	@Override
	public void processNewLines(String[] lines) {
		for (String line : lines) {
			mOutputBuffer.append(line);
			mOutputBuffer.append("\n");
		}
	}

	public boolean isCancelled() {
		return false;
	}
}