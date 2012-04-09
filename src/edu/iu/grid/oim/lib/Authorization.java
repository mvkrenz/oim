package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import java.util.TimeZone;

//provide client the authorization information
public class Authorization {
	static Logger log = Logger.getLogger(Authorization.class);  
	
	private Context guest_context; //used to access DB befort auth/context is fully constructed (avoid chicken/egg)
	
	//internal states
	private String user_dn = null;
	private String user_cn = null;
	//private String client_verify = null;
	//private Integer dn_id = null;
    //private Integer contact_id = null;
	private DNRecord dnrec = null;
    private ContactRecord contact = null;
    public String getUserDN() { return user_dn; }
    public String getUserCN() { return user_cn; }
    public Integer getDNID() { 
    	if(dnrec != null) {
    		return dnrec.id; 
    	}
    	return null;
    }
    //public Integer getContactID() { return contact_id; }
    public ContactRecord getContact() 
    {
    	/*
    	ContactModel model = new ContactModel(guest_context);
    	return model.get(contact_id);
    	*/
    	return contact;
    }
    
    public TimeZone getTimeZone() {
		//load timezone
		if(contact != null) {
    		String id = contact.timezone;
    		if(id != null) {
    			return TimeZone.getTimeZone(id);
    		}
		}
		
		return TimeZone.getDefault();
    }
    
    //User type
    static enum UserType { GUEST, NOCERT, UNREGISTERED, DISABLED, USER, LOCAL };
    private UserType usertype;
    public UserType getUserType() { return usertype; }
    public Boolean isLocal() { return (usertype == UserType.LOCAL); }
    public Boolean isDisabled() { return (usertype == UserType.DISABLED); }
    public Boolean isGuest() { return (usertype == UserType.GUEST); }
    public Boolean isUser() { return (usertype == UserType.USER); }
    public Boolean isUnregistered() { return (usertype == UserType.UNREGISTERED); }
    
    //authorization
    private HashSet<String> actions = new HashSet<String>();
    private HashSet<String> auth_types = new HashSet<String>();
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

	/*
	//TODO  MOVE THIS CRAP to VIEW
	public String getNoDNWarning() {
		return "<h2>Warning! No X509 Certificate Detected in Web Browser!</h2>"+
		"<div class=\"warning\">" +
		"<p>OIM requires the Distinguished Name (DN) of an X509 certificate issued by an <a target=\"_blank\" href='http://software.grid.iu.edu/cadist/'>OSG-approved Certifying Authority (CA)</a> to be registered in order to proceed. No X509 certificate was detected on your web browser.</p>"+
		"<p>Please <strong>provide an X509 certificate</strong> issued by an OSG-approved CA</a> via your web browser. This website will allow you to register your certificate's DN with the OIM system, if it is not already registered.</p><p>If you are not sure how to register, or have any questions, please open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"+
		"</div>";
	}
	public String getUnregisteredUserWarning() {
		return "<h2>Warning! Unregistered User!</h2>"+
		"<div class=\"warning\">" + 
		"<p class=\"warning\">OIM requires the Distinguished Name (DN) of an X509 certificate issued by an <a target=\"_blank\" href='http://software.grid.iu.edu/cadist/'>OSG-approved Certifying Authority (CA)</a> to be registered in order to proceed.</p><p>The following unregistered DN was detected from your web browser: <br/> <strong>" + getUserDN()+ "</strong>.</p>" +
		"<p>Please <strong>register your certificate's DN</strong> with the OIM system using the <strong>Register</strong> menu item above, so you can be allowed to proceed further.</p><p>If you believe, you have previously registered this DN, or are not sure how to register, or have any other questions, please open <a target=\"_blank\" href=\"https://ticket.grid.iu.edu/goc/oim\">a ticket</a> with the OSG Grid Operations Center (GOC).</p>"+
		"</div>"; 
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
	*/
	
	//used to create default Context
	public Authorization() {
		usertype = UserType.GUEST;
	}
	
