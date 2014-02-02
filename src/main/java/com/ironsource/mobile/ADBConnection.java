package com.ironsource.mobile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

import org.apache.commons.io.FileUtils;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

//TODO - forward also automatically
public class ADBConnection extends SystemObjectImpl implements IDeviceChangeListener {

	public final String ROBOTIUM_SERVER_PKG = "il.co.topq.mobile.server.application";
	public final String ROBOTIUM_SERVER_ACTIVITY = "RobotiumServerActivity";
	public final String MCTESTER_PKG = "com.mobilecore.mctester";
	public final String MCTESTER_ACTIVITY = "MainActivity";

	private IDevice device;
	private AndroidDebugBridge adb;
	private File adbLocation;
	private MobileCoreLogcatRecorder mobileCoreLogcatRecorder;

	@Override
	public void init() throws Exception {
		super.init();
		AndroidDebugBridge.initIfNeeded(false);
		adbLocation = findAdbFile();
		adb = AndroidDebugBridge.createBridge(adbLocation.getAbsolutePath() + File.separator + "adb", true);
		if (adb == null) {
			throw new IllegalStateException("Failed to create ADB bridge");
		}
		AndroidDebugBridge.addDeviceChangeListener(this);
		if (adb.hasInitialDeviceList()) {
			device = adb.getDevices()[0];
		} else {
			waitForDeviceToConnect(5000);
		}
		mobileCoreLogcatRecorder = new MobileCoreLogcatRecorder(device);
	}

	// return all logcat messages filtered by messages that contain "RS" String
	public List<LogCatMessage> getMobileCoreLogcatMessages(MobileCoreMsgCode msgCode) throws Exception {
		mobileCoreLogcatRecorder.recordMobileCoreLogcatMessages();
		List<LogCatMessage> returnedMessages = null;
		switch (msgCode) {
		case RS:
			returnedMessages = mobileCoreLogcatRecorder.getRsMessages();
			break;
		case OFFERWALL_MANAGER:
			returnedMessages = mobileCoreLogcatRecorder.getOfferwallManagerMessages();
			break;
		case STICKEEZ_MANAGER:
			returnedMessages = mobileCoreLogcatRecorder.getStickeezManagerMessages();
			break;
		case SLIDER_MANAGER:
			returnedMessages = mobileCoreLogcatRecorder.getSliderManagerMessages();
			break;
		default:
			returnedMessages = new ArrayList<LogCatMessage>();
			break;
		}
		return returnedMessages;
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

	public File getScreenshotWithAdb(File screenshotFile) throws Exception {
		RawImage ri = device.getScreenshot();
		return display(device.getSerialNumber(), ri, screenshotFile);
	}

	public void clearLogcat() throws Exception {
		report.report("send clear logcat command");
		String cmd = "logcat -c";
		String response = executeShellCommand(cmd);
		boolean success = false;
		// TODO - look for case of fail
		if (response != null) {
			report.report("response " + response);
			success = true;
		}
		if (!success)
			throw new Exception("could not clear logcat");
	}

	// the response form the shell
	private String executeShellCommand(String cmd) throws Exception {
		report.report("In execute shell command with command: " + cmd);
		CollectingOutputReceiver receiver = new CollectingOutputReceiver();
		try {
			device.executeShellCommand(cmd, receiver, 3000, TimeUnit.MILLISECONDS);
			report.report("Executed");
			Thread.sleep(3000);
		} catch (Exception e) {
			report.report(e.getMessage());
		}
		return receiver.getOutput();
	}

	// start activity with an adb command
	public boolean startActivity(String packageName, String activityName) throws Exception {
		String activity = String.format("%s/.%s", packageName, activityName);
		report.report("starting activity: " + activity);
		String startRes = executeShellCommand("am start -n " + activity);
		if (startRes.contains("Error type 3")) {
			report.report("activity not found");
			return false;
		}
		return true;
	}

	// stop activity with an adb command
	public void stopActivity(String packageName) throws Exception {
		report.report("force stop package: " + packageName);
		executeShellCommand("am force-stop " + packageName);
	}

	// call start activity to start the robotuim server application
	public void startRobotiumServer() throws Exception {
		report.report("starting robotium server...");
		boolean started = startActivity(ROBOTIUM_SERVER_PKG, ROBOTIUM_SERVER_ACTIVITY);
		device.createForward(4321, 4321);
		if (!started) {
			report.report("robotium server application was not found");
			// TODO - automate the installation process: sign apk -> install apk
			// -> forward ports
			throw new Exception("server is not installed on the device");
		}
		Thread.sleep(3000);
	}

	// TODO - need major fix, not working good at the moment
	public void startUiAutomatorServer() throws Exception {
		report.report("startig uiautomator server");
		String psRes = executeShellCommand("ps | grep uiautomator");
		if (psRes.contains("uiautomator")) {
			String[] ps = psRes.split("\\s+");
			report.report("kill app with name " + ps[1]);
			executeShellCommand("kill " + ps[1]);
			Thread.sleep(2000);
		}
		String startAutomatorServiceRes = executeShellCommand("uiautomator runtest uiautomator-stub.jar bundle.jar -c com.github.uiautomatorstub.Stub &");
		Thread.sleep(5000);
		if (startAutomatorServiceRes.contains("INSTRUMENTATION_STATUS: current=1")) {
			Thread.sleep(2000);
			String verifyPsRes = executeShellCommand("ps | grep uiautomator");
			if (verifyPsRes.contains("uiautomator")) {
				report.report("uiautomator server started");
				Thread.sleep(2000);
				device.createForward(9008, 9008);
				Thread.sleep(1000);
			}
		} else {
			throw new Exception("uiautomator server is not on the device");
		}
	}

	public void installPackage(String apkLocation, boolean reinstall) throws InstallException {
		final String result = device.installPackage(apkLocation, reinstall);
		if (result != null) {
			throw new InstallException("Failed to install: " + result, null);
		}
	}

	public void terminateUiAutomatorServer() throws Exception {
		report.report("about to terminate uiautomator server...");
		// boolean terminated = false;
		// if (isUiAutomatorServerAlive()) {
		// String response = executeShellCommand("killall uiautomator");
		// if (response.contains("Terminated")) {
		// terminated = true;
		// }
		// } else {
		// report.report("uiautomator server is already terminated, skipping action");
		// terminated = true;
		// }
		// if (!terminated) {
		// throw new Exception("uiautomator server could not be stopped");
		// }

	}

	@Deprecated
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
	public void close() {
		report.report("closing ADBConnection");
		try {
			terminateUiAutomatorServer();
		} catch (Exception e) {
			report.report(e.getMessage(), Reporter.WARNING);
		}
		super.close();
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

	private File findAdbFile() throws IOException {
		// Check if the adb file is in the current folder
		File[] adbFile = new File(".").listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().equals("adb") || pathname.getName().equals("adb.exe");
			}
		});
		if (adbFile != null && adbFile.length > 0) {
			return adbFile[0].getParentFile();
		}

