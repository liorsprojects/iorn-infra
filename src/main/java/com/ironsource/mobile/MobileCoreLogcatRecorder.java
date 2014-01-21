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

	private Vector<LogCatMessage> recordedMessages;
	IDevice device;

	public MobileCoreLogcatRecorder(IDevice device) {
		this.device = device;
		recordedMessages = new Vector<LogCatMessage>();
	}

	@Override
	public void log(List<LogCatMessage> msgList) {
		for (LogCatMessage msg : msgList) {
			if (msg.getMessage().contains("\"RS\"") ) {
				recordedMessages.add(msg);
			}
		}
	}
	
	public List<LogCatMessage> getRecordedMessages() {
		return new ArrayList<LogCatMessage>(recordedMessages);
	}
	
	public void recordMobileCoreLogcatMessages() throws Exception {
		LogCatReceiverTask logCatReceiverTask = new LogCatReceiverTask(device);
		logCatReceiverTask.addLogCatListener(this);
		new Thread(logCatReceiverTask).start();
		Thread.sleep(2000);
		logCatReceiverTask.removeLogCatListener(this);
		logCatReceiverTask.stop();
	}
}
