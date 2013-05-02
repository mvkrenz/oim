package edu.iu.grid.oim.model.db;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.x500.X500Principal;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cms.CMSException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.BCrypt;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.GenerateCSR;
import edu.iu.grid.oim.model.cert.ICertificateSigner;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

public class CertificateRequestUserModel extends CertificateRequestModelBase<CertificateRequestUserRecord> {
    static Logger log = Logger.getLogger(CertificateRequestUserModel.class);  
 
    public CertificateRequestUserModel(UserContext _context) {
		super(_context, "certificate_request_user");
	}
    
	//determines if user should be able to view request details, logs, and download certificate (pkcs12 is session specific)
	public boolean canView(CertificateRequestUserRecord rec) {
		return true; //let's allow everyone to view.
	}
		
	//empty if not found
	public ArrayList<ContactRecord> findRAs(CertificateRequestUserRecord rec) throws SQLException {
		ArrayList<ContactRecord> ras = new ArrayList<ContactRecord>();
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs = model.getByVOID(rec.vo_id);
		for(VOContactRecord crec : crecs) {
			if(crec.contact_type_id.equals(11)) { //11 - ra
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				ras.add(contactrec);
			}
		}
		return ras;
	}
	
	//empty if not found
	public ArrayList<ContactRecord> findSponsors(CertificateRequestUserRecord rec) throws SQLException {
		ArrayList<ContactRecord> sponsors = new ArrayList<ContactRecord>();
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs = model.getByVOID(rec.vo_id);
		for(VOContactRecord crec : crecs) {
			if(crec.contact_type_id.equals(12) ) { //12 -- sponsor
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				sponsors.add(contactrec);
			}
		}
		return sponsors;
	}
	
	//true if user can override CN
	public boolean canOverrideCN(CertificateRequestUserRecord rec) {
		if(canApprove(rec) && rec.status.equals(CertificateRequestStatus.REQUESTED)) {
			return true;
		}
		return false;
	}
	
