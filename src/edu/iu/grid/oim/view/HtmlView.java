package edu.iu.grid.oim.view;

import java.io.PrintWriter;

//beware of XSS risk! don't use this to display non-html content
//(if you do, make sure it's HTML escaped)
public class HtmlView implements IView 
{
	private String html;
	public HtmlView(String _html) {
		html = _html;
	}
	public void render(PrintWriter out) {
		out.write(html);
	}

}
