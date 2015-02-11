package edu.iu.grid.oim.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.codec.binary.Base64;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.LogModel;
import edu.iu.grid.oim.model.db.SmallTableModelBase;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class RestServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(RestServlet.class);  
    
    static String GOC_TICKET_MESSAGE = " -- GOC alert will be sent to GOC infrastructure team about this issue. Meanwhile, feel free to open a GOC ticket at https://ticket.grid.iu.edu";
    
	static enum Status {OK, FAILED, PENDING};
    class Reply {
    	Status status = Status.OK;
    	String detail = "Nothing to report";
    	JSONObject params = new JSONObject();
    	
    	void out(HttpServletResponse response) throws IOException {
    		response.setContentType("application/json");
    		response.setStatus(200); //we always return 200 - let user parse the status code instead
    		PrintWriter out = response.getWriter();
    		params.put("status", status.toString());
    		params.put("detail", detail);
    		out.write(params.toString());
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
			
			//host certificate api
			if(action.equals("host_certs_request")) {
				doHostCertsRequest(request, reply);
			//} else if(action.equals("host_certs_renew")) {
			//	doHostCertsRenew(request, reply); //Tony says we don't need host cert renew
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
			} 
			
			//user certificate api
			else if(action.equals("user_cert_request")) {
				doUserCertRequest(request, reply);
			} else if(action.equals("user_cert_renew")) {
				doUserCertRenew(request, reply);
			} else if(action.equals("user_cert_retrieve")) {
				doUserCertRetrieve(request, reply);
			} else if(action.equals("user_cert_approve")) {
				doUserCertApprove(request, reply);
			} else if(action.equals("user_cert_reject")) {
				doUserCertReject(request, reply);
			} else if(action.equals("user_cert_cancel")) {
				doUserCertCancel(request, reply);
			} else if(action.equals("user_cert_revoke")) {
				doUserCertRevoke(request, reply);
			} else if(action.equals("user_cert_issue")) {
				doUserCertIssue(request, reply);
			} 
			
			//misc
			else if(action.equals("reset_daily_quota")) {
				doResetDailyQuota(request, reply);
			} else if(action.equals("reset_yearly_quota")) {
				doResetYearlyQuota(request, reply);
			} else if(action.equals("find_expired_cert_request")) {
				doFindExpiredCertificateRequests(request, reply);
			} else if(action.equals("notify_expiring_cert_request")) {
				doNotifyExpiringCertificateRequest(request, reply);
			} else {
				reply.status = Status.FAILED;
				reply.detail = "No such action";
			}
		
		} catch (RestException e) {
			reply.status = Status.FAILED;
			reply.detail = e.getMessage();
			if(e.getCause() != null) {
				reply.detail += " -- " + e.getCause().getMessage() + GOC_TICKET_MESSAGE;
			}
		} catch(AuthorizationException e) {
			//nothing unclear about auth error.
			reply.status = Status.FAILED;
			reply.detail = e.toString();	
		} catch(Exception e) {
			reply.status = Status.FAILED;
			reply.detail = e.toString();
			if(e.getCause() != null) {
				reply.detail += " -- " + e.getCause().getMessage() + GOC_TICKET_MESSAGE;
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
				/*
			} else if(action.equals("hcvoid")) {
				doHCVOID(request, reply);
				*/
			} else if(action.equals("clear_cache")) {
				SmallTableModelBase.emptyAllCache();
				reply.status = Status.OK;
				reply.detail = "Cleared Cache";
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
			if(e.getMessage() != null) reply.detail += " -- " + e.getMessage() + GOC_TICKET_MESSAGE;	
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
					log.info("Failed to find user specified VO : " + voname + " .. ignoring for now - will throw later if VO context is needed");
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
	    	CertificateRequestHostRecord temp_rec = new CertificateRequestHostRecord();
	    	StringArray dirty_csrs_ar = new StringArray(dirty_csrs);
	    	temp_rec.csrs = dirty_csrs_ar.toXML();
	    	temp_rec.approver_vo_id = approver_vo_id;
			ArrayList<ContactRecord> gas = certmodel.findGridAdmin(temp_rec);
			if(gas.isEmpty()) {
				throw new RestException("No GridAdmins for specified CSRs/VO");
			}
			
			//reset it back to our vo_id now that we called findGridAdmin
			approver_vo_id = temp_rec.approver_vo_id;
		} catch (CertificateRequestException e) {
			throw new RestException("Failed to find GridAdmins for specified CSRs/VO", e);
		}
		ArrayList<String> csrs = new ArrayList<String>();
		for(String csr : dirty_csrs) {
			csrs.add(csr);//no longer dirty at this point
		}
		
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
				//pass pkcs7s
				JSONArray ja = new JSONArray();
				StringArray pkcs7s = new StringArray(rec.cert_pkcs7);
				for(int i = 0;i < pkcs7s.length(); ++i) {
					ja.put(i, pkcs7s.get(i));
				}
				reply.params.put("pkcs7s", ja);
				
				//per conversation with Von on 2/1/2012 about 1.1 freeze, I am removing certificates and intermediates out until 
				//CLI will do key-based access to the output parameter (not index)
				/*
				//pass certificates (pem?)
				JSONArray certs_ja = new JSONArray();
				StringArray certs = new StringArray(rec.cert_certificate);
				for(int i = 0;i < certs.length(); ++i) {
					certs_ja.put(i, certs.get(i));
				}
				reply.params.put("certificates", certs_ja);
				
				//pass intermediates (pem?)
				JSONArray ints_ja = new JSONArray();
				StringArray intermediates = new StringArray(rec.cert_intermediate);
				for(int i = 0;i < intermediates.length(); ++i) {
					ints_ja.put(i, intermediates.get(i));
				}
				reply.params.put("intermediates", ints_ja);		
				*/
			} else if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
				//TODO - issue thread should somehow report issue status instead.
				//TODO - this algorithm probably won't work if the certs are renewed - since we currently don't clear certificate before re-issuing
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
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = Integer.parseInt(dirty_host_request_id);
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		
		//set comment
		String request_comment = request.getParameter("request_comment");
		if(request_comment == null) {
			request_comment = "Approved via command line";
		}
		context.setComment(request_comment);
	
		try {
			CertificateRequestHostRecord rec = model.get(host_request_id);
			if(rec == null) {
				throw new RestException("No such host certificate request ID");
			}
			if(model.canApprove(rec)) {
				model.approve(rec);
			} else {
				throw new AuthorizationException("Your request has been received and must be approved. Please watch your email for a GOC ticket notification with further instructions.");
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
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		
		String dirty_host_request_id = request.getParameter("host_request_id");
		Integer host_request_id = null;
		if(dirty_host_request_id != null) {
			host_request_id = Integer.parseInt(dirty_host_request_id);
		}

		String dirty_serial_id = request.getParameter("serial_id");
		String serial_id = null;
		Integer idx = null;
		if(dirty_serial_id != null) {
			serial_id = dirty_serial_id.replaceAll("/[^a-zA-Z0-9: ]/", "");
			serial_id = model.normalizeSerialID(serial_id);
		}
		
		//set comment
		String request_comment = request.getParameter("request_comment");
		if(request_comment == null) {
			request_comment = "Revocation requested via command line";
		}
		context.setComment(request_comment);
	
		try {
			//lookup request record either by request_id or serial_id
			CertificateRequestHostRecord rec = null;
			if(host_request_id != null) {
				rec = model.get(host_request_id);
				if(rec == null) {
					throw new RestException("No such host certificate request ID");
				}
				if(model.canRevoke(rec)) {
					model.revoke(rec);
				} else {
					throw new AuthorizationException("You are not authorized to revoke this request");
				}
			} else if(serial_id != null) {
				rec = model.getBySerialID(serial_id);
				if(rec == null) {
					throw new RestException("No such host certificate serial ID");
				}				
				
				//need to find the certificate idx..
				String[] serial_ids = rec.getSerialIDs();
				for(int i = 0;i < serial_ids.length;++i) {
					String id = serial_ids[i];
					if(id.equals(serial_id)) {
						idx = i;
						break;
					}
				}
				if(idx == null) {
					throw new RestException("(This should never happen) Couldn't find serial id: "+serial_id);
				}
				
				//check certificate status
				String[] statuses = rec.getStatuses();
				if(!statuses[idx].equals(CertificateRequestStatus.ISSUED)) {
					throw new RestException("Certificate status is currently"+statuses[idx]+" and can not be revoked");
				}
				
				//finally, revoke it
				if(model.canRevoke(rec)) {
					model.revoke(rec, idx);
				} else {
					throw new AuthorizationException("You are not authorized to revoke this request");
				}
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
	
	private void doUserCertRequest(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {	
		//TODO - user cert request with CSR is yet to be implemented
	}
	private void doUserCertRenew(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = null;
		if(dirty_user_request_id != null) {
			user_request_id = Integer.parseInt(dirty_user_request_id);
		}

		String dirty_serial_id = request.getParameter("serial_id");
		String serial_id = null;
		if(dirty_serial_id != null) {
			serial_id = dirty_serial_id.replaceAll("/[^A-Z0-9 ]/", "");
		}
		
		String dirty_password = request.getParameter("password");
		String password = null;
		if(dirty_password != null) {
			password = dirty_password.replaceAll("[^\\x00-\\x7F]", "");//replace all non-ascii
			PKIPassStrengthValidator validator = new PKIPassStrengthValidator();
			if(!validator.isValid(password)) {
				throw new RestException("Password is too weak.");
			}
		} else {
			throw new RestException("Password parameter is missing.");
		}
		
		//set comment
		String request_comment = request.getParameter("request_comment");
		if(request_comment == null) {
			request_comment = "Renewal Request via command line";
		}
		context.setComment(request_comment);
		
		try {
			//lookup request record either by request_id or serial_id
			CertificateRequestUserRecord rec = null;
			if(user_request_id != null) {
				rec = model.get(user_request_id);
			} else if(serial_id != null) {
				rec = model.getBySerialID(serial_id);
			}
			if(rec == null) {
				//see if the serial ID was previously renewed already
				if(serial_id != null && model.findByOldSerialID(serial_id)) {
					throw new RestException("The certificate with serial ID: "+serial_id+" has already been renewed");
				}
				throw new RestException("No such user certificate request ID or serial ID");
			}
			
			//load log
			ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs = model.getLogs(CertificateRequestUserModel.class, rec.id);		
			AuthorizationCriterias criterias = model.canRenew(rec, logs);
			if(criterias.passAll()) {
				model.renew(rec, password);
				reply.params.put("request_id", rec.id);
			} else {
				throw new AuthorizationException("You are not authorized to renew this certificate, or condition of the user certificate currently does not allow you to renew this certificate.");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	private void doUserCertRetrieve(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = Integer.parseInt(dirty_user_request_id);
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		try {
			CertificateRequestUserRecord rec = model.get(user_request_id);
			if(rec == null) {
				throw new RestException("No such user certificate request ID");
			}
			
			if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
				//convert string array to jsonarray and send to user
				reply.params.put("pkcs7", rec.cert_pkcs7);
				reply.params.put("certificate", rec.cert_certificate);
				reply.params.put("intermediate", rec.cert_intermediate);
				if(model.getPrivateKey(rec.id) != null) {
					KeyStore p12 = model.getPkcs12(rec);
					if(p12 == null) {
						throw new RestException("Failed to create pkcs12 for download");
					} else {
						try {
							String password = model.getPassword(rec.id);
							ByteArrayOutputStream os = new ByteArrayOutputStream();
							p12.store(os, password.toCharArray());
							
							//base64 encode and set it to pkcs12 param
							byte[] encoded = Base64.encodeBase64(os.toByteArray());
							reply.params.put("pkcs12", new String(encoded));
						} catch (KeyStoreException e) {
							throw new RestException("KeyStoreException while outputing pkcs12", e);
						} catch (NoSuchAlgorithmException e) {
							throw new RestException("NoSuchAlgorithmException while outputing pkcs12", e);
						} catch (CertificateException e) {
							throw new RestException("CertificateException while outputing pkcs12", e);
						} catch (IOException e) {
							throw new RestException("IOException while outputing pkcs12", e);
						}
					}
				}
			} else if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
				reply.status = Status.PENDING;
			} else {
				reply.status = Status.FAILED;
				reply.detail = "Can't retrieve certificate on request that are not in ISSUED or ISSUING status. Current request status is " + rec.status + ".";
				reply.params.put("request_status", rec.status.toString());
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		}
	}
	private void doUserCertApprove(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = Integer.parseInt(dirty_user_request_id);
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		
		//set comment
		String request_comment = request.getParameter("request_comment");
		if(request_comment == null) {
			request_comment = "Approved via command line";
		}
		context.setComment(request_comment);
		
		try {
			CertificateRequestUserRecord rec = model.get(user_request_id);
			if(rec == null) {
				throw new RestException("No such user certificate request ID");
			}
			if(model.canApprove(rec)) {
				model.approve(rec);
			} else {
				throw new AuthorizationException("You are not authorized to approve this certificate, or condition of the user certificate currently does not allow you to approve this certificate.");
			}
		} catch (SQLException e) {
			throw new RestException("SQLException while making request", e);
		} catch (CertificateRequestException e) {
			throw new RestException("CertificateRequestException while making request", e);
		}
	}
	private void doUserCertReject(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = Integer.parseInt(dirty_user_request_id);
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		try {
			CertificateRequestUserRecord rec = model.get(user_request_id);
			if(rec == null) {
				throw new RestException("No such user certificate request ID");
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
	private void doUserCertCancel(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = Integer.parseInt(dirty_user_request_id);
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		try {
			CertificateRequestUserRecord rec = model.get(user_request_id);
			if(rec == null) {
				throw new RestException("No such user certificate request ID");
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
	private void doUserCertRevoke(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = null;
		if(dirty_user_request_id != null) {
			user_request_id = Integer.parseInt(dirty_user_request_id);
		}

		String dirty_serial_id = request.getParameter("serial_id");
		String serial_id = null;
		if(dirty_serial_id != null) {
			serial_id = dirty_serial_id.replaceAll("/[^a-zA-Z0-9: ]/", "");
		}
		
		//set comment
		String request_comment = request.getParameter("request_comment");
		if(request_comment == null) {
			request_comment = "Revocation requested via command line";
		}
		context.setComment(request_comment);
		
		try {
			//lookup request record either by request_id or serial_id
			CertificateRequestUserRecord rec = null;
			if(user_request_id != null) {
				rec = model.get(user_request_id);
			} else if(serial_id != null) {
				rec = model.getBySerialID(serial_id);
			}
			if(rec == null) {
				throw new RestException("No such user certificate request ID or serial ID");
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
	private void doUserCertIssue(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		String dirty_user_request_id = request.getParameter("user_request_id");
		Integer user_request_id = Integer.parseInt(dirty_user_request_id);
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		try {
			CertificateRequestUserRecord rec = model.get(user_request_id);
			if(rec == null) {
				throw new RestException("No such user certificate request ID");
			}
			if(model.canIssue(rec)) {
				model.startissue(rec, null);//command line doesn't provide any password
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
	
	//should call once an year
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

	//should call every day
	private void doFindExpiredCertificateRequests(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			throw new AuthorizationException("You can't access this interface from there");
		}
			
		CertificateRequestUserModel umodel = new CertificateRequestUserModel(context);
		try {
			umodel.processCertificateExpired();
			umodel.processStatusExpired();
		} catch (SQLException e) {
			throw new RestException("SQLException while processing expired user certificates", e);
		}

		CertificateRequestHostModel hmodel = new CertificateRequestHostModel(context);
		try {
			hmodel.processCertificateExpired();
			hmodel.processStatusExpired();
		} catch (SQLException e) {
			throw new RestException("SQLException while processing expired host certificates", e);
		}
	}
	
	//should call once a week
	private void doNotifyExpiringCertificateRequest(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
		Authorization auth = context.getAuthorization();
		if(!auth.isLocal()) {
			throw new AuthorizationException("You can't access this interface from there");
		}
		
		CertificateRequestUserModel umodel = new CertificateRequestUserModel(context);
		try {
			umodel.notifyExpiringIn(30);
		} catch (SQLException e) {
			throw new RestException("SQLException while processing expired user certificates", e);
		}
		
		CertificateRequestHostModel hmodel = new CertificateRequestHostModel(context);
		try {
			hmodel.notifyExpiringIn(30);
		} catch (SQLException e) {
			throw new RestException("SQLException while processing expired user certificates", e);
		}
	}

	/*
	//this is used just once to populate missing VO ID on host certificate requests
	private void doHCVOID(HttpServletRequest request, Reply reply) throws AuthorizationException, RestException {
		UserContext context = new UserContext(request);	
	
		//pull all records that has no expiration dates set
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		try  {
			StringBuffer queries = new StringBuffer();
			for(CertificateRequestHostRecord rec : model.findNullVO()) {
				//String[] cns = rec.getCNs();
				try {
					System.out.println("processing " + rec.id);
					model.findGridAdmin(rec);
					if(rec.approver_vo_id == null) {
						System.out.println("\tCouldn't figure out approver_vo_id");
					} else {
						//CertificateRequestModelBase base = model;
						//base.update(model.get(rec.id), rec);
						queries.append("UPDATE certificate_request_host SET approver_vo_id = "+ rec.approver_vo_id + " WHERE id = " + rec.id + " and approver_vo_id is NULL limit 1;\n");
					}
				} catch(CertificateRequestException e) {
					System.out.println("\tFailed to reset approver_vo_id");
					System.out.println("\t"+e.toString());
				}
			}
			reply.detail = queries.toString();
		} catch (SQLException e) {
			throw new RestException("SQLException while running doHostCertExSQL", e);
		}
	}
	*/
}