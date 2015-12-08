package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

//beware of XSS risk! don't use this to display non-html content
//(if you do, make sure it's HTML escaped)
public class URLView implements IView 
{
	private String url;
	public URLView(String url) {
		this.url = url;
	}
	public void render(PrintWriter out) {
		if(url == null || url.length() == 0) {
			out.write("<img src=\"images/null.png\"/>");
		} else {
			out.write("<a target=\"_blank\" href=\""+url+"\">"+StringEscapeUtils.escapeHtml(url)+"</a>");
		}
	}

}
