package edu.iu.grid.oim.view;

public class Page extends View {
	private View header;
	private View menu;
	private View content;
	private View footer;
	private View side;
	
	public Page(View _menu, View _content, View _side)
	{
		header = new HtmlFileView("header.html");
		footer = new HtmlFileView("footer.html");
		menu = _menu;
		content = _content;
		side = _side;
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
