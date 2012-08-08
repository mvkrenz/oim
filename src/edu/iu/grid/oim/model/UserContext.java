package edu.iu.grid.oim.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;

//provides easy access to various object that are user specific
public class UserContext {
    static Logger log = Logger.getLogger(UserContext.class);  
    private DivRepRoot divrep_root;
    private String divrep_pageid = null;
	private DivRepPage divrep_pageroot = null;
	
	private Authorization auth = new Authorization();
	//private Connection connection;
	
	private String request_url;
	private String secure_url; //counter part for request_url in https (same if request_url is secure already)
	private String guesthome_url;
	
	private HttpSession session;
	private String remote_addr;
	
	//let's lookup once per user request (not session)
    private DataSource oimds;

	//stores the reason for current transaction (used for log table)
	//why should this work? because *usually* all update within a session occurs under common purpose.
	private String comment;
	public void setComment(String _comment) { comment = _comment; }
	public String getComment() { return comment; }
	public String getRemoteAddr() { return remote_addr; }
	public HttpSession getSession() { return session; }
	
	public UserContext(HttpServletRequest request) throws AuthorizationException
	{	
		//don't store request object because it can get stale really fast... (was causing issue when divrep tries to get session from it)
		//request = _request;	
		
		session = request.getSession();
		auth = new Authorization(request);

		//set request url
		request_url = request.getRequestURI();
		if(request.getQueryString() != null) {
			request_url += "?" + request.getQueryString();
		}
		if(request.isSecure()) {
			secure_url = request_url;
		} else {
			//need to compose secure version of current url (with https)
			/*
			log.debug(request.getRequestURI());
			log.debug(request.getRequestURL());
			log.debug(request.getLocalAddr());
			log.debug(request.getLocalName());
			log.debug(request.getPathInfo());
			log.debug(request.getQueryString());
			*/
			secure_url = "https://" + request.getLocalName();
			if(StaticConfig.conf.getProperty("application.secureport") != null) {
				secure_url += ":"+StaticConfig.conf.getProperty("application.secureport");
			}
			secure_url += request.getRequestURI();
			if(request.getQueryString() != null) {
				secure_url += "?" + request.getQueryString();
			}
		}
		guesthome_url = "http://"+request.getLocalName();
		if(StaticConfig.conf.getProperty("application.guestport") != null) {
			guesthome_url += ":"+StaticConfig.conf.getProperty("application.guestport");
		}
		guesthome_url += StaticConfig.conf.getProperty("application.base");
		
		divrep_root = DivRepRoot.getInstance(request.getSession());
		divrep_pageid = request.getRequestURI() + request.getQueryString();
		remote_addr = request.getRemoteAddr();
		
		//make sure user can bind only 1 IP address (to prevent session fixation attack)
		String addr = (String)session.getAttribute("remote_addr");
		if(addr == null) {
			//never initialized - just store it
			session.setAttribute("remote_addr", remote_addr);
		} else {
			if(!addr.equals(remote_addr)) {
				throw new AuthorizationException("Invalid IP address");
			}
		}
	}
	
	public Connection getConnection() throws SQLException {
		if(oimds == null) {
		    try {
		    	log.debug("Looking for jdbc connection");
		    	Context initContext = new InitialContext();
		    	Context envContext  = (Context)initContext.lookup("java:/comp/env");
		    	oimds = (DataSource)envContext.lookup("jdbc/oim");
		    	log.debug(oimds.toString());
		    } catch( NamingException ne ) {
		    	throw new RuntimeException( "Unable to aquire data source", ne );
		    }	
		}
    	log.debug("Connecting..");
		Connection oim = oimds.getConnection();
    	log.debug(oim.toString());
		return oim;
	}
	
	public static UserContext getGuestContext()
	{
		return new UserContext();
	}
	public String getSecureUrl() {
		return secure_url;
	}
	public String getGuesHomeUrl() {
		//TODO
		return guesthome_url;
	}
	
	//used to create guest context
	private UserContext(){}	
	
	public Authorization getAuthorization()
	{
		return auth;
	}
	public DivRepPage getPageRoot()
	{
		if(divrep_pageroot == null) {
			log.debug("Initializing divrep page root for: " + divrep_pageid);
			divrep_pageroot = divrep_root.initPage(divrep_pageid);
		}
		return divrep_pageroot;
	}
	
	public String getRequestURL()
	{
		return request_url;
	}
	
	/*
	private void setRequestURL(HttpServletRequest request) {
		request_url = "";
		if(request != null) {
			request_url += request.getRequestURI();
			if(request.getQueryString() != null) {
				request_url += "?" + request.getQueryString();
			}
		}
	}
	*/
	
	public enum MessageType {WARNING, ERROR, SUCCESS, INFO};	
	public class Message {
		Message(MessageType type, String text) {
			this.type = type;
			this.text = text;
		}
		public MessageType type;
		public String text;
	}
	public void message(MessageType type, String text) {
		if(session != null) {
			Message msg = new Message(type, text);
			ArrayList<Message> messages = (ArrayList<Message>)session.getAttribute("messages");
			if(messages == null) {
				messages = new ArrayList<Message>();
			}
			messages.add(msg);
			session.setAttribute("messages", messages);
		} else {
			//guest context doesn't have session - used on error page, for example
			//log.error("Failed to add message since no session is associated with this UserContext");
		}
	}
	
	//could return null if session is not set, or no message
	public ArrayList<Message> flushMessages() {
		if(session != null) {
			ArrayList<Message> messages = (ArrayList<Message>)session.getAttribute("messages");
			session.removeAttribute("messages");
			return messages;
		} else {
			//guest context doesn't have session - used on error page, for example
			//log.error("Failed to flush messages since no session is associated with this UserContext");
		}
		return null;
	}
}
