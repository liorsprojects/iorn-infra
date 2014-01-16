package com.ironsource.mobile;

import java.util.List;

import jsystem.framework.system.SystemObjectImpl;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

public class Logcat extends SystemObjectImpl {

	private IDevice device;

	public void initialize() throws Exception {
		AndroidDebugBridge.initIfNeeded(false);
		device = getDevice();
	}

	public void clearLogcat() throws Exception {
		report.report("send clear log command via adb");
		device.executeShellCommand("logcat -c", new MultiLineReceiver() {

			@Override
			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void processNewLines(String[] lines) {
				System.out.println("\n##### ADB #####");
				for (String line : lines) {
					report.report(line);
				}
			}
		});
	}

	public List<LogCatMessage> getLogcatMessages(FilteredLogcatListener filteredLogcatListener) throws Exception {

		LogCatReceiverTask logCatReceiverTask = new LogCatReceiverTask(device);

		logCatReceiverTask.addLogCatListener(filteredLogcatListener);

		new Thread(logCatReceiverTask).start();

		Thread.sleep(3000);

		logCatReceiverTask.stop();

		return filteredLogcatListener.getReturnedMessages();

	}

	private IDevice getDevice() throws Exception {
		AndroidDebugBridge adb = AndroidDebugBridge.createBridge();

		int trials = 10;
		while (trials > 0) {
			Thread.sleep(50);
			if (adb.isConnected()) {
				break;
			}
			trials--;
		}

		if (!adb.isConnected()) {
			System.out.println("Couldn't connect to ADB server");
			throw new Exception();
		}

		trials = 10;
		while (trials > 0) {
			Thread.sleep(50);
			if (adb.hasInitialDeviceList()) {
				break;
			}
			trials--;
		}

		if (!adb.hasInitialDeviceList()) {
			System.out.println("Couldn't list connected devices");
			throw new Exception();
		}

		return adb.getDevices()[0];
	}

	/**
	 * The close method is called in the end of the while execution.<br>
	 * This can be a good place to free resources.<br>
	 */
	public void close() {
		AndroidDebugBridge.disconnectBridge();
		super.close();
	}
}
