package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;

public class Page implements IView {
	
    protected Context context;
    
	private IView header;
	private IView menu;
	private IView content;
	private IView footer;
	private IView side;
	
	public Page(Context _context, IView _menu, IView _content, IView _side)
	{
		context = _context;
		
		header = new HtmlFileView(context, "header.txt");
		footer = new HtmlFileView(context, "footer.txt");
		menu = _menu;
		content = _content;
		side = _side;
	}
	
	public void render(PrintWriter out)
	{
		header.render(out);
		menu.render(out);
		side.render(out);
		content.render(out);		
		footer.render(out);
	}
	/*
	public void addExternalJS(String url)
	{
		header_addon += "<script type=\"text/javascript\" src=\""+url+"\"></script>";
	}
	public void addExternalCSS(String url)
	{
		header_addon += "<link rel=\"stylesheet\" href=\""+url+"\" type=\"text/css\" media=\"screen\" />";
	}
	*/
}
