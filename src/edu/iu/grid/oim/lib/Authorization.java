package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.AuthorizationModel;
import edu.iu.grid.oim.model.db.record.DNRecord;

//provide client the authorization information
public class Authorization {
	static Logger log = Logger.getLogger(Authorization.class);  
	
	private String user_dn = null;
    private Integer dn_id = null;
    private Integer contact_id = null;
    
    private HashSet<String> actions = new HashSet();
    
    public String getUserDN()
    {
    	return user_dn;
    }
    public Integer getContactID()
    {
    	return contact_id;
    }
    
	public void check(String action) throws AuthorizationException
	{
		if(!allows(action)) {
			throw new AuthorizationException("Action ["+action+"] is not allowed for dn_id:" + dn_id);
		}
	}
	
	public Boolean allows(String action)
	{
		return actions.contains(action);

	}
	
	//use this ctor ton construct default guest Authorization object
	public Authorization()
	{
	}
	
	public Authorization(HttpServletRequest request, Connection con) 
	{
		//pull authenticated user dn
		
		//tomcat native way..
		//X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		//user_dn = certs[0].getSubjectDN().getName();
		
		//Use ENV passed from Apache
		String user_dn = (String)request.getAttribute("SSL_CLIENT_S_DN");
		
		//in order to test this locally (with no Apache SSL handling..)
		//let's override DN to my DN.
		if(request.getLocalName().compareTo("localhost") == 0) {
			InetAddress addr;
			try {
				addr = InetAddress.getLocalHost();
		        //byte[] ipAddr = addr.getAddress();
		        String hostname = addr.getHostName();

				log.debug("Server on localhost." +hostname);
		        if(hostname.compareTo("HAYASHIS") == 0) {
					log.debug("Server on localhost. Overriding the DN to Soichi's");
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";
		        } else if ((hostname.compareTo("LAV-AG-DESKTOP") == 0) || 
		        	(hostname.compareTo("SATRIANI") == 0)){
					log.debug("Server on localhost. Overriding the DN to Arvind's");
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Arvind Gopu 369621";
		        }				
			} catch (UnknownHostException e) {
				//ignore then..
			}
		}
		
		log.info("Authenticated User DN: "+user_dn);
		
		//find DNID
		AuthorizationModel model = new AuthorizationModel(con);
		DNRecord certdn;

		try {
			certdn = model.findByDN(user_dn);
			if(certdn == null) {
				log.info("The DN not found in Certificate table");
			} else {
				dn_id = certdn.id;		
				Collection<Integer> auth_type_ids = model.getAuthTypes(dn_id);
				for(Integer auth_type_id : auth_type_ids) {
					Collection<Integer> aids = model.getActionIDs(auth_type_id);
					for(Integer aid : aids) {
						actions.add(model.getAction(aid));
					}
				}
			}	
		} catch (AuthorizationException e) {
			log.error(e);
		} catch (SQLException e) {
			log.error(e);
		}

	}
	
	public class AuthorizationException extends ServletException 
	{
		AuthorizationException(String msg) {
			super(msg);
		}
	}
}
