package com.ironsource.mobile;

import il.co.topq.mobile.client.interfaces.MobileClientInterface;

import java.io.File;

public class TestHelper {
	
	public static void captureScreen(MobileClientInterface mobile, String path, String fileName) throws Exception {
		File f = mobile.takeScreenshot();
		if (f.renameTo(new File(path + File.separator + fileName + ".jpg"))) {
			System.out.println("screenshot saved successful!");
		} else {
			System.out.println("screenshot failed to save");
		}
	}
}