	//pull user_dn from Apache's SSL_CLIENT_S_DN
	public Authorization(HttpServletRequest request) throws AuthorizationException 
	{		
		guest_context = Context.getGuestContext();
		usertype = UserType.GUEST;
		
		String localname = request.getLocalName();
		if(localname.equals("localhost") || localname.equals("localhost.localdomain") || localname.equals("0.0.0.0")) {
			usertype = UserType.LOCAL;
			debugAuthOverride(request);
		}
		
		//figure out usertype
		String client_verify = (String)request.getAttribute("SSL_CLIENT_VERIFY");
		if(client_verify != null && !client_verify.equals("none")) {
			//user is accessing via https
			
			//we set mod_jk to return "none" if the value doesn't exist. let's convert back to null.
			String user_dn_tmp = (String)request.getAttribute("SSL_CLIENT_S_DN");
			if(user_dn_tmp != null && !user_dn_tmp.equals("none")) {
				user_dn = user_dn_tmp;
			}
			String user_cn_tmp = (String)request.getAttribute("SSL_CLIENT_I_DN_CN");
			if(user_cn_tmp != null && !user_cn_tmp.equals("none")) {
				user_cn = user_cn_tmp;
			}
			log.info(request.getRequestURI() + "?" + request.getQueryString());
			log.info("Authenticated User DN: "+user_dn + " SSL_CLIENT_I_DN_CN: " + user_cn);
			
			if(user_cn == null) {
				log.info("SSL_CLIENT_I_DN_CN is not set. Logging in as guest.");
			} else {
				if(client_verify == null || !(client_verify.equals("SUCCESS"))) {
					log.info("SSL_DN / CN is set, but CLIENT_VERIFY has failed :: "+client_verify+". Logging in as guest");
					user_dn = null;
					user_cn = null;
				} else {
					//login as OIM user
					try {
						//check to see if the DN is already registered
						DNModel dnmodel = new DNModel(guest_context);
						dnrec = dnmodel.getByDNString(user_dn);
						
						if(dnrec == null) {
							usertype = UserType.UNREGISTERED;
						} else {
							//check for disabled contact
							ContactModel cmodel = new ContactModel(guest_context);
							contact = cmodel.get(dnrec.contact_id);
							if (contact.isDisabled()) {
								log.info("The DN found in \"dn\" table but is mapped to disabled contact. Set isDisabled to true.");
								usertype = UserType.DISABLED;
							} else {
								initAction(dnrec);
								usertype = UserType.USER;
							}
						}
					} catch (SQLException e) {
						throw new AuthorizationException("Authorization check failed due to " + e.getMessage());
					}
				}
			}
			
		} else {
			//user is mostlikely accessing via http
			usertype = UserType.GUEST;
			try {
				loadActions(0);//load guest actions
			} catch (SQLException e) {
				throw new AuthorizationException("Authorization check failed due to " + e.getMessage());
			}
		}
	}

	private void debugAuthOverride(HttpServletRequest request) {

		try {
			InetAddress addr = InetAddress.getLocalHost();
	        String hostname = addr.getHostName();
			if(hostname.equals("t520")) {
				if(request.isSecure()) {
					request.setAttribute("SSL_CLIENT_VERIFY", "SUCCESS");
			
					//user_dn = null; user_cn = null;//browser didn't give us any dn
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";	
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Kyle A. Gross 453426";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Horst Severini 926890";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511";
					request.setAttribute("SSL_CLIENT_S_DN", "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343");
					request.setAttribute("SSL_CLIENT_I_DN_CN", StaticConfig.getDOECN());
				} else {
					request.setAttribute("SSL_CLIENT_VERIFY", null);
				}
				log.debug("Using debuggin credential");
			}
		} catch (UnknownHostException e) {
			log.error("Couldn't figure out debug user hostname",e);
		} 
	}
	
	//return false if contact is disabled
	private boolean initAction(DNRecord certdn) throws SQLException
	{
		DNAuthorizationTypeModel dnauthtypemodel = new DNAuthorizationTypeModel(guest_context);
		Collection<Integer> auth_type_ids = dnauthtypemodel.getAuthorizationTypesByDNID(certdn.id);
		for (Integer auth_type_id : auth_type_ids) {
			loadActions(auth_type_id);
		}
		
		return true;
	}
	
	private void loadActions(int auth_type_id) throws SQLException {
		AuthorizationTypeActionModel authactionmodel = new AuthorizationTypeActionModel(guest_context);
		ActionModel actionmodel = new ActionModel(guest_context);
		AuthorizationTypeModel authtypemodel = new AuthorizationTypeModel(guest_context); 
		AuthorizationTypeRecord authrec = authtypemodel.get(auth_type_id);
		auth_types.add(authrec.name);
		Collection<Integer> aids = authactionmodel.getActionByAuthTypeID(auth_type_id);
		for (Integer aid : aids) {
			actions.add(actionmodel.get(aid).name);
		}
	}
}
