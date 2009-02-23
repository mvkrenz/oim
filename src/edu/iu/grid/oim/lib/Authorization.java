package edu.iu.grid.oim.lib;

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
	
	private String user_dn = null;
    private Integer dn_id = null;
    private Integer auth_type_id = null; //null = guest
    private Integer person_id = null;
    
    public String getUserDN()
    {
    	return user_dn;
    }
    public Integer getPersonID()
    {
    	return person_id;
    }
    
	public void check(Action action) throws AuthorizationException
	{
		if(!ActionMatrix.allows(action, auth_type_id)) {
			throw new AuthorizationException("Action "+action.toString()+" is not allowed for auth_type_id:" + auth_type_id);
		}
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
			log.debug("Server on localhost. Overriding the DN to Soichi's");
			user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";
		}
		
		log.info("Authenticated User DN: "+user_dn);
		
		//find DNID
		CertificateDNModel model = new CertificateDNModel(con, new Authorization());
		CertificateDNRecord certdn;
		certdn = model.findByDN(user_dn);
		if(certdn == null) {
			log.info("The DN not found in Certificate table");
		} else {
			dn_id = certdn.id;
			auth_type_id = certdn.auth_type_id;
			person_id = certdn.person_id;
			log.debug("The dn_id is " + dn_id);
			log.debug("The auth_type_id is " + auth_type_id);
			log.debug("The person_id is " + person_id);
		}	
	}
	
	public class AuthorizationException extends ServletException 
	{
		AuthorizationException(String msg) {
			super(msg);
		}
	}
}
