package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

//beware of XSS risk! don't use this to display non-html content
//(if you do, make sure it's HTML escaped)
public class CDataView implements IView 
{
	private String html;
	public CDataView(String _html) {
		html = _html;
	}
	public void render(PrintWriter out) {
		out.write("<pre>");
		out.write(StringEscapeUtils.escapeHtml(html));
		out.write("</pre>");
	}

}
