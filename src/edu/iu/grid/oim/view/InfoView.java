package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

//beware of XSS risk! don't use this to display non-html content
//(if you do, make sure it's HTML escaped)
public class InfoView implements IView 
{
	private String html;
	public InfoView(String _html) {
		html = _html;
	}
	public void render(PrintWriter out) {
		out.write("<p>");
		out.write(html);
		out.write("</p>");
	}

}
