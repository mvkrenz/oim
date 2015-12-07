package edu.iu.grid.oim.view;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import java.net.URLEncoder;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;

public class Page implements IView {
	static Logger log = Logger.getLogger(Page.class);  
	
    protected UserContext context;
	private HashMap<String, String> params = new HashMap<String, String>();
	private ArrayList<String> css = new ArrayList<String>();
    
	private HtmlFileView header;
	private IView menu;
	private IView content;
	private HtmlFileView footer;
	private IView side;
	
	public void addCSS(String path) {
		css.add(path);
	}
	
	public Page(UserContext _context, IView _menu, IView _content, IView _side)
	{
		context = _context;

		header = new HtmlFileView(getClass().getResourceAsStream("header.txt"));
		footer = new HtmlFileView(getClass().getResourceAsStream("footer.txt"));
		menu = _menu;
		content = _content;
		side = _side;
	}

	public void render(PrintWriter out)
	{
		Authorization auth = context.getAuthorization();
		
		/*
		if(auth.isSecure()) {
			params.put("__BASE__", StaticConfig.getApplicationBase());
			params.put("__GOCTICKET__", "https://ticket.grid.iu.edu/goc");
		} else {
			params.put("__BASE__", StaticConfig.conf.getProperty("application.guestbase"));	
			params.put("__GOCTICKET__", "http://ticket.grid.iu.edu/goc");
		}
		*/
		params.put("__BASE__", StaticConfig.conf.getProperty("application.base"));
		params.put("__GOCTICKET__", "https://ticket.grid.iu.edu/goc");
		
		params.put("__APPNAME__", StaticConfig.getApplicationName());
		params.put("__VERSION__", StaticConfig.getVersion());
		
		try {
			String request_uri = context.getRequestURL().toString();
			if(request_uri != null) {
				request_uri = URLEncoder.encode(request_uri, "UTF-8");
				params.put("__REF__", request_uri);
			} else {
				params.put("__REF__", "unknown_url");		
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e);
		} 

		if(context.getAuthorization().isUser()) {
			params.put("__DN__", context.getAuthorization().getUserDN());
		} else if(context.getAuthorization().isUnregistered()) {
			params.put("__DN__", "Unregistered DN (" + context.getAuthorization().getUserDN() + ")");
		} else {
			params.put("__DN__", "Guest");
		}		
		
		String exhead = new String();
		for(String css : this.css) {
			exhead += "<link href=\"css/"+css+"\" rel=\"stylesheet\" type=\"text/css\"/>\n";
		}
		params.put("__EXHEAD__", exhead);
		
		//apply params
		header.applyParams(params);
		footer.applyParams(params);
		
		//finally, render
		header.render(out);
		menu.render(out);
		side.render(out);
		content.render(out);
		footer.render(out);
	}
}
