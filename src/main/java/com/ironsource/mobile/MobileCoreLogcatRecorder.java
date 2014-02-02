package com.ironsource.mobile;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

/**
 * 
 * 
 * @author lior_g
 * 
 */
public class MobileCoreLogcatRecorder implements LogCatListener {

	private Vector<LogCatMessage> rsMessages;
	private Vector<LogCatMessage> offerWallManagerMessages;
	private Vector<LogCatMessage> stickeezManagerMessages;
	private Vector<LogCatMessage> sliderManagerMessages;
	private Vector<LogCatMessage> mobileCoreMessages;
	IDevice device;

	public MobileCoreLogcatRecorder(IDevice device) {
		this.device = device;
		rsMessages = new Vector<LogCatMessage>();
		offerWallManagerMessages = new Vector<LogCatMessage>();
		stickeezManagerMessages = new Vector<LogCatMessage>();
		sliderManagerMessages = new Vector<LogCatMessage>();
		mobileCoreMessages = new Vector<LogCatMessage>();
		
	}

	@Override
	public void log(List<LogCatMessage> msgList) {
		for (LogCatMessage msg : msgList) {
			if (msg.getMessage().contains("\"RS\"")) {
				rsMessages.add(msg);
			}
			if(msg.getMessage().contains("OfferwallManager")) {
				offerWallManagerMessages.add(msg);
			}
			if(msg.getMessage().contains("StickeezManager")) {
				stickeezManagerMessages.add(msg);
			}
			if(msg.getMessage().contains("SliderMenuManager")) {
				sliderManagerMessages.add(msg);
			}
			if(msg.getMessage().contains("MobileCoreReport")) {
				sliderManagerMessages.add(msg);
			}
		}
	}
	
	public List<LogCatMessage> getRsMessages() {
		return new ArrayList<LogCatMessage>(rsMessages);
	}
	public List<LogCatMessage> getOfferwallManagerMessages() {
		return new ArrayList<LogCatMessage>(offerWallManagerMessages);
	}
	public List<LogCatMessage> getStickeezManagerMessages() {
		return new ArrayList<LogCatMessage>(stickeezManagerMessages);
	}
	public List<LogCatMessage> getSliderManagerMessages() {
		return new ArrayList<LogCatMessage>(sliderManagerMessages);
	}
	public List<LogCatMessage> getMobileCoreMessages() {
		return new ArrayList<LogCatMessage>(mobileCoreMessages);
	}
	
	public void recordMobileCoreLogcatMessages() throws Exception {
		LogCatReceiverTask logCatReceiverTask = new LogCatReceiverTask(device);
		logCatReceiverTask.addLogCatListener(this);
		new Thread(logCatReceiverTask).start();
		Thread.sleep(3000);
		logCatReceiverTask.removeLogCatListener(this);
		logCatReceiverTask.stop();
	}
}
