package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

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
	//static Logger log = Logger.getLogger(Authorization.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		//BootMenuView menuview = new BootMenuView(UserContext.getGuestContext(), "_error_");
		BootMenuView menuview = new BootMenuView(context, "_error_");
		
		ContentView contentview = createContentView(context, request);		
		BootPage page = new BootPage(context, menuview, contentview, new SideContentView());
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

	    //construct full URL that caused the error (TODO - I still don't know how to put request parameter back..)
	    String request_uri = (String)request.getAttribute("javax.servlet.error.request_uri");
	    URL context_url = context.getRequestURL();
	    String request_uri_full = context_url.getProtocol()+"://"+context_url.getHost();
	    if(context_url.getPort() != -1) {
	    	request_uri_full += ":" + context_url.getPort();
	    }
	    request_uri_full += request_uri;
	    
	    String ticket_url = "https://ticket.grid.iu.edu/submit?app_issue_check&app_issue_type=goc&app_goc_url="+StringEscapeUtils.escapeHtml(request_uri_full);
	    
	    if(throwable instanceof AuthorizationException) {
			contentview.add(new HtmlView("<h2>Authorization Error</h2>"));
			contentview.add(new HtmlView("<p>You are not authorized to access this page.</p>"));
			contentview.add(new HtmlView("<p>If you believe you should have an access to this page, please <a target=\"_blank\" href=\""+ticket_url+"\">open a GOC ticket</a> with the following detail and request for access.</p>"));
			contentview.add(new HtmlView("<h3>Detail</h3>"));
			contentview.add(new HtmlView("<pre>"+throwable.getMessage()+"</pre>"));
	
			contentview.add(new HtmlView("<h3>URL</h3>"));
			contentview.add(new HtmlView("<pre>"+request_uri_full+"</pre>"));
	    } else {    	
			contentview.add(new HtmlView("<h2>Oops!</h2>"));
			contentview.add(new HtmlView("<p>Sorry, OIM has encountered a problem. </p>"));			
			contentview.add(new HtmlView("<p>If this error occured outside GOC's scheduled maintenance days (normally 2nd and 4th Tuesday) time, " +
				"please <a target=\"_blank\" href=\""+ticket_url+"\">open a GOC ticket</a> with following detail.</p>"));
		
			contentview.add(new HtmlView("<h3>URL</h3>"));
			contentview.add(new HtmlView("<pre>"+request_uri_full+"</pre>"));
			
			contentview.add(new HtmlView("<h3>Exception</h3>"));
			contentview.add(new HtmlView("<pre>"+throwable+"</pre>"));

			contentview.add(new HtmlView("<h3>Call Stack</h3>"));
			contentview.add(new HtmlView("<pre>"));
			for (StackTraceElement ste : throwable.getStackTrace()) {
				contentview.add(new HtmlView(ste.toString()+"\n"));
			}
			contentview.add(new HtmlView("</pre>"));
			
			//reportError(contentview, message);
			//dump(request, throwable);
	    }
	    
		return contentview;
	}
	/*
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
	*/
}
