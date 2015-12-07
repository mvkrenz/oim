package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

public class LinkView implements IView {
	
	private String url;
	private String title;
	private Boolean external;
	public LinkView(String _url, String _title, Boolean _external) {
		url = _url;
		title = _title;
		external = _external;
	}
	public void render(PrintWriter out) {
		out.write("<p>");
		if(external) {
			out.write("<a target=\"_blank\" href=\""+url+"\">"+StringEscapeUtils.escapeHtml(title)+"</a>");	
		} else {
			out.write("<a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(title)+"</a>");
		}
		out.write("</p>");
	}

}
