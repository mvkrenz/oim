package edu.iu.grid.oim.view;

public class Page implements View {
	private View header;
	private View menu;
	private View content;
	private View footer;
	private View side;
	
	public Page(View _menu, View _content)
	{
		header = new HtmlFileView("header.html");
		footer = new HtmlFileView("footer.html");
		menu = _menu;
		content = _content;
		side = new HtmlView("<div id=\"sideContent\"><h3>Side Content</h3></div>");
	}
	
	public String toHTML()
	{
		String out = "";
		out += header.toHTML();
		out += menu.toHTML();
		out += side.toHTML();
		out += content.toHTML();		
		out += footer.toHTML();
		return out;
	}
}
