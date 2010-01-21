package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;

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
		params.put("__REF__", getRequestURL(context.getRequest()));
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
	
	private String getRequestURL(HttpServletRequest request) {
		String url = "";
		url += request.getRequestURI();
		if(request.getQueryString() != null) {
			url += "?" + request.getQueryString();
		}
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
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
