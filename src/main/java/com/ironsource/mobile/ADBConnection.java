package com.ironsource.mobile;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
import com.android.ddmlib.IShellOutputReceiver;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;

//TODO - forward also automatically
public class ADBConnection extends SystemObjectImpl implements IDeviceChangeListener, IShellOutputReceiver {

	private final String ROBOTIUM_SERVER_PKG = "il.co.topq.mobile.server.application";
	private final String ROBOTIUM_SERVER_ACTIVITY = "RobotiumServerActivity";

	private IDevice device;
	private AndroidDebugBridge adb;
	private File adbLocation;
	private String shellOuput;
	private MobileCoreLogcatRecorder mobileCoreLogcatRecorder;
	
	

	@SuppressWarnings("unused")
	private boolean cancelShellCommand = false;

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
	
	//return all logcat messages filtered by messages that contain "RS" String
	public List<LogCatMessage> getMobileCoreLogcatMessages() throws Exception {
		mobileCoreLogcatRecorder.recordMobileCoreLogcatMessages();
		return mobileCoreLogcatRecorder.getRecordedMessages();
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
		//TODO - look for case of fail
		if(response != null) {
			report.report("response " + response);
			success = true;
		}
		if (!success)
			throw new Exception("could not clear logcat");
	}

	//execute adb shell command with default timeout of 10 seconds and return the response form the shell 
	private String executeShellCommand(String cmd) throws Exception {
		long maxTimeToWait = 10000L;
		shellOuput = "";
		try {
			device.executeShellCommand(cmd, this, maxTimeToWait, TimeUnit.MILLISECONDS);
		}catch (Exception e) {
			throw e;
		}
		return shellOuput;
	}
	
	//start activity with an adb command
	public boolean startActivity(String packageName, String activityName) throws Exception {
		String activity = String.format("%s/.%s", packageName, activityName);
		report.report("starting activity: " + activity);
		executeShellCommand("am start -n " + activity);
		if (shellOuput.contains("Error type 3")) {
			report.report("activity not found");
			return false;
		}
		return true;

	}

	//call start activity to start the robotuim server application
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

	
	//TODO - need major fix, not working good at the moment 
	public void startUiAutomatorServer() throws Exception {
		report.report("startig uiautomator server");
		if (!isUiAutomatorServerAlive()) {
			String response = executeShellCommand("uiautomator runtest uiautomator-stub.jar bundle.jar -c com.github.uiautomatorstub.Stub &");
			if(response.contains("Error")) {
				// TODO - automate the installation process: ant build ->
				// ant install -> forward ports
				throw new Exception("uiautomator server is not on the device");
			}
			report.report("uiautomator server started");
			Thread.sleep(5000);
			device.createForward(9008, 9008);
			Thread.sleep(1000);
			return;
		}
		report.report("uiautomator server was already running");
	}

	
	public boolean isUiAutomatorServerAlive() throws Exception {
		String response = executeShellCommand("ps | grep uiautomator");
		if(response.contains("uiautomator")) {
			return true;
		}
		return false;
	}

	public void installPackage(String apkLocation, boolean reinstall) throws InstallException {
		final String result = device.installPackage(apkLocation, reinstall);
		if (result != null) {
			throw new InstallException("Failed to install: " + result, null);
		}
	}
	
	public void terminateUiAutomatorServer() throws Exception {
		report.report("about to terminate uiautomator server...");
		boolean terminated = false;
		if (isUiAutomatorServerAlive()) {
			String response = executeShellCommand("killall uiautomator");
			if(response.contains("Terminated")) {
				terminated = true;
			}
		} else {
			report.report("uiautomator server is already terminated, skipping action");
			terminated = true;
		}
		if (!terminated) {
			throw new Exception("uiautomator server could not be stopped");
		}

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

	@Override
	public void addOutput(byte[] data, int offset, int length) {
		report.report("called");
		shellOuput = new String(data);
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
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
