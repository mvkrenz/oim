package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.SmallTableModelBase;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

public class RestServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(RestServlet.class);  
    
	static enum Status {OK, FAILED, PENDING};
    class Reply {
    	Status status = Status.OK;
    	String detail = "Nothing to report";
    	JSONObject params = new JSONObject();
    	
    	void out(HttpServletResponse response) throws IOException {
    		response.setContentType("application/json");
    		PrintWriter out = response.getWriter();
    		params.put("status", status.toString());
    		params.put("detail", detail);
    		out.write(params.toString());
    		
    		/*
    		//set httpresponse code
    		switch(status) {
    		case OK: 
    		case PENDING:
    			response.setStatus(HttpServletResponse.SC_OK);
    			break;
    		case FAILED:
    			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    			break;
    		}
    		*/
    	}
    }
    
    @SuppressWarnings("serial")
	class RestException extends Exception {
    	public RestException(String message) {
    		super(message);
    	}
    	public RestException(String message, Exception e) {
    		super(message, e);
    	}
    }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Reply reply = new Reply();
		
		try {
			String action = request.getParameter("action");
			if(action.equals("host_certs_request")) {
				doHostCertsRequest(request, reply);
			} else if(action.equals("host_certs_renew")) {
				doHostCertsRenew(request, reply);
			} else if(action.equals("host_certs_retrieve")) {
				doHostCertsRetrieve(request, reply);
			} else if(action.equals("host_certs_approve")) {
				doHostCertsApprove(request, reply);
			} else if(action.equals("host_certs_reject")) {
				doHostCertsReject(request, reply);
			} else if(action.equals("host_certs_cancel")) {
				doHostCertsCancel(request, reply);
			} else if(action.equals("host_certs_revoke")) {
				doHostCertsRevoke(request, reply);
			} else if(action.equals("host_certs_issue")) {
				doHostCertsIssue(request, reply);
			} else if(action.equals("reset_daily_quota")) {
				doResetDailyQuota(request, reply);
			} else if(action.equals("reset_yearly_quota")) {
				doResetYearlyQuota(request, reply);
			} else if(action.equals("find_expired_cert_request")) {
				doFindExpiredCertificateRequests(request, reply);
			} else {
				reply.status = Status.FAILED;
				reply.detail = "No such action";
			}
		
		} catch (RestException e) {
			reply.status = Status.FAILED;
			reply.detail = e.getMessage();
			if(e.getCause() != null) {
				reply.detail += " -- " + e.getCause().getMessage();
			}
		} catch(AuthorizationException e) {
			reply.status = Status.FAILED;
			reply.detail = e.getMessage();	
			if(e.getCause() != null) {
				reply.detail += " -- " + e.getCause().getMessage();
			}
		} catch(Exception e) {
			reply.status = Status.FAILED;
			reply.detail = e.getMessage();
			if(e.getCause() != null) {
				reply.detail += " -- " + e.getCause().getMessage();
			}
		}
		reply.out(response);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException  
	{
		Reply reply = new Reply();
		try {
			String action = request.getParameter("action");
			if(action.equals("quota_info")) {
				doQuotaInfo(request, reply);
			} else if(action.equals("user_info")) {
				doUserInfo(request, reply);
			} else {
				reply.status = Status.FAILED;
				reply.detail = "No such action";
			}
		} catch(AuthorizationException e) {
			reply.status = Status.FAILED;
			reply.detail = e.toString();	
		} catch(Exception e) {
			reply.status = Status.FAILED;
			reply.detail = e.toString();
			if(e.getMessage() != null) reply.detail += " -- " + e.getMessage();	
		}
		reply.out(response);
	}
 
	private void doHostCertsRequest(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		
		String[] dirty_csrs = request.getParameterValues("csrs");
		if(dirty_csrs == null) {
			throw new RestException("csrs parameter is not set.");
		}
		
		String name, email, phone;
		if(auth.isUser()) {
			ContactRecord user = auth.getContact();
			name = user.name;
			email = user.primary_email;
			phone = user.primary_phone;
			
			context.setComment("OIM authenticated user; " + name + " submitted host certificatates request.");
		} else if(auth.isUnregistered()) {
			throw new RestException("Accessed via https using unregistered user certificate :" + auth.getUserDN());
		} else if(auth.isDisabled()) {
			throw new RestException("Accessed via https using disabled user certificate: "+ auth.getUserDN());
		} else if(auth.isSecure()) {
			throw new RestException("Accessed via https without a user certificate (please use http for guest access)");
		} else {
			//must be a guest then.. we need name/address info.			
			name = request.getParameter("name");
			email = request.getParameter("email");
			phone = request.getParameter("phone");
			
			//TODO - validate
			if(name == null || email == null || phone == null) {
				throw new RestException("Please provide name, email, phone in order to create GOC ticket.");
			}
			context.setComment("Guest user; " + name + " submitted host certificatates request.");
		}
		
		//user may provide vo context
		Integer approver_vo_id = null;
		String voname = request.getParameter("vo");
		try {
			if(voname != null) {
				VOModel vmodel = new VOModel(context);
				VORecord vo = vmodel.getByName(voname);
				if(vo == null) {
					log.warn("Failed to find user specified VO : " + voname + " .. ignoring for now - will throw later if VO context is needed");
				} else {
					approver_vo_id = vo.id;
				}
			}
		} catch (SQLException e) {
			throw new RestException("Failed to lookup voname: " + voname, e);
		}
		
		CertificateRequestHostModel certmodel = new CertificateRequestHostModel(context);
		
		//lookup gridadmins for specified csr (in specified vo context)
		try {
			ArrayList<ContactRecord> gas = certmodel.findGridAdmin(dirty_csrs, approver_vo_id);
			if(gas.isEmpty()) {
				throw new RestException("No GridAdmins for specified CSRs/VO");
			}
		} catch (CertificateRequestException e) {
			throw new RestException("Failed to find GridAdmins for specified CSRs/VO", e);
		}
		String [] csrs = dirty_csrs;//no longer dirty at this point
		
		//optional parameters
		String request_comment = request.getParameter("request_comment");
		String[] request_ccs = request.getParameterValues("request_ccs");
		
		CertificateRequestHostRecord rec;
		try {
			if(auth.isUser()) {
				rec = certmodel.requestAsUser(csrs,  auth.getContact(), request_comment, request_ccs, approver_vo_id);
			} else {
				rec = certmodel.requestAsGuest(csrs, name, email, phone, request_comment, request_ccs, approver_vo_id);
			}
			if(rec == null) {
				throw new RestException("Failed to make request");
			}
			log.debug("success");
			reply.params.put("gocticket_url", StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id);
			reply.params.put("host_request_id", rec.id.toString());
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	
	private void doHostCertsRenew(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		//Authorization auth = context.getAuthorization();
		
		throw new RestException("No yet implemented");
	}
	
	private void doHostCertsRetrieve(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		//Authorization auth = context.getAuthorization();
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			
			if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
				//convert string array to jsonarray and send to user
				JSONArray ja = new JSONArray();
				StringArray pkcs7s = new StringArray(rec.cert_pkcs7);
				for(int i = 0;i < pkcs7s.length(); ++i) {
					ja.put(i, pkcs7s.get(i));
				}
				reply.params.put("pkcs7s", ja);
			} else if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
				//TODO - issue thread should somehow report issue status instead.
				//count number of certificate issued so far
				StringArray pkcs7s = new StringArray(rec.cert_pkcs7);
				int issued = 0;
				JSONArray certstatus = new JSONArray();
				for(int i = 0;i < pkcs7s.length(); ++i) {
					String pkcs7 = pkcs7s.get(i);
					if(pkcs7 != null) {
						issued++;
						certstatus.put(i, "ISSUED");
					} else {
						certstatus.put(i, "ISSUING");
					}
				}
				reply.params.put("cert_status", certstatus);
				reply.status = Status.PENDING;
				reply.detail = issued + " of " + pkcs7s.length() + " certificates has been issued";
			} else {
				reply.status = Status.FAILED;
				reply.detail = "Can't retrieve certificates on request that are not in ISSUED or ISSUING status. Current request status is " + rec.status + ".";
				if(rec.status_note != null) {
					reply.detail += " Last note: " + rec.status_note;
				}
				reply.params.put("request_status", rec.status.toString());
			}
			
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		}
	}
	
	private void doHostCertsApprove(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		//Authorization auth = context.getAuthorization();
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
	
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			if(model.canApprove(rec)) {
				model.approve(rec);
			} else {
				throw new AuthorizationException("You can't approve this request");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	
	private void doHostCertsReject(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
	
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			if(model.canReject(rec)) {
				model.reject(rec);
			} else {
				throw new AuthorizationException("You can't reject this request");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}

	private void doHostCertsCancel(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
	
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			if(model.canCancel(rec)) {
				model.cancel(rec);
			} else {
				throw new AuthorizationException("You can't cancel this request");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	
	private void doHostCertsRevoke(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
	
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			if(model.canRevoke(rec)) {
				model.revoke(rec);
			} else {
				throw new AuthorizationException("You can't revoke this request");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	
	//start issuing cert.. will not block
	private void doHostCertsIssue(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
	
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			if(model.canIssue(rec)) {
				model.startissue(rec);
			} else {
				throw new AuthorizationException("You can't issue this request");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	
	private void doQuotaInfo(HttpServletRequest request, Reply reply) throws AuthorizationException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			throw new AuthorizationException("You can't access this interface from there");
		}
		ConfigModel config = new ConfigModel(context);
		reply.params.put("global_usercert_year_count", config.QuotaGlobalUserCertYearCount.getInteger());
		reply.params.put("global_usercert_year_max", config.QuotaGlobalUserCertYearMax.getInteger());
		reply.params.put("global_hostcert_year_count", config.QuotaGlobalHostCertYearCount.getInteger());
		reply.params.put("global_hostcert_year_max", config.QuotaGlobalHostCertYearMax.getInteger());
		
		reply.params.put("quota_hostcert_year_max", config.QuotaUserHostYearMax.getInteger());
		reply.params.put("quota_hostcert_day_max", config.QuotaUserHostDayMax.getInteger());
		reply.params.put("quota_usercert_year_max", config.QuotaUserCertYearMax.getInteger());
	}
	
	private void doUserInfo(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isUser()) {
			throw new AuthorizationException("This API is only for registered users.");
		}
		
		ConfigModel config = new ConfigModel(context);
		reply.params.put("global_usercert_year_count", config.QuotaGlobalUserCertYearCount.getInteger());
		reply.params.put("global_usercert_year_max", config.QuotaGlobalUserCertYearMax.getInteger());
		reply.params.put("global_hostcert_year_count", config.QuotaGlobalHostCertYearCount.getInteger());
		reply.params.put("global_hostcert_year_max", config.QuotaGlobalHostCertYearMax.getInteger());
	
		reply.params.put("quota_hostcert_year_max", config.QuotaUserHostYearMax.getInteger());
		reply.params.put("quota_hostcert_day_max", config.QuotaUserHostDayMax.getInteger());
		reply.params.put("quota_usercert_year_max", config.QuotaUserCertYearMax.getInteger());
		
		ContactRecord user = auth.getContact();
		reply.params.put("count_hostcert_day", user.count_hostcert_day);
		reply.params.put("count_hostcert_year", user.count_hostcert_year);
		reply.params.put("count_usercert_year", user.count_usercert_year);
		
		reply.detail = "User quota details for "+ auth.getUserDN();
		
		//load domains that user can approve certificates for
		try {
			GridAdminModel gmodel = new GridAdminModel(context);
			ArrayList<GridAdminRecord> recs = gmodel.getGridAdminsByContactID(user.id);
			JSONArray domains = new JSONArray();
			for(GridAdminRecord rec : recs) {
				domains.put(rec.domain);
			}
			reply.params.put("gridadmin_domains", domains);
		} catch (SQLException e) {
			throw new RestException("Failed to load gridadmin list", e);
		}
	}
	
	private void doResetDailyQuota(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			throw new AuthorizationException("You can't access this interface from there");
		}
		
		//reset user counter
		ContactModel model = new ContactModel(context);
		try {
			model.resetCertsDailyCount();
			SmallTableModelBase.emptyAllCache();
		} catch (SQLException e) {
			throw new RestException("SQLException while resetting user daily count", e);
		}
	}
	
	private void doResetYearlyQuota(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			throw new AuthorizationException("You can't access this interface from there");
		}
		
		//reset user counter
		ContactModel model = new ContactModel(context);
		try {
			model.resetCertsYearlyCount();
			SmallTableModelBase.emptyAllCache();
		} catch (SQLException e) {
			throw new RestException("SQLException while resetting user yearly count", e);
		}
		
		//reset global counter
		ConfigModel config = new ConfigModel(context);
		try {
			config.QuotaGlobalHostCertYearCount.set("0");
			config.QuotaGlobalUserCertYearCount.set("0");
		} catch (SQLException e) {
			throw new RestException("SQLException while resetting global yearly count", e);
		}
	}

	private void doFindExpiredCertificateRequests(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			throw new AuthorizationException("You can't access this interface from there");
		}
			
		//process expired user certificates?
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		try {
			model.processExpired();
		} catch (SQLException e) {
			throw new RestException("SQLException while processing expired certificates", e);
		}
		
		//TODO - process expired host certificates?
	}
}