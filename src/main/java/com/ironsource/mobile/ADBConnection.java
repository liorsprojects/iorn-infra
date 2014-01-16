package com.ironsource.mobile;

import java.util.List;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.NullOutputReceiver;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

public class ADBConnection extends SystemObjectImpl implements IDeviceChangeListener{

	private final String ROBOTIUM_SERVER_PKG = "il.co.topq.mobile.server.application";
	private final String ROBOTIUM_SERVER_ACTIVITY = "RobotiumServerActivity";

	private IDevice device;
    private AndroidDebugBridge adb;
    
	public void initialize() throws Exception {
		AndroidDebugBridge.initIfNeeded(false);
		adb = AndroidDebugBridge.createBridge();
		Thread.sleep(2000);
		if(adb.hasInitialDeviceList()) {
			device = adb.getDevices()[0];
		} else { 
			waitForDeviceToConnect(5000);
		}
	}

	private void waitForDeviceToConnect(int timeoutForDeviceConnection) throws Exception {
		final long start = System.currentTimeMillis();
		while (device == null) {
			if (System.currentTimeMillis() - start > timeoutForDeviceConnection) {
				throw new Exception("Cound not find conneced device");
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Not important
			}
		}
		
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
				for (String line : lines) {
					report.report(line);
				}
			}
		});
	}

	public void startRobotiumServer() throws Exception {

		String cmd = "am start -n " + ROBOTIUM_SERVER_PKG + "/" + ROBOTIUM_SERVER_PKG + "." + ROBOTIUM_SERVER_ACTIVITY;
		
		device.executeShellCommand(cmd, new MultiLineReceiver() {

			@Override
			public boolean isCancelled() {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public void processNewLines(String[] lines) {
				boolean running = true;
				for (String line : lines) {
					if (line.contains("Error")) {
						running = false;
					} else {
						continue;
					}
				}
				if (!running) {
					report.report("Could not start robotuim server", Reporter.FAIL);
				}

			}
		});
		Thread.sleep(2000);
	}

	public void startUiAutomatorServer() throws Exception {

		device.executeShellCommand("uiautomator runtest uiautomator-stub.jar bundle.jar -c com.github.uiautomatorstub.Stub &", NullOutputReceiver.getReceiver());
		device.createForward(9008, 9008);
	}
	public void terminateUiAutomatorServer() throws Exception {
		
		device.executeShellCommand("killall uiautomator", NullOutputReceiver.getReceiver());
	}

	public List<LogCatMessage> getLogcatMessages(FilteredLogcatListener filteredLogcatListener) throws Exception {

		LogCatReceiverTask logCatReceiverTask = new LogCatReceiverTask(device);

		logCatReceiverTask.addLogCatListener(filteredLogcatListener);

		new Thread(logCatReceiverTask).start();

		Thread.sleep(3000);

		logCatReceiverTask.stop();

		return filteredLogcatListener.getReturnedMessages();

	}


	/**
	 * The close method is called in the end of the while execution.<br>
	 * This can be a good place to free resources.<br>
	 */
	public void close()  {
		super.close();
	}
	
	public static void main(String[] args) throws Exception {
		ADBConnection con = new ADBConnection();
		con.initialize();
		con.startUiAutomatorServer();
		Thread.sleep(10000);
		con.terminateUiAutomatorServer();
		
		
	}

	@Override
	public void deviceConnected(IDevice device) {
		this.device = device;
		
	}

	@Override
	public void deviceDisconnected(IDevice device) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deviceChanged(IDevice device, int changeMask) {
		// TODO Auto-generated method stub
		
	}
}