		final String androidHome = System.getenv("ANDROID_HOME");
		if (androidHome == null || androidHome.isEmpty()) {
			throw new IOException("ANDROID_HOME environment variable is not set");
		}

		final File root = new File(androidHome);
		if (!root.exists()) {
			throw new IOException("Android home: " + root.getAbsolutePath() + " does not exist");
		}

		try {
			// String[] extensions = { "exe" };
			Collection<File> files = FileUtils.listFiles(root, null, true);
			for (Iterator<File> iterator = files.iterator(); iterator.hasNext();) {
				File file = (File) iterator.next();
				// TODO: Eran - I think should be using equals as compareTo is
				// more sortedDataStructure oriented.
				if (file.getName().equals("adb.exe") || file.getName().equals("adb")) {
					return file.getParentFile();
				}
			}
		} catch (Exception e) {
			throw new IOException("Failed to find adb in " + root.getAbsolutePath());
		}
		throw new IOException("Failed to find adb in " + root.getAbsolutePath());
	}

	private static File display(String device, RawImage rawImage, File screenshotFile) throws Exception {
		BufferedImage image = new BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_RGB);
		// Dimension size = new Dimension(image.getWidth(), image.getHeight());

		int index = 0;
		int indexInc = rawImage.bpp >> 3;
		for (int y = 0; y < rawImage.height; y++) {
			for (int x = 0; x < rawImage.width; x++, index += indexInc) {
				int value = rawImage.getARGB(index);
				image.setRGB(x, y, value);
			}
		}
		if (screenshotFile == null) {
			screenshotFile = File.createTempFile("screenshot", ".png");

		}
		ImageIO.write(image, "png", screenshotFile);
		return screenshotFile;
	}

	public MobileCoreLogcatRecorder getMobileCoreLogcatRecorder() {
		return mobileCoreLogcatRecorder;
	}
}
