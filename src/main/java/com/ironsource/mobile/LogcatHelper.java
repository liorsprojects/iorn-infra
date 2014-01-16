package com.ironsource.mobile;

import java.io.IOException;
import java.util.List;

import jsystem.framework.report.Reporter;
import jsystem.framework.report.ReporterHelper;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log.LogLevel;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;
import com.android.ddmlib.logcat.LogCatFilter;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;
import com.android.ddmlib.logcat.LogCatReceiverTask;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LogcatHelper {
	
	/**
	 * A connection to the host-side android debug bridge (adb) 
	 * 
	 * This is the central point to communicate with any devices, emulators, or the applications running on them. 
	 *
	 */
	public void init() {
		AndroidDebugBridge.init(false);
	}

	/**
	 * Terminates the ddm library. This must be called upon application termination
	 */
	public void finish() {
		AndroidDebugBridge.terminate();
	}

	public IDevice[] getDevices() throws Exception {

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

		return adb.getDevices();
	}
	
	/**
     * Helper method: search if the query string matches the message.
     * @param query words to search for
     * @param message text to search in
     * @return true if the encoded query is present in message
     */
    private boolean search(String query, LogCatMessage message) {
        List<LogCatFilter> filters = LogCatFilter.fromString(query,
                LogLevel.VERBOSE);

        /* all filters have to match for the query to match */
        for (LogCatFilter f : filters) {
            if (!f.matches(message)) {
                return false;
            }
        }
        return true;
    }

	public void printLog(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
		
		
		
		LogCatReceiverTask lcrt;
		LogCatListener lcl;

		lcrt = new LogCatReceiverTask(device);
		
		
		lcl = new LogCatListener() {
			@Override
			public void log(List<LogCatMessage> msgList) {
				
				
				
				
				/**
				 * LogCatFilter(String name, String tag, String text,
				 *	@NonNull String pid, String appName, LogLevel logLevel)
				 */
				
				//LogCatFilter filterTag = new LogCatFilter("", "MobileCore", "", "", "", LogLevel.VERBOSE);
				
				System.out.println("Print msg count of: " + msgList.size());
				
				for (LogCatMessage msg : msgList) {
					
					
					
					//if(search("\"rs\"", msg)) {
					
						System.out.println(msg.getTime());
						System.out.println(msg.getPid());
						System.out.println(msg.getLogLevel());
						System.out.println(msg.getAppName());
						System.out.println(msg.getTag());
						System.out.println(msg.getTid());
						System.out.println(msg.getMessage());
				//	}

					// logcat = logcat + msg.toString() + "\n";
				}
			}
		};
		lcrt.addLogCatListener(lcl);
		
		
		lcrt.run();

	
		
	}
	
	

	public static void main(String[] args) {
		LogcatHelper logcatHelper = new LogcatHelper();

		logcatHelper.init();

		IDevice[] devices = null;

		try {
			devices = logcatHelper.getDevices();
		} catch (Exception e) {

		}

		if (devices == null) {
			return;
		}
		
		//logcatHelper.printLog(devices[0]);
	}

	public static void extractMsgAsJson(List<LogCatMessage> messages) throws ParseException {
		for (LogCatMessage logCatMessage : messages) {
			String msg = logCatMessage.getMessage();
			String[] split = msg.split("\\{", 2);
			String jsonString  = "{" + split[1];
			
			JSONParser parser = new JSONParser();
			Object obj = parser.parse(jsonString);
			JSONObject jsonObject = (JSONObject)obj;
			String rs = (String) jsonObject.get("RS");
			//TODO  - remove after use...
//			JSONArray offersArray = (JSONArray)jsonObject.get("Offers");
//			for (int i = 0; i < offersArray.size(); i++) {
//				JSONObject objApp = (JSONObject)offersArray.get(i);
//				String name = (String)objApp.get("Name");
//				System.out.println("\nApp " + i + " Name is: " + name);
//			}
			
		}
		
		
	 

		
	}
}
