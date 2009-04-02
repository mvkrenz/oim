package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
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
    public Integer getDNID()
    {
    	return dn_id;
    }
    public Integer getContactID()
    {
    	return contact_id;
    }
    
	public void check(String action) throws AuthorizationException
	{
		if(!allows(action)) {
			throw new AuthorizationException("Action:"+action+" is not authorized for " + user_dn);
		}
	}
	
	public Boolean allows(String action)
	{
		return actions.contains(action);

	}
	
	//use this instance for guest authentication
	public static Authorization Guest = new Authorization();
	private Authorization()
	{
	}
	
	public Authorization(HttpServletRequest request) 
	{
		//pull authenticated user dn
		
		//tomcat native way..
		//X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		//user_dn = certs[0].getSubjectDN().getName();
		
		//Use ENV passed from Apache
		user_dn = (String)request.getAttribute("SSL_CLIENT_S_DN");
		
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
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511";
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
		
		DNRecord certdn;

		try {
			DNModel dnmodel = new DNModel(Authorization.Guest);
			certdn = dnmodel.getByDNString(user_dn);
			if(certdn == null) {
				log.info("The DN not found in Certificate table");
			} else {
				dn_id = certdn.id;
				contact_id = certdn.contact_id;
				
				DNAuthorizationTypeModel dnauthtypemodel = new DNAuthorizationTypeModel(Authorization.Guest);
				Collection<Integer> auth_type_ids = dnauthtypemodel.getAuthorizationTypesByDNID(certdn.id);
				AuthorizationTypeActionModel authactionmodel = new AuthorizationTypeActionModel(Authorization.Guest);
				ActionModel actionmodel = new ActionModel(Authorization.Guest);
				for(Integer auth_type_id : auth_type_ids) {
					Collection<Integer> aids = authactionmodel.getActionByAuthTypeID(auth_type_id);
					for(Integer aid : aids) {
						actions.add(actionmodel.get(aid).name);
					}
				}
			}	
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
