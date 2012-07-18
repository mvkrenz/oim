package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.SideContentView;

/**
 * Servlet implementation class ErrorServlet
 */
public class ErrorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(Authorization.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		BootMenuView menuview = new BootMenuView(UserContext.getGuestContext(), "_error_");
		
		ContentView contentview = createContentView(context, request);		
		BootPage page = new BootPage(UserContext.getGuestContext(), menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(UserContext context, HttpServletRequest request)
	{
		ContentView contentview = new ContentView(context);

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
			contentview.add(new HtmlView("<p>Sorry, OIM has encountered a problem. </p>"));
			contentview.add(new HtmlView("<div class=\"alert\">"));
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
}
