package edu.iu.grid.oim.lib;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.UserContext;
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
	
	private UserContext guest_context; //used to access DB befort auth/context is fully constructed (avoid chicken/egg)
	
	//internal states
	private String user_dn = null;
	private String user_cn = null;
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
    	return contact;
    }
    
    
    //it really doesn't make sense that this belongs in Authorization, but since Authorization holds the Contact record..
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
	
	//used to create default Context
	public Authorization() {
		usertype = UserType.GUEST;
	}
	
	//pull user_dn from Apache's SSL_CLIENT_S_DN
	public Authorization(HttpServletRequest request) throws AuthorizationException 
	{		
		guest_context = UserContext.getGuestContext();
		usertype = UserType.GUEST;
		
		String localname = request.getLocalName();
		String remoteaddr = request.getRemoteAddr();
		if(localname.equals("localhost") || localname.equals("localhost.localdomain") || localname.equals("0.0.0.0") 
				|| remoteaddr.equals("192.168.1.75") //t520
				|| remoteaddr.equals("192.168.1.77") //ubuntu-t520
				) {
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
			
			if(user_dn == null || user_cn == null) {
				log.info("SSL_CLIENT_S_DN or SSL_CLIENT_I_DN_CN is not set. Logging in as guest.");
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
							loadGuestAction();
						} else {
							//check for disabled contact
							ContactModel cmodel = new ContactModel(guest_context);
							contact = cmodel.get(dnrec.contact_id);
							if (contact.isDisabled()) {
								log.info("The DN found in \"dn\" table but is mapped to disabled contact. Set isDisabled to true.");
								usertype = UserType.DISABLED;
								loadGuestAction(); //disabled user can still access guest content
							} else {
								usertype = UserType.USER;
								initAction(dnrec);
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
			loadGuestAction();
		}
	}
	
	private void loadGuestAction() throws AuthorizationException {
		try {
			loadActions(0);//load guest actions
		} catch (SQLException e) {
			throw new AuthorizationException("Authorization check failed due to " + e.getMessage());
		}
	}

	private void debugAuthOverride(HttpServletRequest request) {

		try {
			InetAddress addr = InetAddress.getLocalHost();
	        String hostname = addr.getHostName();
			if(hostname.equals("t520") || hostname.equals("d830")) {
				if(request.isSecure()) {
					request.setAttribute("SSL_CLIENT_VERIFY", "SUCCESS");
			
					//user_dn = null; user_cn = null;//browser didn't give us any dn
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343";	
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Kyle A. Gross 453426";
					//user_dn = "/DC=org/DC=doegrids/OU=People/CN=Horst Severini 926890";
					//request.setAttribute("SSL_CLIENT_S_DN", "/DC=org/DC=doegrids/OU=People/CN=christopher pipes 556895"); //disabled
					request.setAttribute("SSL_CLIENT_S_DN", "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi 461343");
					//request.setAttribute("SSL_CLIENT_S_DN", "/DC=org/DC=doegrids/OU=People/CN=Soichi Hayashi new2");
					//request.setAttribute("SSL_CLIENT_S_DN", "/DC=org/DC=doegrids/OU=People/CN=Alain Roy 424511");
					request.setAttribute("SSL_CLIENT_I_DN_CN", "Test CA");
					log.debug("Using debuggin credential");
				} else {
					request.setAttribute("SSL_CLIENT_VERIFY", null);
				}
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
