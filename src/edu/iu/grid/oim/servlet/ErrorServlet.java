package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

//import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.SendMail_deprecated;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
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
	static Logger log = Logger.getLogger(Authorization.class);  

    public ErrorServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		BootMenuView menuview = new BootMenuView(Context.getGuestContext(), "_error_");
		ContentView contentview = createContentView(request);		
		BootPage page = new BootPage(Context.getGuestContext(), menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(HttpServletRequest request)
	{
		ContentView contentview = new ContentView();
		

		//extract exception info (from attribute)
	    Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
	    if(throwable == null) {
	    	throwable = (Throwable) request.getSession().getAttribute("exception");
	    }
	    
	    //unwrap the exception one-level..
	    if(throwable instanceof ServletException) {
	    	ServletException e = (ServletException)throwable;
	    	if(e.getRootCause() != null) {
	    		throwable = e.getRootCause();
	    	}
	    }

	    if(throwable instanceof AuthorizationException) {
	    	String request_uri = "n/a";
	    	if(request.getSession().getAttribute("request_uri") != null) {
	    		request_uri = request.getSession().getAttribute("request_uri").toString();
	    	}
			contentview.add(new HtmlView("<h2>Authorization Error</h2>"));
			contentview.add(new HtmlView("<div class=\"alert\"><p>You are not authorized to access this page. Click <string>Home</string> on the above menu for more information.</p>"));
			contentview.add(new HtmlView("<p>Detail: "+throwable.getMessage()+"</p>"));
			contentview.add(new HtmlView("<p>If you believe you should have an access this page, <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim?ref="+request_uri+"\">Open GOC ticket</a></p></div>"));
	    } else {
			contentview.add(new HtmlView("<h2>Oops!</h2>"));
			contentview.add(new HtmlView("<div class=\"alert\"><p>Sorry, OIM has encountered a problem. </p>"));
			contentview.add(new HtmlView("<p>Detail: "+throwable.getMessage()+"</p></div>"));
			contentview.add(new HtmlView("<p>The GOC has been notified about this error, however, you can also <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">Open GOC ticket</a></p>"));
		
			//reportError(contentview, message);
			//dump(request, throwable);
	    }
	    
		return contentview;
	}
	
	private void dump(HttpServletRequest request, Throwable throwable)
	{		
		//user info
		log.error("SSL_CLIENT_S_DN: " + request.getAttribute("SSL_CLIENT_S_DN").toString());
		log.error("SSL_CLIENT_I_DN_CN: " + request.getAttribute("SSL_CLIENT_I_DN_CN").toString());
	
		log.error("Exception: " + throwable.getMessage());
		StringBuffer strace = new StringBuffer();
	    for(StackTraceElement trace : throwable.getStackTrace()) {
	    	strace.append(trace.getClassName() + "." + trace.getMethodName()+ "(" + trace.getFileName() + ":" + trace.getLineNumber() + ")\n");
	    }
	    log.error("Stack Trace: " + strace.toString());
	    
	    Integer status_code = (Integer)request.getAttribute("javax.servlet.error.status_code");
	    if(status_code != null) {
	    	log.error("javax.servlet.error.status_code: " + status_code.toString());
	    }
	    log.error("javax.servlet.error.message: " + request.getAttribute("javax.servlet.error.message").toString());
	    log.error("javax.servlet.error.exception_type: " + request.getAttribute("javax.servlet.error.exception_type").toString());
		
	    //put error date
    	Date current = new Date();
    	log.error("Date: " + current.toString());
	}
	
	/*
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
	*/
}
