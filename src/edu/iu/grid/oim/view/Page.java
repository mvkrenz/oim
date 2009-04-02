package edu.iu.grid.oim.view;

import java.io.PrintWriter;

public class Page extends View {
	private View header;
	private String header_addon;
	private View menu;
	private View content;
	private View footer;
	private View side;
	
	public Page(View _menu, View _content, View _side)
	{
		header = new HtmlFileView("header.html");
		header_addon = "";
		footer = new HtmlFileView("footer.html");
		menu = _menu;
		content = _content;
		side = _side;
	}
	
	public void render(PrintWriter out)
	{
		header.render(out);
		out.println(header_addon);
		menu.render(out);
		side.render(out);
		content.render(out);		
		footer.render(out);
	}
	
	public void addExternalJS(String url)
	{
		header_addon += "<script type=\"text/javascript\" src=\""+url+"\"></script>";
	}
	public void addExternalCSS(String url)
	{
		header_addon += "<link rel=\"stylesheet\" href=\""+url+"\" type=\"text/css\" media=\"screen\" />";
	}
}
