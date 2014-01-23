package com.ironsource.mobile;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.client.interfaces.MobileClientInterface;

import java.io.File;
import java.util.List;

import jsystem.framework.system.SystemObjectImpl;

import org.topq.uiautomator.AutomatorService;
import org.topq.uiautomator.client.DeviceClient;

import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.logcat.LogCatFilter;
import com.android.ddmlib.logcat.LogCatMessage;

//TODO - use CommandResponse for robotium commands verifications
public class MobileSO extends SystemObjectImpl {


	private String serverHost;
	private int serverPort;	
	private ADBConnection adbConnection;
	private AutomatorService uiAutomatorClient; 
	private MobileClientInterface robotiumClient;
	


	/**
	 * The init() method will be called by JSystem after the instantiation of
	 * the system object. <br>
	 * This can be a good place to assert that all the members that we need were
	 * defined in the SUT file.
	 */
	public void init() throws Exception {
		super.init();
		
		adbConnection = new ADBConnection();
		adbConnection.init();
	
		adbConnection.startUiAutomatorServer();
		
		adbConnection.startRobotiumServer();
		
		report.report("Initiate ui-automator client");
		uiAutomatorClient = DeviceClient.getUiAutomatorClient("http://192.168.56.101:9008");
		
		report.report("Initiate robotium client");
		robotiumClient = new MobileClient(serverHost, serverPort);
		
	}
	
	
	public File capturescreenWithRobotium() throws Exception {
		report.report("capture screen");
		File f = robotiumClient.takeScreenshot();
		return f;
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
		report.report("closing MobileSO");
		super.close();
	}

	public MobileClientInterface getRobotiumClient() {
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
	public ADBConnection getAdbConnection() {
		return adbConnection;
	}

}
