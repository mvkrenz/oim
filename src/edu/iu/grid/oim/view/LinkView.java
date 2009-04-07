package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

public class LinkView implements IView {
	
	private String url;
	private String title;
	public LinkView(String _url, String _title) {
		url = _url;
		title = _title;
	}
	public void render(PrintWriter out) {
		out.write("<a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(title)+"</a>");
	}

}
