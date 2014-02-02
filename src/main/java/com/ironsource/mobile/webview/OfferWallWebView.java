package com.ironsource.mobile.webview;

import il.co.topq.mobile.client.impl.WebElement;

import java.util.ArrayList;
import java.util.List;

public class OfferWallWebView {

	private WebElement closeButton;
	private List<WebElement> ownElements;
	private List<AdItemWebElement> adElementList;
	
	public OfferWallWebView() {
		ownElements = new ArrayList<WebElement>();
		adElementList = new ArrayList<AdItemWebElement>();
	}

	public WebElement getCloseButton() {
		return closeButton;
	}

	public void setCloseButton(WebElement closeButton) {
		this.closeButton = closeButton;
	}

	public List<WebElement> getOwnElements() {
		return ownElements;
	}

	public void setOwnElements(List<WebElement> ownElements) {
		this.ownElements = ownElements;
	}

	public List<AdItemWebElement> getAdElementList() {
		return adElementList;
	}

	public void setAdElementList(List<AdItemWebElement> adElementList) {
		this.adElementList = adElementList;
	}
	
	public void addOwnItem(WebElement element) {
		ownElements.add(element);
	}
	
	public void addAdElement(AdItemWebElement element) {
		adElementList.add(element);
	}

}
