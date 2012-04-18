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
import edu.iu.grid.oim.model.Context;

public class BootPage implements IView {
	static Logger log = Logger.getLogger(BootPage.class);  
	
    protected Context context;
	private HashMap<String, String> params = new HashMap<String, String>();
	private ArrayList<String> css = new ArrayList<String>();
    
	private HtmlFileView header;
	private IView menu;
	private IView content;
	private HtmlFileView footer;
	private IView side;
	private IView pageheader;
	
	private boolean putsideviewleft = false;
	
	public void addCSS(String path) {
		css.add(path);
	}
	
	public BootPage(Context _context, IView _menu, IView _content, IView _side)
	{
		context = _context;
		header = new HtmlFileView(getClass().getResourceAsStream("boot_header.txt"));
		footer = new HtmlFileView(getClass().getResourceAsStream("boot_footer.txt"));
		menu = _menu;
		content = _content;
		side = _side;
	}
	public void putSideViewLeft(boolean b) {
		putsideviewleft = b;
	}
	
	public void setPageHeader(IView pageheader) {
		this.pageheader = pageheader;
	}

	public void render(PrintWriter out)
	{
		Authorization auth = context.getAuthorization();
		
		if(auth.isGuest()) {
			params.put("__BASE__", StaticConfig.conf.getProperty("application.guestbase"));	
		} else {
			params.put("__BASE__", StaticConfig.getApplicationBase());
		}
		
		if(StaticConfig.isDebug()) {
			params.put("__APPNAME__", StaticConfig.getApplicationName() + " (Debug)");
		} else {
			params.put("__APPNAME__", StaticConfig.getApplicationName());
		}
		params.put("__VERSION__", StaticConfig.getVersion());
		//log.debug(StaticConfig.conf.getProperty("debug"));
		
		try {
			String request_uri = context.getRequestURL();
			if(request_uri != null) {
				request_uri = URLEncoder.encode(request_uri, "UTF-8");
				params.put("__REF__", request_uri);
			} else {
				params.put("__REF__", "unknown_url");		
			}
		} catch (UnsupportedEncodingException e) {
			log.error(e);
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
		
		if(pageheader != null) {
			out.println("<div class=\"page-header\">");
			pageheader.render(out);
			out.println("</div>");
		}
		out.println("<div class=\"container-fluid\">"); //closed in the footer

		out.println("<div class=\"row-fluid\">");

		if(side != null) {
			if(putsideviewleft) {
				out.println("<div class=\"span3\">");
				side.render(out);
				out.println("</div>");//span9		
				
				out.println("<div class=\"span9\">");
				content.render(out);
				out.println("</div>");//span9	
			} else {
				out.println("<div class=\"span9\">");
				content.render(out);
				out.println("</div>");//span9
				
				out.println("<div class=\"span3\">");
				side.render(out);
				out.println("</div>");//span9
			}
		} else {
			out.println("<div class=\"span12\">");
			content.render(out);
			out.println("</div>");
		}
		
		out.println("</div>");//row-fuild

		footer.render(out);
	}
}
