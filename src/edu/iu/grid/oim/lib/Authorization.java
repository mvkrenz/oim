package edu.iu.grid.oim.lib;

import java.security.cert.X509Certificate;
import java.sql.Connection;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import edu.iu.grid.oim.model.CertificateDNModel;
import edu.iu.grid.oim.model.record.CertificateDNRecord;

//provide client the authorization information
public class Authorization {
	static Logger log = Logger.getLogger(Authorization.class);  
	
	private String user_dn = null;
    private Integer dn_id = null;
    private int auth_type_id = 0; //0 = guest
    
	
	public void check(Action action) throws AuthorizationException
	{
		if(!ActionMatrix.allows(action, auth_type_id)) {
			throw new AuthorizationException("Action "+action.toString()+" is not allowed");
		}
	}
	
	public Authorization(HttpServletRequest request, Connection con) {
		//pull authenticated user dn
		X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		if(certs != null) {
			user_dn = certs[0].getSubjectDN().getName();
			log.info("Authenticated User: "+user_dn);
			
			CertificateDNModel model = new CertificateDNModel(con, null);
			CertificateDNRecord certdn = model.findByDN("/DC=org/DC=doegrids/OU=People/CN=Arvind Gopu 369621");
			if(certdn == null) {
				log.info("The DN not found in Certificate table : " + user_dn);
			} else {
				dn_id = certdn.getID();
				auth_type_id = certdn.getAuthTypeID();
				log.debug("The dn_id is " + dn_id);
				log.debug("The auth_type_id is " + auth_type_id);
			}		
		} else {
			log.info("User with no DOE Cert has logged in (guest?)");
		}
	}
	
	public class AuthorizationException extends Exception 
	{
		AuthorizationException(String msg) {
			super(msg);
		}
	}
}
