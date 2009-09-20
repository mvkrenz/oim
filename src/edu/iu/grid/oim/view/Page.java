package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
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
		HashMap<String, String> params = new HashMap<String, String>();
		params.put("__STATICBASE__", StaticConfig.getStaticBase());
		params.put("__APPNAME__", StaticConfig.getApplicationName());
		params.put("__VERSION__", StaticConfig.getVersion());
		if(context.getAuthorization().isGuest()) {
			params.put("__DN__", "Guest");
		} else {
			params.put("__DN__", context.getAuthorization().getUserDN());
		}
		
		header = new HtmlFileView("header.txt", params);
		footer = new HtmlFileView("footer.txt", params);
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
}
