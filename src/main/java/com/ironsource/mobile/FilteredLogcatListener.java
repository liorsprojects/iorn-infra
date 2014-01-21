package com.ironsource.mobile;

import java.util.List;
import java.util.Vector;

import com.android.ddmlib.logcat.LogCatFilter;
import com.android.ddmlib.logcat.LogCatListener;
import com.android.ddmlib.logcat.LogCatMessage;

public class FilteredLogcatListener implements LogCatListener {

	private List<LogCatFilter> filters;
	private Vector<LogCatMessage> returnedMessages;
	private boolean partial;

	public FilteredLogcatListener(List<LogCatFilter> filters, boolean partial) {
		this.partial = partial;
		this.filters = filters;
		this.returnedMessages = new Vector<LogCatMessage>();
	}

	@Override
	public void log(List<LogCatMessage> msgList) {
		boolean addMessage = false;
		for (LogCatMessage message : msgList) {
			int filterMatchCount = 0;
			for (LogCatFilter filter : filters) {
				if(filter.matches(message)) {
					filterMatchCount++;
				}
			}
			
			if(partial && filterMatchCount > 0) {
				addMessage = true;
			} else if(!partial && filterMatchCount == filters.size()) {
				addMessage = true;
			}
			
			if(addMessage) {
				returnedMessages.add(message);
			}
		}	
	}
	
	public List<LogCatMessage> getReturnedMessages() {
		return returnedMessages;
	}
}
