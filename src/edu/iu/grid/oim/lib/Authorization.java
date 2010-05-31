package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;

//provide client the authorization information
public class Authorization {
	static Logger log = Logger.getLogger(Authorization.class);  
	
	private Context guest_context;
	
	private String user_dn = null;
	private String user_cn = null;
	private Integer dn_id = null;
    private Integer contact_id = null;
    private Boolean isDisabled = false;
    private Boolean islocal = false;
    //private TimeZone timezone = null;
    
    private HashSet<String> actions = new HashSet<String>();
    private HashSet<String> auth_types = new HashSet<String>();
    
    public Boolean isGuest()
    {
    	if(user_dn == null || user_cn == null) return true;
    	return false;
    }
    public Boolean isOIMUser()
    {
    	if ((dn_id == null) || (isDisabled())) return false;
    	return true;
    }
    public Boolean isDisabledOIMUser()
    {
    	if ((dn_id != null) && (isDisabled())) return true;
    	return false;
    }
    
    public String getUserDN() { return user_dn; }
    public String getUserCN() { return user_cn; }
    public Integer getDNID() { return dn_id; }
    public Integer getContactID() { return contact_id; }
    public ContactRecord getContact() throws SQLException
    {
    	ContactModel model = new ContactModel(guest_context);
    	return model.get(contact_id);
    }
    //public TimeZone getTimeZone() { return timezone; }
    
    //true if the client is accessing from localhost
    public Boolean isLocal() { return islocal; }
    public Boolean isDisabled() { return isDisabled; }
    
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
	
	public HashSet<String> getAuthorizationTypesForCurrentDN(){
		return auth_types;
	}
	
	public String getNoDNWarning() {
		return "<p><br/></p><h2>Warning! No X509 Certificate Detected in Web Browser!</h2>"+
		"<div class=\"warning\">" +
		"<p>OIM requires the Distinguished Name (DN) of an X509 certificate issued by an <a target=\"_blank\" href='http://software.grid.iu.edu/cadist/'>OSG-approved Certifying Authority (CA)</a> to be registered in order to proceed. No X509 certificate was detected on your web browser.</p>"+
		"<p>Please <strong>provide an X509 certificate</strong> issued by an OSG-approved CA</a> via your web browser. This website will allow you to register your certificate's DN with the OIM system, if it is not already registered.</p><p>If you are not sure how to register, or have any questions, please open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"+
		"</div><p><br/></p>";
	}
	
	public String getUnregisteredUserWarning() {
		return "<p><br/></p><h2>Warning! Unregistered User!</h2>"+
		"<div class=\"warning\">" + 
		"<p class=\"warning\">OIM requires the Distinguished Name (DN) of an X509 certificate issued by an <a target=\"_blank\" href='http://software.grid.iu.edu/cadist/'>OSG-approved Certifying Authority (CA)</a> to be registered in order to proceed.</p><p>The following unregistered DN was detected from your web browser: <br/> <strong>" + getUserDN()+ "</strong>.</p>" +
		"<p>Please <strong>register your certificate's DN</strong> with the OIM system using the <strong>Register</strong> menu item above, so you can be allowed to proceed further.</p><p>If you believe, you have previously registered this DN, or are not sure how to register, or have any other questions, please open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"+
		"</div><p><br/></p>"; 
	}
	
	public String getDisabledUserWarning() {
		return "<h2>Warning! De-activated User Account!</h2>"+
			"<div class=\"warning\">"+
			"<p>OIM requires the Distinguished Name (DN) of an X509 certificate issued by an <a target=\"_blank\" href='http://software.grid.iu.edu/cadist/'>OSG-approved Certifying Authority (CA)</a> to be registered in order to proceed.</p>"+
			"<p>The following unregistered DN was detected from your web browser: <br/> <strong>" + getUserDN() + "</strong></p>" +
			"<p>This DN is indeed <strong>already registered</strong> but the account associated with the DN is <strong>de-activated</strong>. </p>"+
			"<p>If you believe, this is in error, or have any other questions, please open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"+
			"</div>";
	}

	
	//Guest
	public Authorization() {}
	
