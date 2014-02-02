package com.ironsource.mobile.webview;

import il.co.topq.mobile.client.impl.WebElement;

import java.util.ArrayList;
import java.util.List;

public class AdItemWebElement {

	private String appName;
	private List<WebElement> innerElements;

	public AdItemWebElement() {
		innerElements = new ArrayList<WebElement>();
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public List<WebElement> getInnerElements() {
		return innerElements;
	}

	public void setInnerElements(List<WebElement> innerElements) {
		this.innerElements = innerElements;
	}
	
	public void addElement(WebElement element) {
		innerElements.add(element);
	}
	
	
	
	
}
