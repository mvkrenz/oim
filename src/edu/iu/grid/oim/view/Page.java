package edu.iu.grid.oim.view;

public class Page implements IView {
	private IView header;
	private IView menu;
	private IView content;
	private IView footer;
	private IView side;
	
	public Page(IView _menu, IView _content, IView _side)
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