	//true if user can approve request
	public boolean canApprove(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REQUESTED)) {
			//RA doesn't *approve* REVOKACTION - RA just click on revoke button
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				//Is user RA agent for specified vo?
				VOContactModel model = new VOContactModel(context);
				//sContactModel cmodel = new ContactModel(context);
				ArrayList<VOContactRecord> crecs;
				try {
					crecs = model.getByVOID(rec.vo_id);
					for(VOContactRecord crec : crecs) {
						if(crec.contact_type_id.equals(11)) { //11 - RA
							if(crec.contact_id.equals(contact.id)) return true;
						}
					}
				} catch (SQLException e1) {
					log.error("Failed to lookup RA/sponsor information", e1);
				}			
			}
		}
		return false;
	}
	
	public boolean canReject(CertificateRequestUserRecord rec) {
		return canApprove(rec); //same rule as approval
	}	
	
	public boolean canCancel(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.APPROVED) || //if renew_requesterd > approved cert is canceled, it should really go back to "issued", but currently it doesn't.
			//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				//requester can cancel one's own request
				if(rec.requester_contact_id.equals(contact.id)) return true;
				
				//ra can cancel
				VOContactModel model = new VOContactModel(context);
				//ContactModel cmodel = new ContactModel(context);
				ArrayList<VOContactRecord> crecs;
				try {
					crecs = model.getByVOID(rec.vo_id);
					for(VOContactRecord crec : crecs) {
						if(crec.contact_type_id.equals(11)) { //11 - RA
							if(crec.contact_id.equals(contact.id)) return true;
						}
					}
				} catch (SQLException e1) {
					log.error("Failed to lookup RA/sponsor information", e1);
				}	
			}
		}
		return false;
	}
	
	public boolean canCancelWithPass(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {

			//anyone including guest can cancel guest submitted request with a valid pass
			if(rec.requester_passphrase != null) {
				return true;	
			}
			
		}
		return false;
	}
	
	public LogDetail getLastLog(String state, ArrayList<LogDetail> logs) {
		for(LogDetail log : logs) {
			if(log.status.equals(state)) {
				return log;
			}
		}
		return null;
	}
	
	public boolean canRenew(CertificateRequestUserRecord rec, ArrayList<LogDetail> logs) {
		if(!auth.isUser()) return false;	
		return canRenew(rec, logs, auth.getContact());
	}
	//can a user renew the certificate immediately?
	public boolean canRenew(CertificateRequestUserRecord rec, ArrayList<LogDetail> logs, ContactRecord contact) {
		if(!canView(rec)) return false;
		
		//only issued request can be renewed
		if(!rec.status.equals(CertificateRequestStatus.ISSUED)) return false;
		
		//original requester?
		if(!rec.requester_contact_id.equals(contact.id)) return false;

		//approved within 5 years?
		LogDetail last = getLastLog(CertificateRequestStatus.APPROVED, logs);
		if(last == null) return false; //never approved
		Calendar five_years_ago = Calendar.getInstance();
		five_years_ago.add(Calendar.YEAR, -5);
		if(last.time.before(five_years_ago.getTime())) return false;
		
		//will expire in less than 6 month?
		Calendar six_month_future = Calendar.getInstance();
		six_month_future.add(Calendar.MONTH, 6);
		if(rec.cert_notafter.after(six_month_future.getTime()))  {
			//nope... but, if it's in debug mode, let user(testers) renew it
			if(!StaticConfig.isDebug()) {
				return false;
			}
		}
		
		//email hasn't changed since issue?
		try {
			java.security.cert.Certificate[] chain = CertificateManager.parsePKCS7(rec.cert_pkcs7);
			X509Certificate c0 = (X509Certificate)chain[0];
			Collection<List<?>> list = c0.getSubjectAlternativeNames();
			Iterator<List<?>> it = list.iterator();
			List<?> first = it.next();
			String cert_email = (String)first.get(1);
			if(!cert_email.equals(contact.primary_email) && !cert_email.equals(contact.secondary_email)) {
				//doesn't match primary nor secondary email
				return false;
			}
		} catch (Exception e) {
			log.error("CertificateRequestUserModel::canRenw() :: Failed to parse pkcs7 to test email address (skipping this test). id:" + rec.id, e);
		}
	
		//all good
		return true;
	}
	
	/*
	public boolean canRequestRenew(CertificateRequestUserRecord rec, ArrayList<LogDetail> logs) {
		if(!canView(rec)) return false;
		
		if(canRenew(rec, logs)) return false;//if user can renew it immediately, then no need for request.
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser()) {
				return true;
			}
		}
		return false;
	}
	*/
	
	public boolean canReRequest(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
/*
		if(	rec.status.equals(CertificateRequestStatus.REJECTED) ||
			rec.status.equals(CertificateRequestStatus.CANCELED) ||
			rec.status.equals(CertificateRequestStatus.REVOKED) ||
			rec.status.equals(CertificateRequestStatus.EXPIRED)) {
			//requester can re-request
			if(auth.isUser() && auth.getContact().id.equals(rec.requester_contact_id)) {
				return true;
			}
		}		
		
		if (rec.status.equals(CertificateRequestStatus.EXPIRED) ) {
			//guest user needs to be able to re-request expired cert..
			return true;
		}
*/
		if(	rec.status.equals(CertificateRequestStatus.REJECTED) ||
				rec.status.equals(CertificateRequestStatus.CANCELED) ||
				rec.status.equals(CertificateRequestStatus.REVOKED) ||
				rec.status.equals(CertificateRequestStatus.EXPIRED)) {
			//anyone including guest can re-request if the status is one of avobe.
			//Previously, we only allowed guest to re-request if the certificate was expired.
			//however, sometime user expires *while* going through the request process.
			//in that case, RA will cancel the cert, and guest user can re-request
			//TODO Once everyone transition to DigiCert, I believe we should only allow guest user to re-request if the request is in EXPIRED state.
			
			if(auth.isUser()) {
				//user can re-request his own cert .
				if(auth.getContact().id.equals(rec.requester_contact_id)) {
					return true;
				}
				//can't re-request someone else's cert
				return false;
			} else {
				//allow guest to renew any cert, provided that RA/sponsor will vet.
				return true;
			}
		}	
		
		return false;
	}
	
	public boolean canRequestRevoke(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser() && canRevoke(rec)) {
				//if user can directly revoke it, no need to request it
				return false;
			}
			
			//all else, allow
			return true;
		}
		return false;
	}
	
	public boolean canRevoke(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			
			if(auth.isUser()) {
				
				//requester oneself can revoke it
				ContactRecord contact = auth.getContact();
				if(rec.requester_contact_id.equals(contact.id)) return true;
				
				//ra can revoke it
				VOContactModel model = new VOContactModel(context);
				//ContactModel cmodel = new ContactModel(context);
				ArrayList<VOContactRecord> crecs;
				try {
					crecs = model.getByVOID(rec.vo_id);
					for(VOContactRecord crec : crecs) {
						//ContactRecord contactrec = cmodel.get(crec.contact_id);
						if(crec.contact_type_id.equals(11)) { //11 - ra
							if(crec.contact_id.equals(contact.id)) return true;
						}
					}
				} catch (SQLException e1) {
					log.error("Failed to lookup RA/sponsor information", e1);
				}
			}
		}
	
		return false;
	}
	
	//why can't we just issue certificate after it's been approved? because we might have to create pkcs12
	public boolean canIssue(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.APPROVED)) {			
			//requester oneself can issue
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				if(rec.requester_contact_id.equals(contact.id)) return true;
			} else {
				if(rec.requester_passphrase != null) {
					//guest user can try entering retrieval passphrase
					return true;
				}
			}
		}
		return false;
	}
		
	//NO-AC
	public void approve(CertificateRequestUserRecord rec) throws CertificateRequestException {
		//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canRequestUserCert(rec.requester_contact_id)) {
    		throw new CertificateRequestException("Exeeding Quota");
    	}
		
		if(rec.status.equals(CertificateRequestStatus.REQUESTED)) {
			//update request status
			rec.status = CertificateRequestStatus.APPROVED;
			try {
				super.update(get(rec.id), rec);
				quota.incrementUserCertRequest(rec.requester_contact_id);
			} catch (SQLException e) {
	    		throw new CertificateRequestException("Failed to approve user certificate request: " + rec.id, e);
			}	
			
			try {
				//get requester name
				ContactModel cmodel = new ContactModel(context);
				ContactRecord requester = cmodel.get(rec.requester_contact_id);
		
				//update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				ticket.description = "Dear " + requester.name + ",\n\n";
				ticket.description += "Your user certificate request has been approved.\n\n";
				ticket.description += "> " + context.getComment();
				ticket.description += "\n\nTo retrieve your certificate please visit " + getTicketUrl(rec.id) + " and click on Issue Certificate button.";
				ticket.nextaction = "Requester to download certificate"; // NAD will be set 7 days from today by default
				fp.update(ticket, rec.goc_ticket_id);
			} catch (SQLException e) {
				throw new CertificateRequestException("Probably faile to find requester for notification: "+rec.status);
			}
		
		} else {
			//shouldn't reach here.. 
    		throw new CertificateRequestException("Don't know how to approve request which is currently in stauts: "+rec.status);
		}
	}
	
	//NO-AC
	public void cancel(CertificateRequestUserRecord rec) throws CertificateRequestException {
		try {
			//info.. We can't put RENEW_REQUESTED back to ISSUED - since we've already reset certificate
			if(rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
				rec.status = CertificateRequestStatus.ISSUED;
			} else {
				rec.status = CertificateRequestStatus.CANCELED;
			}
			super.update(get(rec.id), rec);
			
			
			///////////////////////////////////////////////////////////////////////////////////////
			// All good - now close ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				ticket.description = contact.name + " has canceled this certificate request.\n\n";
			} else {
				ticket.description = "guest shouldn't be canceling";
			}
			ticket.description += "> " + context.getComment();
			ticket.status = "Resolved";
			fp.update(ticket, rec.goc_ticket_id);
			
		} catch (SQLException e) {
    		throw new CertificateRequestException("Failed to cancel user certificate request:" + rec.id, e);
		}
	}
	
	//NO-AC
	public void reject(CertificateRequestUserRecord rec) throws CertificateRequestException {
		if(	//rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			rec.status = CertificateRequestStatus.ISSUED;
		} else {
			//all others
			rec.status = CertificateRequestStatus.REJECTED;
		}
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
    		throw new CertificateRequestException("Failed to reject user certificate request:" + rec.id, e);
		}
		
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has rejected this certificate request.\n\n";
		ticket.description += "> " + context.getComment();
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	/*
	//NO-AC
	//reuse previously used csr - only oim user can do this
	public void requestRenew(CertificateRequestUserRecord rec) throws CertificateRequestException {
		if(!auth.isUser()) {
			throw new CertificateRequestException("only oim user can request renew");
		}
		
		rec.status = CertificateRequestStatus.RENEW_REQUESTED;
		
		//clear previously issued cert
    	rec.cert_certificate = null;
    	rec.cert_intermediate = null;
    	rec.cert_pkcs7 = null;
    	rec.cert_serial_id = null;
    	
    	//we don't need passphrase - only requester can renew, and we are not creating private key
    	rec.requester_passphrase = null;
    	rec.requester_passphrase_salt = null;
		
		try {
			super.update(get(rec.id), rec);
			
	    	//create notification ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
	    	prepareNewTicket(ticket, "User Certificate Renew Request", rec, auth.getContact());
			rec.goc_ticket_id = fp.open(ticket);
			context.setComment("Opened GOC Ticket " + rec.goc_ticket_id);
			super.update(get(rec.id), rec);
			
		} catch (SQLException e) {
			log.error("Failed to request user certificate request renewal: " + rec.id);
			throw new CertificateRequestException("Failed to update request status", e);
		}
	}
	*/
	
	//NO-AC
	public void requestRevoke(CertificateRequestUserRecord rec) throws CertificateRequestException {
		rec.status = CertificateRequestStatus.REVOCATION_REQUESTED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to request revocation of user certificate: " + rec.id);
			throw new CertificateRequestException("Failed to update request status", e);
		}
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
	
		//Update CC ra (it might have been changed since last time request was made)
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs;
		String ras = "";
		try {
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //11 - ra
					ticket.ccs.add(contactrec.primary_email);
					if(!ras.isEmpty()) {
						ras += ", ";
					}
					ras += contactrec.name;
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA/sponsor information - ignoring", e1);
		}
		
		ticket.description = "Dear "+ras+ " (RAs)\n\n";
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has requested revocation of this certificate request.";
		} else {
			ticket.description = "Guest user with IP:" + context.getRemoteAddr() + " has requested revocation of this certificate request.";		
		}
		ticket.description += "\n\nRA may revoke this request at " + getTicketUrl(rec.id);
		ticket.nextaction = "RA to process"; //nad will be set to 7 days from today by default
		ticket.status = "Engineering"; //probably need to reopen
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//NO-AC
	public void revoke(CertificateRequestUserRecord rec) throws CertificateRequestException {
		//revoke
		CertificateManager cm = new CertificateManager();
		try {
			cm.revokeUserCertificate(rec.cert_serial_id);
			log.info("Revoked " + rec.dn + " with serial id:" + rec.cert_serial_id);
			
			//update record
			try {
				rec.status = CertificateRequestStatus.REVOKED;
				//context.setComment("Certificate Approved");
				super.update(get(rec.id), rec);
			
			} catch (SQLException e) {
				log.error("Failed to update user certificate status: " + rec.id, e);
				throw new CertificateRequestException("Failed to update user certificate status", e);
			}
			
			//disable associated dn (if any)
			try {
				DNModel dnmodel = new DNModel(context);
				DNRecord dnrec = dnmodel.getByDNString(rec.dn);
				if(rec != null) {
					log.info("Disabling associated DN record");
					dnrec.disable = true;
					dnmodel.update(dnrec);
				}
			} catch (SQLException e) {
				log.warn("Failed to remove associated DN.. continuing", e);
			}
			
			Authorization auth = context.getAuthorization();
			ContactRecord contact = auth.getContact();
			
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.description = contact.name + " has revoked this certificate.\n\n";
			ticket.description += "> " + context.getComment();
			ticket.status = "Resolved";
			fp.update(ticket, rec.goc_ticket_id);
			
		} catch (CertificateProviderException e1) {
			log.error("Failed to revoke user certificate", e1);
			throw new CertificateRequestException("Failed to revoke user certificate", e1);
		}	
	
	}

	//NO-AC
	public void cancelWithPass(final  CertificateRequestUserRecord rec, final String password) throws CertificateRequestException {
		//verify passphrase if necessary
		if(rec.requester_passphrase != null) {
			String hashed = BCrypt.hashpw(password, rec.requester_passphrase_salt);
			if(!hashed.equals(rec.requester_passphrase)) {
				throw new CertificateRequestException("Failed to match password.");
			}
		}
		cancel(rec);
	}
	
	//go directly from ISSUED > ISSUEING
	public void renew(final CertificateRequestUserRecord rec) throws CertificateRequestException {		
		
		//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canRequestUserCert(rec.requester_contact_id)) {
    		throw new CertificateRequestException("Quota Exceeded");
    	}
   	
		//notification -- just make a note on an existing ticket - we don't need to create new goc ticket - there is nobody we need to inform -
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " is renewing user certificate.\n\n";
			ticket.description += "> " + context.getComment();
		} else {
			throw new CertificateRequestException("guest can't renew");
		}
		ticket.status = "Engineering";//reopen ticket temporarily
		fp.update(ticket, rec.goc_ticket_id);
		
		/*
		//clear previously issued cert
    	rec.cert_certificate = null;
    	rec.cert_intermediate = null;
    	rec.cert_pkcs7 = null;
    	rec.cert_serial_id = null;
    	*/
		
    	//we don't need passphrase - only requester can renew, and we are not creating private key
    	rec.requester_passphrase = null;
    	rec.requester_passphrase_salt = null;
    	
    	//start issuing immediately
		startissue(rec, null);
		
		//increment quota
		try {
			quota.incrementUserCertRequest(rec.requester_contact_id);
		} catch (SQLException e) {
    		log.error("Failed to incremenet quota while renewing request id:"+rec.id);
		}
		context.message(MessageType.SUCCESS, "Successfully renewed a certificate. Please download & install your certificate.");
	}
	
	// NO-AC
	public void startissue(final CertificateRequestUserRecord rec, final String password) throws CertificateRequestException {
		
		//verify passphrase (for guest request) to make sure we are issuing cert for person submitted the request
		if(rec.requester_passphrase != null) {
			String hashed = BCrypt.hashpw(password, rec.requester_passphrase_salt);
			if(!hashed.equals(rec.requester_passphrase)) {
				throw new CertificateRequestException("Failed to match password.");
			}
		}

		// mark the request as "issuing.."
		try {
			rec.status = CertificateRequestStatus.ISSUING;
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to issue user certificate for request:" + rec.id);
			throw new CertificateRequestException("Failed to update certificate request status");
		}

		//Maybe I should use Quartz instead? 
		new Thread(new Runnable() {
			public void failed(String message) {
				log.error(message);
				rec.status = CertificateRequestStatus.FAILED;
				try {
					context.setComment(message);
					CertificateRequestUserModel.super.update(get(rec.id), rec);
				} catch (SQLException e1) {
					log.error("Failed to update request status while processing failed condition :" + message, e1);
				}
				
				//update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				ticket.description = "Failed to issue certificate\n\n";
				ticket.description += message+"\n\n";
				ticket.description += "The alert has been sent to GOC alert for further actions on this issue.";
				ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.fail.assignee"));
				ticket.nextaction = "GOC developer to investigate";
				fp.update(ticket, rec.goc_ticket_id);
				
			}
			public void failed(String message, Throwable e) {				
				message += " :: " + e.getMessage();
				failed(message);
				log.error(e);
			}
			public void run() {
				try {					
					//if csr is not set, we need to create new one and private key for user
					if (rec.csr == null) {
						X500Name name = rec.getX500Name();
						GenerateCSR csrgen = new GenerateCSR(name);
						rec.csr = csrgen.getCSR();
						context.setComment("Generated CSR and private key");
						CertificateRequestUserModel.super.update(get(rec.id),rec);
	
						// store private key in memory to be used to create pkcs12 later
						HttpSession session = context.getSession();
						log.debug("user session ID:" + session.getId());
						session.setAttribute("PRIVATE_USER:" + rec.id, csrgen.getPrivateKey());
						session.setAttribute("PASS_USER:" + rec.id, password);
					}
					
					//lookup requester contact information
					ContactModel cmodel = new ContactModel(context);
					ContactRecord requester = cmodel.get(rec.requester_contact_id);
					
					//now we can sign it
					String cn = rec.getCN();//TODO - check for null?
					CertificateManager cm = new CertificateManager();
					ICertificateSigner.Certificate cert = cm.signUserCertificate(rec.csr, cn, requester.primary_email);
					rec.cert_certificate = cert.certificate;
					rec.cert_intermediate = cert.intermediate;
					rec.cert_pkcs7 = cert.pkcs7;
					rec.cert_serial_id = cert.serial;
					log.info("user cert issued by digicert: serial_id:" + cert.serial);
					log.info("pkcs7:" + cert.pkcs7);
					
					//get some information we need from the issued certificate
					java.security.cert.Certificate[]  chain = CertificateManager.parsePKCS7(rec.cert_pkcs7);
					X509Certificate c0 = (X509Certificate)chain[0];
					rec.cert_notafter = c0.getNotAfter();
					rec.cert_notbefore = c0.getNotBefore();
					
					//do a bit of validation
					Calendar today = Calendar.getInstance();
					if(Math.abs(today.getTimeInMillis() - rec.cert_notbefore.getTime()) > 1000*3600*24) {
						log.warn("User certificate issued for request "+rec.id+" has cert_notbefore set too distance from current timestamp");
					}
					long dayrange = (rec.cert_notafter.getTime() - rec.cert_notbefore.getTime()) / (1000*3600*24);
					if(dayrange < 390 || dayrange > 405) {
						log.warn("User certificate issued for request "+rec.id+ " has valid range of "+dayrange+" days (too far from 395 days)");
					}
					
					//update dn with the one returned by DigiCert
					X500Principal dn = c0.getSubjectX500Principal();
					String apache_dn = CertificateManager.X500Principal_to_ApacheDN(dn);
					rec.dn = apache_dn;
					
					//make sure dn starts with correct base
					String user_dn_base = StaticConfig.conf.getProperty("digicert.user_dn_base");
					if(!apache_dn.startsWith(user_dn_base)) {
						log.warn("User certificate issued for request " + rec.id + " has DN:"+apache_dn+" which doesn't have an expected DN base: "+user_dn_base);
					}
					
					DNModel dnmodel = new DNModel(context);
					DNRecord dnrec = dnmodel.getByDNString(rec.dn);
					if(dnrec == null) {
						//insert a new DN record
						dnrec = new DNRecord();
						dnrec.contact_id = rec.requester_contact_id;
						dnrec.dn_string = rec.dn;
						dnrec.disable = false;
						dnrec.id = dnmodel.insert(dnrec);
						
						//TODO - we should aggregate all currently approved authorization types and give the DN access to all of it instead
						//Give user OSG end user access
						DNAuthorizationTypeModel dnauthmodel = new DNAuthorizationTypeModel(context);
						DNAuthorizationTypeRecord dnauthrec = new DNAuthorizationTypeRecord();
						dnauthrec.dn_id = dnrec.id;
						dnauthrec.authorization_type_id = 1; //OSG End User
						dnauthmodel.insert(dnauthrec);
					} else {
						
						//TODO - -what is the point of this check? just paranoid?
						if(!dnrec.contact_id.equals(rec.requester_contact_id)) {
							//this DN belongs to someone else.
							//dnrec.contact_id = rec.requester_contact_id;
							log.error("The DN issued " + rec.dn + " is already registered to a different contact:" + dnrec.contact_id + " - updating it to " + rec.requester_contact_id + " this should have never happened");
						}
						
						dnrec.disable = false; //maybe it was disabled previously?
						dnmodel.update(dnrec);
					}
					
					//TODO - we've moved the timing of enabling contact enable from approve() to here. this means that, we could have 2 requests 
					//that are in APPROVED status and users can issue them both. This could lead to duplicate contacts with identical
					//email addresses.. to prevent this, we should validate against duplicate email inside
					//canIssue() again, but I am not sure that it's worth doing it.
					
					//enable requeser contact (just in case)
					requester.disable = false;
					cmodel.update(requester);
						
					//all done at this point
					rec.status = CertificateRequestStatus.ISSUED;
					context.setComment("Certificate has been issued by signer. serial number: " + rec.cert_serial_id);
					CertificateRequestUserModel.super.update(get(rec.id), rec);
	
					// update ticket
					Footprints fp = new Footprints(context);
					FPTicket ticket = fp.new FPTicket();
					Authorization auth = context.getAuthorization();
					if(auth.isUser()) {
						ContactRecord contact = auth.getContact();
						ticket.description = contact.name + " has issued certificate. Resolving this ticket.";
					} else {
						ticket.description = "Guest with IP:" + context.getRemoteAddr() + " has issued certificate. Resolving this ticket.";
					}
					ticket.status = "Resolved";
					fp.update(ticket, rec.goc_ticket_id);
					
				} catch (ICertificateSigner.CertificateProviderException e) {
					failed("Failed to sign certificate -- CertificateProviderException ", e);
				} catch (CMSException e) { //from parsePKCS7
					failed("Failed to sign certificate -- can't parse returned pkcs7 for request id:" + rec.id, e);
				} catch (Exception e) { //probably from parsePKCS7 (like StringIndexOutOfBoundsException)
					failed("Failed to sign certificate -- can't parse returned pkcs7 request id:" + rec.id, e);
				}
			}
		}).start();
	}

	public PrivateKey getPrivateKey(Integer id) {
		HttpSession session = context.getSession();
		if(session == null) return null;
		return (PrivateKey)session.getAttribute("PRIVATE_USER:"+id);	
	}
	
	public String getPassword(Integer id) {
		HttpSession session = context.getSession();
		if(session == null) return null;
		return (String)session.getAttribute("PASS_USER:"+id);		
	}
	

	//construct pkcs12 using private key stored in session and issued certificate
	//return null if unsuccessful - errors are logged
	public KeyStore getPkcs12(CertificateRequestUserRecord rec) {			
		//pull certificate chain from pkcs7

		try {
			java.security.cert.Certificate[] chain = CertificateManager.parsePKCS7(rec.cert_pkcs7);
			
			//HttpSession session = context.getSession();
			String password = getPassword(rec.id);
			if(password == null) {
				log.error("can't retrieve certificate encryption password while creating pkcs12");
				
				/*
				//experimenet trying to create pkcs12 without private key - doesn't work
				KeyStore p12 = KeyStore.getInstance("PKCS12");
				p12.load(null, null);
				p12.setCertificateEntry("USER"+rec.id, chain[0]);
				return p12;
				*/
			} else {
				KeyStore p12 = KeyStore.getInstance("PKCS12");
				p12.load(null, null);  //not sure what this does.
				PrivateKey private_key = getPrivateKey(rec.id);
				p12.setKeyEntry("USER"+rec.id, private_key, password.toCharArray(), chain); 
				return p12;
			}
		} catch (IOException e) {
			log.error("Failed to get encoded byte array from bouncy castle certificate.", e);
		} catch (CertificateException e) {
			log.error("Failed to generate java security certificate from byte array", e);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CMSException e) {
			log.error("Failed to get encoded byte array from bouncy castle certificate.", e);
		} 
		return null;
	}

	//NO-AC
	public CertificateRequestUserRecord get(int id) throws SQLException {
		CertificateRequestUserRecord rec = null;
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE id = " + id)) {
	    	rs = stmt.getResultSet();
	    	if(rs.next()) {
	    		rec = new CertificateRequestUserRecord(rs);
			}
	    }	
	    stmt.close();
	    conn.close();
	    return rec;
	}
	
	/*
	//NO-AC
	public CertificateRequestUserRecord getByDN(String apache_dn) throws SQLException {
		CertificateRequestUserRecord rec = null;
		ResultSet rs = null;
		Connection conn = connectOIM();
		PreparedStatement stmt = conn.prepareStatement("SELECT * FROM "+table_name+ " WHERE dn = ?");
		stmt.setString(1, apache_dn);
	    rs = stmt.executeQuery();
	    if(rs.next()) {
    		rec = new CertificateRequestUserRecord(rs);
	    }
	    stmt.close();
	    conn.close();
	    return rec;
	}
	*/
	
	public void processExpired() throws SQLException {
		
		//search for expired certificates
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE status = '"+CertificateRequestStatus.ISSUED+"' AND cert_notafter < CURDATE()")) {
	    	rs = stmt.getResultSet();
	    	
	    	DNModel dnmodel = new DNModel(context);
	    	
	    	while(rs.next()) {
	    		CertificateRequestUserRecord rec = new CertificateRequestUserRecord(rs);
    			rec.status = CertificateRequestStatus.EXPIRED;
	    		context.setComment("Certificate is no longer valid.");
    			super.update(get(rec.id), rec);
    			
    			//disable DN (TODO -- Not yet tested..)
    			DNRecord dnrec = dnmodel.getByDNString(rec.dn);
    			if(dnrec != null) {
    				dnrec.disable = true;
    				dnmodel.update(dnrec);
    			}
    			
				// update ticket
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				
				ContactModel cmodel = new ContactModel(context);
				ContactRecord requester = cmodel.get(rec.requester_contact_id);
				
				//send notification
				ticket.description = "Dear " + requester.name + ",\n\n";
				ticket.description += "Your user certificate ("+rec.dn+") has expired. Please visit "+getTicketUrl(rec.id)+" and submit re-request.\n\n";
				
				fp.update(ticket, rec.goc_ticket_id);
				
				log.info("sent expiration notification for user certificate request: " + rec.id + " (ticket id:"+rec.goc_ticket_id+")");
			}
	    }	
	    stmt.close();
	    conn.close();
	}
	
	public void notifyExpiringIn(Integer days) throws SQLException {
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		ContactModel cmodel = new ContactModel(context);
		
		for(CertificateRequestUserRecord rec : findExpiringIn(days)) {
			
			ContactRecord requester = cmodel.get(rec.requester_contact_id);
			Date expiration_date = rec.cert_notafter;
			
			//send notification
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.description = "Dear " + requester.name + ",\n\n";
			ticket.description += "Your user certificate ("+rec.dn+") will expire on "+dformat.format(expiration_date)+"\n\n";
			
			//can user renew?
			ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs =
					getLogs(CertificateRequestUserModel.class, rec.id);
			if(canRenew(rec, logs, requester)) {	
				ticket.description += "Please renew by visiting "+getTicketUrl(rec.id)+"\n\n";
				ticket.status = "Engineering"; //reopen it - until user renew
			} else {
				ticket.description += "Please request for new user certificate by visiting https://oim.grid.iu.edu/oim/certificaterequestuser\n\n";
			}
			
			//TODO - clear CC list (or suppress cc-emailing)
			
			if(StaticConfig.isDebug()) {
				log.debug("skipping (this is debug) ticket update on ticket : " + rec.goc_ticket_id + " to notify expiring user certificate");
				log.debug(ticket.description);
				log.debug(ticket.status);
			} else {
				fp.update(ticket, rec.goc_ticket_id);
				log.info("updated goc ticket : " + rec.goc_ticket_id + " to notify expiring user certificate");
			}
		
		}
	}
	//return requests that I am RA of
	public ArrayList<CertificateRequestUserRecord> getIApprove(Integer id) throws SQLException {
		ArrayList<CertificateRequestUserRecord> recs = new ArrayList<CertificateRequestUserRecord>();
		
		//list all vo that user is ra of
		HashSet<Integer> vo_ids = new HashSet<Integer>();
		VOContactModel model = new VOContactModel(context);
		try {
			for(VOContactRecord crec : model.getByContactID(id)) {
				if(crec.contact_type_id.equals(11)) { //RA
					vo_ids.add(crec.vo_id);	
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA information", e1);
		}	
		
		if(vo_ids.size() != 0) {
			ResultSet rs = null;
			Connection conn = connectOIM();
			Statement stmt = conn.createStatement();
			String vo_ids_str = StringUtils.join(vo_ids, ",");
			stmt.execute("SELECT * FROM "+table_name + " WHERE vo_id IN ("+vo_ids_str+") AND status in ('REQUESTED','RENEW_REQUESTED','REVOKE_REQUESTED')");	
	    	rs = stmt.getResultSet();
	    	while(rs.next()) {
	    		recs.add(new CertificateRequestUserRecord(rs));
	    	}
		    stmt.close();
		    conn.close();
		}

	    return recs;
	}
	
	public ArrayList<CertificateRequestUserRecord> getISubmitted(Integer contact_id) throws SQLException {
		ArrayList<CertificateRequestUserRecord> recs = new ArrayList<CertificateRequestUserRecord>();
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id = " + contact_id);	
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		recs.add(new CertificateRequestUserRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return recs;
	}
	
	public ArrayList<CertificateRequestUserRecord> findExpiringIn(Integer days) throws SQLException {
		ArrayList<CertificateRequestUserRecord> recs = new ArrayList<CertificateRequestUserRecord>();
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE status = '"+CertificateRequestStatus.ISSUED+"' AND CURDATE() > DATE_SUB( cert_notafter, INTERVAL "+days+" DAY )");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		recs.add(new CertificateRequestUserRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return recs;
	}
	
	//return requests that guest has submitted
	public ArrayList<CertificateRequestUserRecord> getGuest() throws SQLException {
		ArrayList<CertificateRequestUserRecord> ret = new ArrayList<CertificateRequestUserRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id is NULL");
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		ret.add(new CertificateRequestUserRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return ret;
	}
	
    public boolean requestWithCSR(String csr, String fullname, Integer vo_id) throws SQLException { 
    	//TODO
    	
    	return false;
    }
    
    public CertificateRequestUserRecord requestUsertWithNOCSR(
    	Integer vo_id, ContactRecord requester, ContactRecord sponsor, String cn, String request_comment) 
    			throws SQLException, CertificateRequestException {
		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();
    	request(vo_id, rec, requester, sponsor, cn, request_comment);
    	return rec;
    }
       
    //returns insertec request record if successful. if not, null
    public CertificateRequestUserRecord requestGuestWithNOCSR(
    	Integer vo_id, ContactRecord requester, ContactRecord sponsor, String passphrase, String request_comment) 
    		throws SQLException, CertificateRequestException {     	
		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();		
		String salt = BCrypt.gensalt(12);//let's hard code this for now..
		rec.requester_passphrase_salt = salt;
		rec.requester_passphrase = BCrypt.hashpw(passphrase, salt);
    	request(vo_id, rec, requester, sponsor, null, request_comment);//cn is not known until we check the contact
    	return rec;
    } 
    
    public X500Name generateDN(String cn) {
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE);

        //DigiCert overrides the DN, so none of these matters - except CN which is used to send common_name parameter
        //We are creating this so that we can create private key
        x500NameBld.addRDN(BCStyle.DC, "com");
        x500NameBld.addRDN(BCStyle.DC, "DigiCert-Grid");
        if(StaticConfig.isDebug()) {
        	//let's assume debug means we are using digicert pilot
        	x500NameBld.addRDN(BCStyle.O, "OSG Pilot");
        } else {
        	x500NameBld.addRDN(BCStyle.O, "Open Science Grid");
        }
        x500NameBld.addRDN(BCStyle.OU, "People");   
        x500NameBld.addRDN(BCStyle.CN, cn); //don't use "," or "/" which is used for DN delimiter
        
        return x500NameBld.build();
    }
    
    private String getTicketUrl(Integer ticket_id) {
    	String base;
    	if(StaticConfig.isDebug()) {
    		base = "https://oim-itb.grid.iu.edu/oim/";
    	} else {
    		base = "https://oim.grid.iu.edu/oim/";
    	}
		return base + "certificateuser?id=" + ticket_id;
    }
    
    //NO-AC 
    //return true for success
    private void request(Integer vo_id, CertificateRequestUserRecord rec, ContactRecord requester, ContactRecord sponsor, String cn, String request_comment) throws SQLException, CertificateRequestException 
    {
    	
    	String note = "";
    	
		///////////////////////////////////////////////////////////////////////////////////////////
		// Check conditions & finalize DN (register contact if needed)
		DNModel dnmodel = new DNModel(context);
		if(auth.isUser()) {
			//generate DN
			X500Name name = generateDN(cn);
			rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
			note += "NOTE: Additional user certificate request for OIM user\n";
		} else {
			//Guest request -- check contact record with the same email address
			ContactModel cmodel = new ContactModel(context);
			ContactRecord existing_crec = cmodel.getEnabledByemail(requester.primary_email);//this is from the form, so I just have to check against primary_email
			if(existing_crec == null) {
				//register new disabled contact (until issued)
				requester.disable = true; //don't enable until the request gets approved
				requester.id = cmodel.insert(requester);
				
				//and generate dn
		    	cn = requester.name + " " + requester.id;
		    	X500Name name = generateDN(cn);
				rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
				note += "NOTE: User is registering OIM contact & requesting new certificate: contact id:"+requester.id+"\n";
			} else {
				//Guest is attempting take over existing contact
				//generate dn (with existing_crec id)
		    	cn = requester.name + " " + existing_crec.id;
		    	X500Name name = generateDN(cn);
				rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
				
				//find if the existing contact has any DN associated with the contact
				ArrayList<DNRecord> dnrecs = dnmodel.getEnabledByContactID(existing_crec.id);
				if(dnrecs.isEmpty()) {
					//OK for contact record take over
					
					//pre-registered contact - just let user associate with this contact id
					requester.id = existing_crec.id;
					requester.disable = false;
					requester.person = true;
					
					//update contact information with information that user just gave me
					//NOTE -- we are updating oim contact from guest submitted info before doing DN 
					//collision test here
					cmodel.update(requester);
					
					note += "NOTE: User is taking over a contact only account with id: "+existing_crec.id+"\n";
				} else {
					//we can't take over contact record that already has DN attached.
					throw new CertificateRequestException("Provided email address is already associated with existing OIM account. If you are already registered to OIM, please login before making your request. Please contact GOC for more assistance.");
				}
			}	
		}
		
		//make sure we won't collide with existing dn
		DNRecord existing_rec = dnmodel.getEnabledByDNString(rec.dn);
		if(existing_rec != null) {
			throw new CertificateRequestException("The DN already exist in OIM (contact ID:"+existing_rec.contact_id+"). Please choose different CN, or contact GOC for more assistance.");
		}
		
		note += "NOTE: Requested DN: " + rec.dn + "\n\n";
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Make Request
		Date current = new Date();
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;	
		rec.requester_contact_id = requester.id;
		rec.vo_id = vo_id;
		
		if(request_comment != null) {
			note += "Requester Comment: "+request_comment;
			context.setComment(request_comment);
		} else {
			context.setComment("Making Request for " + requester.name);
		}
    	super.insert(rec);
		//quota.incrementUserCertRequest();

    	//create notification ticket
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
    	prepareNewTicket(ticket, "User Certificate Request",  rec, requester, sponsor);
		ticket.description += "\n\n"+note;
		rec.goc_ticket_id = fp.open(ticket);
		context.setComment("Opened GOC Ticket " + rec.goc_ticket_id);
		super.update(get(rec.id), rec);
    }
    
    private void prepareNewTicket(FPTicket ticket, String title, CertificateRequestUserRecord rec, ContactRecord requester, ContactRecord sponsor) throws SQLException {
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		
		//Create ra & sponsor list
		String ranames = "";
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs;
		try {
			//add RAs
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //11 - ra
					ticket.ccs.add(contactrec.primary_email);
					if(ranames.length() != 0) {
						ranames += ", ";
					}
					ranames += contactrec.name;
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA information - ignoring", e1);
		}

		//add sponsor email
		ticket.ccs.add(sponsor.primary_email);
		
		//submit goc ticket
		VOModel vmodel = new VOModel(context);
		VORecord vrec = vmodel.get(rec.vo_id);
		
		ticket.title = title + " for "+requester.name + "(VO:"+vrec.name+")";
		String auth_status = "An unauthenticated user; ";
		if(auth.isUser()) {
			auth_status = "An OIM Authenticated user; ";
		}
		ticket.description = "Dear " + ranames + " (" + vrec.name + " VO RAs),\n\n";
		ticket.description += auth_status + requester.name + " <"+requester.primary_email+"> has requested a user certificate. ";
		ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + getTicketUrl(rec.id) + "\n\n";
		
		//if sponsor id is null, that means it doesn't exist in our contact DB.
		if(sponsor.id == null) {
			ticket.description += "User has manually entered sponsor information with name: " + sponsor.name + ". RA must confirm the identify of this sponsor. ";
			ticket.description += "RA should also consider registering this sponsor for this VO.";
		} else {
			ticket.description += "User has selected a registered sponsor: " + sponsor.name + " who has been CC-ed to this request. ";
		}
		/*
		if(StaticConfig.isDebug()) {
			ticket.assignees.add("hayashis");
		} else {
			ticket.assignees.add("adeximo");
		}
		*/
		ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.user.assignee"));
		ticket.ccs.add("osg-ra@opensciencegrid.org");
		
		ticket.nextaction = "RA/Sponsors to verify requester";	 //NAD will be set to 7 days in advance by default
		
		//set metadata
		ticket.metadata.put("ASSOCIATED_VO_ID", vrec.id.toString());
		ticket.metadata.put("ASSOCIATED_VO_NAME", vrec.name);
		SCModel scmodel = new SCModel(context);
		SCRecord screc = scmodel.get(vrec.sc_id);
		ticket.metadata.put("SUPPORTING_SC_ID", screc.id.toString());
		ticket.metadata.put("SUPPORTING_SC_NAME", screc.name);
		ticket.metadata.put("SUBMITTED_VIA", "OIM/CertManager(user)");
		if(auth.isUser()) {
			ticket.metadata.put("SUBMITTER_DN", auth.getUserDN());
		} 
		ticket.metadata.put("SUBMITTER_NAME", requester.name);
    }
    
    //no-ac
    public boolean rerequest(CertificateRequestUserRecord rec, String guest_passphrase) throws CertificateRequestException 
    {    	
		rec.status = CertificateRequestStatus.REQUESTED;
		rec.csr = null; //this causes issue() to regenerate CSR with new private key
		if(guest_passphrase != null) {
			//submitted as guest
			String salt = BCrypt.gensalt(12);//let's hard code this for now..
			rec.requester_passphrase_salt = salt;
			rec.requester_passphrase = BCrypt.hashpw(guest_passphrase, salt);
		} else {
			//need to reset passphrase if there is any
			rec.requester_passphrase_salt = null;
			rec.requester_passphrase = null;
			
			//only the same user should be able to re-request, but just in case..
			rec.requester_contact_id = auth.getContact().id;
		}
		
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
			//quota.incrementUserCertRequest();
		} catch (SQLException e) {
			log.error("Failed to re-request user certificate request: " + rec.id);
			return false;
		}
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Notification
		
		try {
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			
			//CC RAs
			String ranames = "";
			VOContactModel model = new VOContactModel(context);
			ContactModel cmodel = new ContactModel(context);
			ArrayList<VOContactRecord> crecs;
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //11 - ra
					ticket.ccs.add(contactrec.primary_email);
					
					if(ranames.length() != 0) {
						ranames += ", ";
					}
					ranames += contactrec.name;
				}
			}
			
			//CC sponsor
			//TODO - need to re-CC sponsor, but I am not sure how.
			
			ContactRecord requester = cmodel.get(rec.requester_contact_id);
			VOModel vmodel = new VOModel(context);
			VORecord vrec = vmodel.get(rec.vo_id);
			ticket.description = "Dear " + ranames + " (" + vrec.name + " VO RA/Sponsors),\n\n";
			if(guest_passphrase != null) {
				ticket.description += "A guest user has re-requested this user certificate request. Please contact the original requester; " + requester.name + 
					" <"+requester.primary_email+"> and confirm authenticity of this re-request, and approve / disapprove at" + getTicketUrl(rec.id);
			} else {
				ticket.description += "An OIM Authenticated user "  + requester.name + " <"+requester.primary_email+"> has re-requested this user certificate request. ";
				ticket.description += "Please approve / disapprove this request at " + getTicketUrl(rec.id);
			}

			ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.user.assignee"));
			ticket.nextaction = "RA/Sponsors to verify requester";	 //NAD will be set to 7 days in advance by default
			ticket.status = "Engineering"; //I need to reopen resolved ticket.
			fp.update(ticket, rec.goc_ticket_id);
			
		} catch (SQLException e1) {
			log.error("Failed to create ticket update content - ignoring", e1);
		}
		
		return true;
    }
	//prevent low level access - please use model specific actions
    @Override
    public Integer insert(CertificateRequestUserRecord rec) throws SQLException
    { 
    	throw new UnsupportedOperationException("Please use model specific actions instead (request, approve, reject, etc..)");
    }
    @Override
    public void update(CertificateRequestUserRecord oldrec, CertificateRequestUserRecord newrec) throws SQLException
    {
    	throw new UnsupportedOperationException("Please use model specific actions insetead (request, approve, reject, etc..)");
    }
    @Override
    public void remove(CertificateRequestUserRecord rec) throws SQLException
    {
    	throw new UnsupportedOperationException("disallowing remove cert request..");
    }
    
    /*
    public void _test() {
    	try {
			CertificateRequestUserRecord rec = get(21);
			try {
				java.security.cert.Certificate[]  chain = CertificateManager.parsePKCS7(rec.cert_pkcs7);
				X509Certificate c0 = (X509Certificate)chain[0];
				Date not_after = c0.getNotAfter();
				Date not_before = c0.getNotBefore();
		
			} catch (CMSException e) {
				log.error("Failed to lookup certificate information for issued user cert request id:" + rec.id, e);
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    */

    //NO AC
	public CertificateRequestUserRecord getBySerialID(String serial_id) throws SQLException {
		CertificateRequestUserRecord rec = null;
		ResultSet rs = null;
		Connection conn = connectOIM();
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM "+table_name+ " WHERE cert_serial_id = ?");
		pstmt.setString(1, serial_id);
	    if (pstmt.executeQuery() != null) {
	    	rs = pstmt.getResultSet();
	    	if(rs.next()) {
	    		rec = new CertificateRequestUserRecord(rs);
			}
	    }	
	    pstmt.close();
	    conn.close();
	    return rec;
		
	}

	//pass null to not filter
	public ArrayList<CertificateRequestUserRecord> search(String dn_contains, String status, Integer vo_id, Date request_after, Date request_before) throws SQLException {
		ArrayList<CertificateRequestUserRecord> recs = new ArrayList<CertificateRequestUserRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		String sql = "SELECT * FROM "+table_name+" WHERE 1 = 1";
		if(dn_contains != null) {
			sql += " AND dn like \"%"+StringEscapeUtils.escapeSql(dn_contains)+"%\"";
		}
		if(status != null) {
			sql += " AND status = \""+status+"\"";
		}
		if(vo_id != null) {
			sql += " AND vo_id = "+vo_id;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(request_after != null) {
			sql += " AND request_time >= \""+sdf.format(request_after) + "\"";
		}
		if(request_before != null) {
			sql += " AND request_time <= \""+sdf.format(request_before) + "\"";
		}
		
		PreparedStatement stmt = conn.prepareStatement(sql);
	    rs = stmt.executeQuery();
	    while(rs.next()) {
    		recs.add(new CertificateRequestUserRecord(rs));
	    }
	    stmt.close();
	    conn.close();
	    return recs;
	}

	@Override
	CertificateRequestUserRecord createRecord() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
}
