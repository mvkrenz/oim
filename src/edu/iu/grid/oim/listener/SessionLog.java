package edu.iu.grid.oim.listener;

import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;

import org.apache.log4j.Logger;

public class SessionLog implements HttpSessionListener {

    static Logger log = Logger.getLogger(SessionLog.class);  

    @Override
	public void sessionCreated(HttpSessionEvent se) {
		log.debug("Session created: " + se.toString());
	}

    @Override
	public void sessionDestroyed(HttpSessionEvent se) {
		log.info("Session destroyed: " + se.toString());
		HttpSession session = se.getSession();
		Enumeration e = session.getAttributeNames();
		StringBuffer names = new StringBuffer();
		while(e.hasMoreElements()) {
			String key = (String)e.nextElement();
			names.append(key);
			names.append(" " );
		}
		log.info("Attributes: " + names.toString());
	}
}