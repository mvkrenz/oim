package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.SendMail;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;

/**
 * Servlet implementation class ErrorServlet
 */
public class ErrorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	//attributes to display on debug dump
	protected static String[] vars = {
        "javax.servlet.error.status_code",
        "javax.servlet.error.exception_type",
        "javax.servlet.error.message",
        "javax.servlet.error.exception",
        "javax.servlet.error.request_uri"
    };

    public ErrorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		MenuView menuview = new MenuView(Context.getGuestContext(), "_error_");
		ContentView contentview = createContentView(request);		
		Page page = new Page(menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(HttpServletRequest request)
	{
		ContentView contentview = new ContentView();
        
		contentview.add(new HtmlView("<h2>Oops!</h2>"));
		contentview.add(new HtmlView("<p>Sorry, OIM has encountered a problem.</p>"));
		
		if(Config.isDebug()) {
			contentview.add(new HtmlView("<table>"));
	        for (int i = 0; i < vars.length; i++) {
	    		contentview.add(new HtmlView("<TR><TD>" + vars[i] + "</TD><TD>" +
	                request.getAttribute(vars[i]) + 
	                "</TD></TR>"));
	        }
	        contentview.add(new HtmlView("</table>"));
		} else {
			//create error report
			StringBuffer message = new StringBuffer();
			
			//put error date
	    	Date current = new Date();
	    	message.append("Date: ");
	    	message.append(current);
	    	message.append("\n\n");
	    	
	    	//dump request object
	    	for (Enumeration e = request.getAttributeNames() ; e.hasMoreElements() ;) {
	    		String key = (String)e.nextElement();
	        	message.append(key);
	        	message.append("\n");
	        	message.append(request.getAttribute(key));
	        	message.append("\n\n");
	    	}
	    	
	    	try {
	    		SendMail.sendErrorEmail(message.toString());
				contentview.add(new HtmlView("<p>Detail of this issue has been sent to GOC and GOC will be processing this issue soon. We appologize for your inconvenience.</p>"));			
	    	} catch (MessagingException e) {
				contentview.add(new HtmlView("<p>OIM has tried to send the error report to OIM development team, but the attemp has failed due to following reason.</p>"));			
				contentview.add(new HtmlView("<div class=\"indent\">"));
				contentview.add(new HtmlView("<p>"+e.toString()+"</p>"));
				contentview.add(new HtmlView("</div>"));
				contentview.add(new HtmlView("<p>Please open a ticket at GOC with following error report</p>"));
				contentview.add(new HtmlView("<div class=\"indent\">"));
				contentview.add(new HtmlView("<pre>"+message.toString()+"</pre>"));
				contentview.add(new HtmlView("</div>"));
	    	}
		}
		
		return contentview;
	}
}
