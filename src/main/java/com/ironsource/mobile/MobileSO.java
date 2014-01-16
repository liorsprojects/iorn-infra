package com.ironsource.mobile;

import il.co.topq.mobile.client.impl.MobileClient;
import il.co.topq.mobile.client.interfaces.MobileClientInterface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import jsystem.framework.system.SystemObjectImpl;

import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.logcat.LogCatFilter;
import com.android.ddmlib.logcat.LogCatMessage;

public class MobileSO extends SystemObjectImpl {

	public MobileClientInterface mobileClient;

	private String serverHost;
	private int serverPort;
	
	private Logcat logcat;
	/**
	 * The init() method will be called by JSystem after the instantiation of
	 * the system object. <br>
	 * This can be a good place to assert that all the members that we need were
	 * defined in the SUT file.
	 */
	public void init() throws Exception {
		super.init();
		report.report("Initiate moblie client");
		mobileClient = new MobileClient(serverHost, serverPort);
		
		report.report("Launch MCTester app main activity");
		mobileClient.launch("com.mobilecore.mctester.MainActivity");
		
		logcat = new Logcat();
		logcat.initialize();
	}
	
	
	//TODO - fix@@@@@
	public String capturescreen(/*String name, String path*/) throws Exception {
		File f = mobileClient.takeScreenshot();
		return f.getAbsolutePath();
//		if (f.renameTo(new File(path + File.separator + name + ".jpg"))) {
//			report.report("screenshot saved successful!");
//		} else {
//			report.report("screenshot failed to save");
//		}
//		return path + File.separator + name + ".jpg";
	}
	
	
	public void clearLogcat() throws Exception {
		logcat.clearLogcat();
	}
	
	public List<LogCatMessage> getFilterdMessages() throws Exception {
		List<LogCatFilter> filters = LogCatFilter.fromString("\"RS\"", LogLevel.DEBUG);
		//TODO - remove this
		//filters.add(new LogCatFilter("", "MobileCore" , "", "", "com.mobliecore.mctesterqa:mcServiceProcess", LogLevel.DEBUG));
		List<LogCatMessage> messages = null;
		messages = logcat.getLogcatMessages(new FilteredLogcatListener(filters, false));
		
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

	public MobileClientInterface getMobileClient() {
		return mobileClient;
	}

	public void setMobileClient(MobileClientInterface mobileClient) {
		this.mobileClient = mobileClient;
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

}
