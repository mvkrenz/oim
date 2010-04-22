package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
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


    public ErrorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		MenuView menuview = new MenuView(Context.getGuestContext(), "_error_");
		ContentView contentview = createContentView(request);		
		Page page = new Page(Context.getGuestContext(), menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(HttpServletRequest request)
	{
		ContentView contentview = new ContentView();
		

		//extract exception info
	    Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
	    if(throwable instanceof ServletException) {
	    	ServletException e = (ServletException)throwable;
	    	if(e.getRootCause() != null) {
	    		throwable = e.getRootCause();
	    	}
	    }
		HashMap<String, String> message = constructMessageDetail(request, throwable);
		
	    if(throwable instanceof AuthorizationException) {
			contentview.add(new HtmlView("<h2>Authorization Error</h2>"));
			contentview.add(new HtmlView("<p>You are not authorized to access this page. Click <string>Home</string> on the above menu for more information.</p>"));
			contentview.add(new HtmlView("<p>If you believe, you should be able to access the page, then please open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"));
	    } else {
			contentview.add(new HtmlView("<h2>Oops!</h2>"));
			contentview.add(new HtmlView("<p>Sorry, OIM has encountered a problem. </p>"));
			contentview.add(new HtmlView("<p>The GOC has been notified about this error; please feel free to additionally open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"));
			reportError(contentview, message);
	    }
	    
		return contentview;
	}
	
	private HashMap<String, String> constructMessageDetail(HttpServletRequest request, Throwable throwable)
	{
		HashMap<String, String> message = new HashMap<String, String>();
		
		//request info (this doesn't work because /error is invoked to display this page)
		//message.put("Request URI", request.getRequestURI());
		//message.put("Request Query", request.getQueryString());
		
		//user info
		message.put("SSL_CLIENT_S_DN", (String)request.getAttribute("SSL_CLIENT_S_DN"));
		message.put("SSL_CLIENT_I_DN_CN", (String)request.getAttribute("SSL_CLIENT_I_DN_CN"));
	
	    message.put("Exception", throwable.getMessage());
		StringBuffer strace = new StringBuffer();
	    for(StackTraceElement trace : throwable.getStackTrace()) {
	    	strace.append(trace.getClassName() + "." + trace.getMethodName()+ "(" + trace.getFileName() + ":" + trace.getLineNumber() + ")\n");
	    }
	    message.put("Stack Trace", strace.toString());
	    
	    Integer status_code = (Integer)request.getAttribute("javax.servlet.error.status_code");
	    if(status_code != null) {
	    	message.put("javax.servlet.error.status_code", status_code.toString());
	    }
	    message.put("javax.servlet.error.message", (String)request.getAttribute("javax.servlet.error.message"));
	    message.put("javax.servlet.error.exception_type", (String)request.getAttribute("javax.servlet.error.exception_type"));
		
	    //put error date
    	Date current = new Date();
    	message.put("Date", current.toString());
    	
	    return message;
	}
	
	private StringBuffer reportError(ContentView contentview, HashMap<String, String> message)
	{
		//create error report
		StringBuffer buffer = new StringBuffer();
		
    	//create error report
    	for (String key : message.keySet()) {
    		buffer.append("[" + key + "]");
    		buffer.append("\n");
    		buffer.append(message.get(key));
    		buffer.append("\n\n");
    	}
    	
		if(StaticConfig.isDebug()) {
			//display to browser
			contentview.add(new HtmlView("<h3>Debug Dump</h3>"));
			contentview.add(new HtmlView("<pre>"+StringEscapeUtils.escapeHtml(buffer.toString())+"</pre>"));
		} else {
			//send report to GOC via email
		   	try {
    			SendMail.sendErrorEmail(buffer.toString());
				contentview.add(new HtmlView("<p>Details of this issue have been sent to the GOC, and will be processed soon. We apologize for the inconvenience.</p>"));			
		   	} catch (MessagingException e) {
				contentview.add(new HtmlView("<p>OIM tried to send an error report to OIM development team, but the attempt has failed due to following reason.</p>"));			
				contentview.add(new HtmlView("<div class=\"indent\">"));
				contentview.add(new HtmlView("<p>"+e.toString()+"</p>"));
				contentview.add(new HtmlView("</div>"));
				contentview.add(new HtmlView("<p>Please open a ticket at <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/other\">GOC Ticket</a> with following details regarding this error.</p>"));
				
				contentview.add(new HtmlView("<div class=\"indent\">"));
				contentview.add(new HtmlView("<pre>"+buffer.toString()+"</pre>"));
				contentview.add(new HtmlView("</div>"));
	    	}
	   	}
		return buffer;
	}
}
