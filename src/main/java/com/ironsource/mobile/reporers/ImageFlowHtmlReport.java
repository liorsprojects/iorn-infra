package com.ironsource.mobile.reporers;

import java.io.File;

public class ImageFlowHtmlReport {
	
	private static final String SIMPLE_TITLE_FORMAT = "<p>%s</p>";
	private static final String SIMPLE_IMG_FORMAT = "<img src=\"%s\"/>";
	private static final String BLANK_ROW = "<br/>";
	private static final String STYLE = "<style>a,p,h1,h2,h3,div,img " +
											"{" +
												"display: block" +
											"}"	+
												"p{color:red}" +
											"img{width:30%}" +
											"#scaleWidget {position:absolute;right:10px;top:10px;}" + 
											"#scaleWidget button{float:left;}" +
										"</style>";
	
	StringBuilder htmlBody;
	
	public ImageFlowHtmlReport() {
		htmlBody = new StringBuilder("<h3>Test Screenshot flow</h3>");
	}
	
	public void addTitledImage(String title, File imagefile) {
		htmlBody.append(BLANK_ROW);
		htmlBody.append(String.format(SIMPLE_TITLE_FORMAT, title));
		htmlBody.append(String.format(SIMPLE_IMG_FORMAT, imagefile.getAbsoluteFile()));
		htmlBody.append(BLANK_ROW);
	}
	
	public String getHtmlReport() {
		return "<html><head>" + STYLE + "</head><body>" + htmlBody.toString() +"</body></html>";
	}
	
	public void addScaleButtonWidget() {
		String scaleButtonWidget = "<div id=\"scaleWidget\"><button type=\"button\">+</button><button type=\"button\">-</button></div>";
		htmlBody.append(scaleButtonWidget);
	}
}
