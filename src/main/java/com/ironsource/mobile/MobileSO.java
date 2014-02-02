package com.ironsource.mobile;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.client.interfaces.MobileClientInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jsystem.framework.report.Reporter;
import jsystem.framework.system.SystemObjectImpl;

import org.json.simple.JSONObject;
import org.topq.uiautomator.AutomatorService;
import org.topq.uiautomator.Selector;
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
		uiAutomatorClient = DeviceClient.getUiAutomatorClient("http://172.17.25.133:9008");
		
		
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
	 * this method wait for specified manager ( OfferWallManger, StickeezManager...),
	 * to report message to logcat that contains the desired string passed to it.  
	 * 
	 * @param code - the manager
	 * @param msg - content to wait for
	 * @param timeout - timeout in milliseconds
	 * @throws Exception
	 */
	public void waitForManagerMessageToContain(MobileCoreMsgCode code, String msg, int timeout) throws Exception {
		long now = System.currentTimeMillis();
		boolean exist = false;
		report.report("waiting for " + code.toString() + " to preduce message : " + msg);
		List<LogCatMessage> messages;
		while (!exist) {
			if (System.currentTimeMillis() - now > timeout) {
				throw new Exception(code.toString() + " Did not preduce message that contains '" + msg + "' after: " + timeout + " millis");
			}

			messages = adbConnection.getMobileCoreLogcatMessages(code);
			for (LogCatMessage logCatMessage : messages) {
				String message = logCatMessage.getMessage();
				if (message.contains(msg)) {
					report.step(code.toString() + " preduced message contains '" + msg + "'");
					exist = true;
					break;
				}
			}
			Thread.sleep(1000);
		}
	}
	
	/**
	 * gets offerwall id as string
	 *
	 * @return
	 * @throws Exception 
	 */
	public String getOwId() throws Exception {
		List<LogCatMessage> messages = adbConnection.getMobileCoreLogcatMessages(MobileCoreMsgCode.MOBILECORE_REPORT);
		for(LogCatMessage message : messages) {
			if(message.getMessage().contains("\"ow_id\"")) {
				if(message.getMessage().contains("bn_ic_2")) {
					return "bn_ic_2";
				} else if(message.getMessage().contains("2_ic_big")) {
					return "2_ic_big";
				}
			}
		}
		return "";
	}
	
	
	
	
	/**
	 * this method waits for "RS" code to appear in logcat by certain flow name.
	 * 
	 * @param rsCode - RSCode enum type 
	 * @param flowCode - FlowCode enum type
	 * @param timeout - int timeout in milliseconds
	 * 
	 * @throws Exception in case of timeout or "RS" with "E" was reported.
	 */
	public void waitForRSCode(RSCode rsCode, FlowCode flowCode, int timeout) throws Exception {
		long now = System.currentTimeMillis();
		boolean exist = false;
		report.report("waiting for RS Code '"+ rsCode.getRsCode() +"' for Flow " + flowCode.getFlowCode());
		List<LogCatMessage> messages;
		String lookString = "\"Flow\":\"" + flowCode.getFlowCode() + "\"";
		report.report("looking for :  " + lookString);
		while (!exist) {
			if (System.currentTimeMillis() - now > timeout) {
				throw new Exception("Did not find expected RS code: " + rsCode.getRsCode() + " after: " + timeout + " millis");
			}
			messages = adbConnection.getMobileCoreLogcatMessages(MobileCoreMsgCode.RS);
			for (LogCatMessage logCatMessage : messages) {
				String msg = logCatMessage.getMessage();
				
				
				if (msg.contains("\"RS\":\"" + rsCode.getRsCode() + "\"") && msg.contains("\"Flow\":\"" + flowCode.getFlowCode() + "\"")) {
					report.step("Found RS Code: " + rsCode.getRsCode());
					exist = true;
				}
				if (logCatMessage.getMessage().contains("\"RS\":\"E\"")) {
					JSONObject jsonMsg = LogcatHelper.extractMsgAsJson(logCatMessage.getMessage());
					String errorMsg = (String) jsonMsg.get("Err");
					report.report("Error: Found RS Code: E while waiting for " + rsCode.getRsCode(), Reporter.FAIL);
					
					throw new Exception("Error message: " + errorMsg);
				}
			}
			Thread.sleep(1000);
		}
	}
	
	
	
	
	/**
	 * clear open activities
	 * 
	 * 1. press home button.
	 * 2. press recent activities button.
	 * 3. try to swipe out all open activities.
	 * 4. press home button.
	 * 
	 * @throws Exception
	 * 
	 */
	//TODO - each rom implemented the recent activity screen in a different way.
	public void clearRecentApps() throws Exception {
		uiAutomatorClient.pressKey("home");
		Thread.sleep(1000);
		uiAutomatorClient.pressKey("recent");
		Thread.sleep(1000);
		report.step("about to close all open apps");
		List<Selector> recentSelectors = new ArrayList<Selector>();
		recentSelectors.add(new Selector().setText("Robotium Server"));
		recentSelectors.add(new Selector().setText("MCTester"));
		recentSelectors.add(new Selector().setText("Google Play Store"));
		
		for (Selector selector : recentSelectors) {
			if(uiAutomatorClient.waitForExists(selector, 1000)) {
				uiAutomatorClient.swipe(selector, "r", 5);
				report.report("closed " + selector.getText() + "app");
			}
			Thread.sleep(1000);		
		}
		report.step("no more apps to close");
		
		uiAutomatorClient.pressKey("home");
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
