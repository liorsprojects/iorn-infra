package com.ironsource.mobile;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.client.interfaces.MobileClientInterface;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import org.topq.uiautomator.AutomatorService;
import org.topq.uiautomator.Selector;
import org.topq.uiautomator.client.DeviceClient;

import jsystem.framework.system.SystemObjectImpl;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.logcat.LogCatFilter;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.uiautomator.core.UiObjectNotFoundException;

public class MobileSO extends SystemObjectImpl {

	public MobileClientInterface robotiumClient;

	private String serverHost;
	private int serverPort;
	
	private ADBConnection adbConnection;
	AutomatorService uiAutomatorClient; 
	


	/**
	 * The init() method will be called by JSystem after the instantiation of
	 * the system object. <br>
	 * This can be a good place to assert that all the members that we need were
	 * defined in the SUT file.
	 */
	public void init() throws Exception {
		super.init();
		
		
		adbConnection = new ADBConnection();
		adbConnection.initialize();
		adbConnection.startUiAutomatorServer();
		adbConnection.startRobotiumServer();
		
		uiAutomatorClient = DeviceClient.getUiAutomatorClient("192.168.56.101:9008");
		
		report.report("Initiate moblie client");
		robotiumClient = new MobileClient(serverHost, serverPort);
		
		report.report("Launch MCTester app main activity");
		robotiumClient.launch("com.mobilecore.mctester.MainActivity");

		
	}
	

	public File capturescreen() throws Exception {
		File f = robotiumClient.takeScreenshot();
		return f;
	}
		
	public void clearLogcat() throws Exception {
		adbConnection.clearLogcat();
	}
	
	public void clickOnStickee() throws Exception {
		Selector selector = new Selector();
		selector.setDescription("Show stickee");
		selector.setClassName("android.widget.Button");
		uiAutomatorClient.click(selector);
	}
	
	public List<LogCatMessage> getFilterdMessages() throws Exception {
		List<LogCatFilter> filters = LogCatFilter.fromString("\"RS\"", LogLevel.DEBUG);
		//TODO - remove this
		//filters.add(new LogCatFilter("", "MobileCore" , "", "", "com.mobliecore.mctesterqa:mcServiceProcess", LogLevel.DEBUG));
		List<LogCatMessage> messages = null;
		messages = adbConnection.getLogcatMessages(new FilteredLogcatListener(filters, false));
		
		for (int i = 0; i < messages.size(); i++) {
			String tag  = messages.get(i).getTag();
			if(tag.contains("dalvikvm") || tag.equals("TilesManager")) {
				messages.remove(i);
			}
		}
		return messages; 
	}
	
	
	
	/**
	 * The close method is called in the end of the while execution.<br>
	 * This can be a good place to free resources.<br>
	 */
	public void close() {
		super.close();
	}

	public MobileClientInterface getRobotiumClien() {
		return robotiumClient;
	}

	public void setMobileClient(MobileClientInterface robotiumClient) {
		this.robotiumClient = robotiumClient;
	}

	public String getServerHost() {
		return serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}
	
	public AutomatorService getUiAutomatorClient() {
		return uiAutomatorClient;
	}

}
