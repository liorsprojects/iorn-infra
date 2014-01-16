package com.ironsource.mobile;

import java.io.IOException;
import java.util.List;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

public class ADBConnection extends SystemObjectImpl {

	private final String ROBOTIUM_SERVER_PKG = "il.co.topq.mobile.server.application";
	private final String ROBOTIUM_SERVER_ACTIVITY = "RobotiumServerActivity";

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
						continue;
					} else {
						running = false;
						break;
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

		Runnable uiAutomatorServer = new Runnable() {

			@Override
			public void run() {
				try {
					device.executeShellCommand("uiautomator runtest uiautomator-stub.jar bundle.jar -c com.github.uiautomatorstub.Stub",
							new MultiLineReceiver() {

								@Override
								public boolean isCancelled() {
									// TODO Auto-generated method stub
									return false;
								}

								@Override
								public void processNewLines(String[] lines) {
									for (String line : lines) {
										System.out.println(line);
									}
								}
							});
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AdbCommandRejectedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ShellCommandUnresponsiveException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};
		Thread uiServerThread = new Thread(uiAutomatorServer);
		uiServerThread.setDaemon(true); // important, otherwise JVM does not
										// exit at end execution
		uiServerThread.start();
		Thread.sleep(2000);
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
