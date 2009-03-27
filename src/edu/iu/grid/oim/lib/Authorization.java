package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.CertificateDNModel;
import edu.iu.grid.oim.model.db.record.CertificateDNRecord;

//provide client the authorization information
public class Authorization {
	static Logger log = Logger.getLogger(Authorization.class);  
	
	private String user_dn 		= null;
    private Integer dn_id  		= null;
    private Integer contact_id 	= null;
    private Integer authorization_type_id = null; //null = guest
    
    public String getUserDN()
    {
    	return user_dn;
    }
    public Integer getContactID()
    {
    	return contact_id;
    }
    
	public void check(Action action) throws AuthorizationException
	{
		if(!allows(action)) {
			throw new AuthorizationException("Action "+action.toString()+" is not allowed for auth_type_id:" + authorization_type_id);
		}
	}
	
	public Boolean allows(Action action)
	{
		return ActionMatrix.allows(action, authorization_type_id);
	}
	
	//use this ctor ton construct default guest Authorization object
	public Authorization()
	{
	}
	
	public Authorization(HttpServletRequest request, Connection con) throws AuthorizationException 
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
		        byte[] ipAddr = addr.getAddress();
		        String hostname = addr.getHostName();

				log.debug("Server on localhost." +hostname);
		        if(hostname.compareTo("HAYASHIS") == 0) {
					log.debug("Server on localhost. Overriding the DN to Soichi's");
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";
		        }				
		        else if ((hostname.compareTo("LAV-AG-DESKTOP") == 0) || 
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
		CertificateDNModel model = new CertificateDNModel(con);
		CertificateDNRecord dn;
		dn = model.findByDN(user_dn);
		if(dn == null) {
			log.info("The DN not found in Certificate table");
		} else {
			dn_id = dn.id;
			authorization_type_id = dn.authorization_type_id;
			contact_id = dn.contact_id;
			log.debug("The dn_id is " + dn_id);
			log.debug("The authorization_type_id is " + authorization_type_id);
			log.debug("The contact_id is " + contact_id);
		}	
	}
	
	public class AuthorizationException extends ServletException 
	{
		AuthorizationException(String msg) {
			super(msg);
		}
	}
}