	//pull user_dn from Apache's SSL_CLIENT_S_DN
	public Authorization(HttpServletRequest request) throws AuthorizationException 
	{		
		guest_context = Context.getGuestContext();
		
		//we set mod_jk to return "none" if the value doesn't exist. let's convert back to null.
		String user_dn_tmp = (String)request.getAttribute("SSL_CLIENT_S_DN");
		if(user_dn_tmp != null && !user_dn_tmp.equals("none")) {
			user_dn = user_dn_tmp;
		}
		String user_cn_tmp = (String)request.getAttribute("SSL_CLIENT_I_DN_CN");
		if(user_cn_tmp != null && !user_cn_tmp.equals("none")) {
			user_cn = user_cn_tmp;
		}
		
		String localname = request.getLocalName();
		if(localname.equals("localhost") ||
				localname.equals("localhost.localdomain") ||
				localname.equals("0.0.0.0")) {
			islocal = true;
			log.info("localuser... auth.isLocal() will be true");
		}
		
		//debug - for development
		if(islocal) {
			InetAddress addr;
			try {
				//override user_cn
		        user_cn = StaticConfig.getDOECN();
		        
		        //override with fake dn
				addr = InetAddress.getLocalHost();
		        String hostname = addr.getHostName();
				log.debug("Server on localhost." +hostname);			
		        if(hostname.compareTo("d830") == 0) {
					log.debug("Server on localhost. Overriding the DN to Soichi's");
					//user_dn = null; //browser didn't give us any dn
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";		
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Carl Lundstedt 229191";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Tim Silvers 993975";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Tiberiu Stef-Praun 764752";
					//user_cn = null;
		        } else if ((hostname.compareTo("lav-ag-desktop") == 0) || 
		        	(hostname.compareTo("SATRIANI") == 0)){
					log.debug("Server on localhost. Overriding the DN to Arvind's");
					// // Test when No DN is provided
					user_dn = "/DC=org/DC=doegrids/OU=People/CN=Arvind Gopu 369621";  // GOC staff
					// user_dn = "/DC=org/DC=doegrids/OU=People/CN=Arvind Gopu 3696212224546";  // Fake Arvind DN that is not registered
					// user_dn = "/C=KR/O=KISTI/O=GRID/O=KISTI/CN=84035421 Beob Kyum Kim";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Arvind Gopu 369621222";  // Fake Arvind DN that is registered but de-activated
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Robert C Ball 331645"; // AGLT2 Admin
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Brian Bockelman 504307"; // Measurements 
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Robert W. Gardner Jr. 669916" ; // AGLT2 (ATLAS) vo owner's manager
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511";      //OSG user
					//user_dn = "/DC=gov/DC=fnal/O=Fermilab/OU=People/CN=Keith Chadwick/CN=UID:chadwick"; // End user VO admin
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Mine Altunay 215076"; // Security auth
		        }		
			} catch (UnknownHostException e) {
				//ignore then..
			}
		}
		
		//outpu some log
		log.info(request.getRequestURI() + "?" + request.getQueryString());
		log.info("Authenticated User DN: "+user_dn + " SSL_CLIENT_I_DN_CN: " + user_cn);
		
		if(user_cn == null) {
			log.info("SSL_CLIENT_I_DN_CN is not set. Logging in as guest.");
		} else {
			try {
				DNModel dnmodel = new DNModel(guest_context);
				initAction(dnmodel.getByDNString(user_dn));
			} catch (SQLException e) {
				throw new AuthorizationException("Authorization check failed due to " + e.getMessage());
			}
		}
	}
	
	private void initAction(DNRecord certdn) throws SQLException
	{
		if(certdn == null) {
			log.info("The DN not found in \"dn\" table");
		} else {
			contact_id = certdn.contact_id;
			dn_id = certdn.id;
			user_dn = certdn.dn_string;

			ContactModel cmodel = new ContactModel(guest_context);
			ContactRecord contact = cmodel.get(contact_id);
			if (contact.isDisabled()) {
				isDisabled = true;
				log.info("The DN found in \"dn\" table but is mapped to disabled contact. Set isDisabled to true.");
			}
			else {
				DNAuthorizationTypeModel dnauthtypemodel = new DNAuthorizationTypeModel(guest_context);
				AuthorizationTypeModel authtypemodel = new AuthorizationTypeModel(guest_context); 
				Collection<Integer> auth_type_ids = dnauthtypemodel.getAuthorizationTypesByDNID(certdn.id);

				AuthorizationTypeActionModel authactionmodel = new AuthorizationTypeActionModel(guest_context);
				ActionModel actionmodel = new ActionModel(guest_context);
				
				for (Integer auth_type_id : auth_type_ids) {
					auth_types.add(authtypemodel.get(auth_type_id).name);
					Collection<Integer> aids = authactionmodel.getActionByAuthTypeID(auth_type_id);
					for (Integer aid : aids) {
						actions.add(actionmodel.get(aid).name);
					}
				}
			}
		}
	}
}
