package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeActionModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;

//provide client the authorization information
public class Authorization {
	static Logger log = Logger.getLogger(Authorization.class);  
	
	private Context guest_context;
	
	private String user_dn = null;
	private String user_cn = null;
	private Integer dn_id = null;
    private Integer contact_id = null;
    
    private HashSet<String> actions = new HashSet<String>();
    
    public String getUserDN() { return user_dn; }
    public String getUserCN() { return user_cn; }
    public Integer getDNID() { return dn_id; }
    public Integer getContactID() { return contact_id; }
    public ContactRecord getContact() throws SQLException
    {
    	ContactModel model = new ContactModel(guest_context);
    	return model.get(contact_id);
    }
    
	public void check(String action) throws AuthorizationException
	{
		if(!allows(action)) {
			// TODO Need cleaner error message in these situations -agopu
			throw new AuthorizationException("Action:"+action+" is not authorized for " + user_dn);
		}
	}
	
	public Boolean allows(String action)
	{
		return actions.contains(action);
	}
	
	//Guest
	public Authorization() {}
	
	//pull user_dn from Apache's SSL_CLIENT_S_DN
	public Authorization(HttpServletRequest request, Connection connection) 
	{		
		guest_context = Context.getGuestContext(connection);
		
		//tomcat native way..
		//X509Certificate[] certs = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
		//user_dn = certs[0].getSubjectDN().getName();
		
		user_dn = (String)request.getAttribute("SSL_CLIENT_S_DN");
		user_cn = (String)request.getAttribute("SSL_CLIENT_I_DN_CN");
		
		//debug - for development
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
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511"; //OSG user
		        } else if ((hostname.compareTo("lav-ag-desktop") == 0) || 
		        	(hostname.compareTo("SATRIANI") == 0)){
					log.debug("Server on localhost. Overriding the DN to Arvind's");
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Arvind Gopu 369621";  // GOC
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Robert C Ball 331645"; // AGLT2 Admin
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Robert W. Gardner Jr. 669916" ; // AGLT2 vo owner's manager
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511";      //OSG user
					// user_dn = "/DC=gov/DC=fnal/O=Fermilab/OU=People/CN=Keith Chadwick/CN=UID:chadwick"; // End user VO admin
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Mine Altunay 215076"; // Security auth
		        }		
		        user_cn = Config.getDOECN();
			} catch (UnknownHostException e) {
				//ignore then..
			}
		}
		
		log.info("Authenticated User DN: "+user_dn);
		log.info("SSL_CLIENT_I_DN_CN: " + user_cn);
		
		if(!user_cn.equals(Config.getDOECN())) {
			log.error("DN_CN is not \""+Config.getDOECN()+"\". It is \"" + user_cn + "\". Logging in as guest.");
		} else {
			try {
				DNModel dnmodel = new DNModel(guest_context);
				initAction(dnmodel.getByDNString(user_dn));
			} catch (SQLException e) {
				log.error(e);
			}
		}
	}
	
	private void initAction(DNRecord certdn) throws SQLException
	{
		if(certdn == null) {
			log.info("The DN not found in Certificate table");
		} else {
			dn_id = certdn.id;
			contact_id = certdn.contact_id;
			user_dn = certdn.dn_string;
			
			DNAuthorizationTypeModel dnauthtypemodel = new DNAuthorizationTypeModel(guest_context);
			Collection<Integer> auth_type_ids = dnauthtypemodel.getAuthorizationTypesByDNID(certdn.id);
			AuthorizationTypeActionModel authactionmodel = new AuthorizationTypeActionModel(guest_context);
			ActionModel actionmodel = new ActionModel(guest_context);
			for(Integer auth_type_id : auth_type_ids) {
				Collection<Integer> aids = authactionmodel.getActionByAuthTypeID(auth_type_id);
				for(Integer aid : aids) {
					actions.add(actionmodel.get(aid).name);
				}
			}
		}	
	}

	public class AuthorizationException extends ServletException 
	{
		AuthorizationException(String msg) {
			super(msg);
		}
	}
}
