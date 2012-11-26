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
import java.util.Date;
import java.util.HashSet;

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
import edu.iu.grid.oim.lib.StringArray;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
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
    /*
    //find certificate request with the same DN that user is currently using to login
    public CertificateRequestUserRecord getCurrent() throws SQLException {
    	CertificateRequestUserRecord rec = null;
    	Authorization auth = context.getAuthorization();
    	if(!auth.isUser()) {
    		return null;
    	}
    	
		ResultSet rs = null;
		Connection conn = connectOIM();
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM "+table_name+" WHERE dn = ?");
		pstmt.setString(1, auth.getUserDN());
		//log.debug(pstmt.toString());
	    if (pstmt.execute()) {
	    	rs = pstmt.getResultSet();
	    	if(rs != null && rs.next()) {
	    		
	    		rec = new CertificateRequestUserRecord(rs);
			}
	    }	
		pstmt.close();
		conn.close();
	    return rec;
    }
	*/
    
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
			if(crec.contact_type_id.equals(11) 
					&& (crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2)) ) { //primary and secondary RA
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
			if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(3) ) { //terriary is sponsor
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
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			//RA doesn't *approve* REVOKACTION - RA just click on revoke button
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				/*
				//super ra can see all requests
				if(auth.allows("admin_all_user_cert_requests")) return true;
				*/
				
				//Is user RA agent for specified vo?
				VOContactModel model = new VOContactModel(context);
				ContactModel cmodel = new ContactModel(context);
				ArrayList<VOContactRecord> crecs;
				try {
					crecs = model.getByVOID(rec.vo_id);
					for(VOContactRecord crec : crecs) {
						if(crec.contact_type_id.equals(11) && //RA
							(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2))) { //primary or secondary
							//ContactRecord contactrec = cmodel.get(crec.contact_id);
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
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				//requester can cancel one's own request
				if(rec.requester_contact_id.equals(contact.id)) return true;
				
				//ra can cancel
				VOContactModel model = new VOContactModel(context);
				ContactModel cmodel = new ContactModel(context);
				ArrayList<VOContactRecord> crecs;
				try {
					crecs = model.getByVOID(rec.vo_id);
					for(VOContactRecord crec : crecs) {
						if(crec.contact_type_id.equals(11) && //RA
								(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2))) { //primary or secondary
							//ContactRecord contactrec = cmodel.get(crec.contact_id);
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
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(!auth.isUser()) {
				//guest can cancel guest submitted request with a valid pass
				if(rec.requester_passphrase != null) {
					return true;	
				}
			}
		}
		return false;
	}
	
	public boolean canRequestRenew(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canReRequest(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REJECTED) ||
			rec.status.equals(CertificateRequestStatus.CANCELED) ||
			rec.status.equals(CertificateRequestStatus.REVOKED) ) {
			if(auth.isUser()) {
				return true;
			}
		}		
		
		if (rec.status.equals(CertificateRequestStatus.EXPIRED) ) {
			//guest user needs to be able to re-request expired cert.. but how can I prevent spammer?
			return true;
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
				ContactModel cmodel = new ContactModel(context);
				ArrayList<VOContactRecord> crecs;
				try {
					crecs = model.getByVOID(rec.vo_id);
					for(VOContactRecord crec : crecs) {
						//ContactRecord contactrec = cmodel.get(crec.contact_id);
						if(crec.contact_type_id.equals(11) && //RA
								(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2))) { //primary or secondary
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
	//return true if success
	public boolean approve(CertificateRequestUserRecord rec) {
		//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canRequestUserCert(rec.requester_contact_id)) {
    		log.error("Exceeded user quota.");
    		return false;
    	}
    	
		
		if(rec.status.equals(CertificateRequestStatus.REQUESTED)) {
			//update request status
			rec.status = CertificateRequestStatus.APPROVED;
			try {
				super.update(get(rec.id), rec);
				quota.incrementUserCertRequest(rec.requester_contact_id);
			} catch (SQLException e) {
				log.error("Failed to approve user certificate request: " + rec.id, e);
				return false;
			}	
			
			return approveNewRequest(rec);
		} else if(rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			//update request status
			rec.status = CertificateRequestStatus.APPROVED;
			try {
				super.update(get(rec.id), rec);
				quota.incrementUserCertRequest(rec.requester_contact_id);
			} catch (SQLException e) {
				log.error("Failed to approve user certificate request: " + rec.id, e);
				return false;
			}
			
			return approveRenewRequest(rec);
		} else {
			log.error("Don't know how to approve request which is currently in stauts: "+rec.status);
			return false;
		}
	}
	
	private boolean approveNewRequest(CertificateRequestUserRecord rec) {
	
		try {
			//Then insert a new DN record
			DNRecord dnrec = new DNRecord();
			dnrec.contact_id = rec.requester_contact_id;
			dnrec.dn_string = rec.dn;
			dnrec.disable = false;
			//dnrec.usercert_request_id = rec.id;
			DNModel dnmodel = new DNModel(context);
			dnrec.id = dnmodel.insert(dnrec);
			
			//Give user OSG end user access
			DNAuthorizationTypeModel dnauthmodel = new DNAuthorizationTypeModel(context);
			DNAuthorizationTypeRecord dnauthrec = new DNAuthorizationTypeRecord();
			dnauthrec.dn_id = dnrec.id;
			dnauthrec.authorization_type_id = 1; //OSG End User
			dnauthmodel.insert(dnauthrec);
			
			//enable contact
			ContactModel cmodel = new ContactModel(context);
			ContactRecord requester = cmodel.get(rec.requester_contact_id);
			requester.disable = false;
			cmodel.update(cmodel.get(rec.requester_contact_id), requester);

			//update ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.description = "Dear " + requester.name + ",\n\n";
			ticket.description += "Your user certificate request has been approved.\n\n";
			ticket.description += "> " + context.getComment();
			ticket.description += "\n\nTo retrieve your certificate please visit " + getTicketUrl(rec) + " and click on Issue Certificate button.";
			ticket.nextaction = "Requester to download certificate"; // NAD will be set 7 days from today by default
			fp.update(ticket, rec.goc_ticket_id);
			
		} catch (SQLException e) {
			log.error("Failed to associate new DN with requeter contact", e);
		}
		
		return true;
	}
	
	private boolean approveRenewRequest(CertificateRequestUserRecord rec) {
		
		//notify user
		try {
			//pull requester info
			ContactModel cmodel = new ContactModel(context);
			ContactRecord requester = cmodel.get(rec.requester_contact_id);
			
			//update ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.description = "Dear " + requester.name + ",\n\n";
			ticket.description += "Your user certificate request has been approved.\n\n";
			ticket.description += "> " + context.getComment();
			ticket.description += "\n\nTo retrieve your certificate please visit " + getTicketUrl(rec) + " and click on Issue Certificate button.";
			ticket.nextaction = "Requester to download certificate"; // NAD will be set 7 days from today by default
			fp.update(ticket, rec.goc_ticket_id);
		} catch (SQLException e) {
			log.error("Failed to approve user certificate request: " + rec.id + " while obtaining requester info", e);
			return false;
		}
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean cancel(CertificateRequestUserRecord rec) {
		try {
			if(rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
				rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
				rec.status = CertificateRequestStatus.ISSUED;
			} else {
				rec.status = CertificateRequestStatus.CANCELED;
			}
			super.update(get(rec.id), rec);
			
			
			///////////////////////////////////////////////////////////////////////////////////////
			// All good - now close ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			Authorization auth = context.getAuthorization();
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
			log.error("Failed to cancel user certificate request:" + rec.id);
			return false;
		}
		
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean reject(CertificateRequestUserRecord rec) {
		if(	rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			//go back to issued status if it's from renew_requested
			rec.status = CertificateRequestStatus.ISSUED;
		} else {
			//all others
			rec.status = CertificateRequestStatus.REJECTED;
		}
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to reject user certificate request:" + rec.id);
			return false;
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has rejected this certificate request.\n\n";
		ticket.description += "> " + context.getComment();
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public void requestRenew(CertificateRequestUserRecord rec) throws CertificateRequestException {
		rec.status = CertificateRequestStatus.RENEW_REQUESTED;
		
		//setting this to null so that oim will regenerate key which is generated when csr is created 
		//we shoudn't do this if user is providing us the CSR (guessing user will re-use the same private key)
		rec.csr = null; 
		
    	rec.cert_certificate = null;
    	rec.cert_intermediate = null;
    	rec.cert_pkcs7 = null;
    	rec.cert_serial_id = null;
		try {
			super.update(get(rec.id), rec);
			//quota.incrementUserCertRequest();
		} catch (SQLException e) {
			log.error("Failed to request user certificate request renewal: " + rec.id);
			throw new CertificateRequestException("Failed to update request status", e);
		}
		
	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		
		//Update CC ra & sponsor (it might have been changed since last time request was made)
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs;
		String ras = "";
		try {
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //RA contacts
					ticket.ccs.add(contactrec.primary_email);
					if(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2)) {
						if(!ras.isEmpty()) {
							ras += ", ";
						}
						ras += contactrec.name;
					}
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA/sponsor information - ignoring", e1);
		}
		
		ticket.description = "Dear "+ras+ " (RAs)\n\n";
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description += "An authenticated user; " + contact.name + " has requested renewal for this certificate request.\n\n";
		} else {
			ticket.description += "A guest user with IP address: " + context.getRemoteAddr() + " has requested renewal for this certificate request.\n\n";
		}
		ticket.description += "> " + context.getComment();
		ticket.description += "\n\nPlease approve / disapporove this request at " + getTicketUrl(rec);
		ticket.nextaction = "RA/Sponsor to verify&approve"; //nad will be set to 7 days from today by default
		ticket.status = "Engineering";
		
		fp.update(ticket, rec.goc_ticket_id);
	}
	
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
	
		//Update CC ra & sponsor (it might have been changed since last time request was made)
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs;
		String ras = "";
		try {
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //RA contacts
					ticket.ccs.add(contactrec.primary_email);
					if(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2)) {
						if(!ras.isEmpty()) {
							ras += ", ";
						}
						ras += contactrec.name;
					}
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
		ticket.description += "\n\nPlease approve / disapporove this request at " + getTicketUrl(rec);
		ticket.nextaction = "RA to process"; //nad will be set to 7 days from today by default
		ticket.status = "Engineering"; //probably need to reopen
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//NO-AC
	//return true if success
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
			
			//remove associated dn (if any)
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
	
	// NO-AC
	// return true if success
	public void startissue(final CertificateRequestUserRecord rec, final String password) throws CertificateRequestException {
		
		//verify passphrase if necessary
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
				ticket.description += "The alert has been sent to GOC alert for furthre actions on this issue.";
				ticket.ccs.add(StaticConfig.conf.getProperty("certrequest.fail.assignee"));
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
					//if csr is not set, we need to create one and private key for user
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
	
	public void processExpired() throws SQLException {
		
		//search for expired certificates
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE cert_notafter < CURDATE()")) {
	    	rs = stmt.getResultSet();
	    	
	    	DNModel dnmodel = new DNModel(context);
	    	
	    	while(rs.next()) {
	    		CertificateRequestUserRecord rec = new CertificateRequestUserRecord(rs);
	    		if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
	    			rec.status = CertificateRequestStatus.EXPIRED;
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
					ticket.description = "Certificate has been expired";
					fp.update(ticket, rec.goc_ticket_id);
	    		}
			}
	    }	
	    stmt.close();
	    conn.close();
	}
	
	//return requests that I am ra/sponsor
	public ArrayList<CertificateRequestUserRecord> getIApprove(Integer id) throws SQLException {
		ArrayList<CertificateRequestUserRecord> recs = new ArrayList<CertificateRequestUserRecord>();
		
		//list all vo that user is ra/sponsor of
		HashSet<Integer> vo_ids = new HashSet<Integer>();
		VOContactModel model = new VOContactModel(context);
		try {
			for(VOContactRecord crec : model.getByContactID(id)) {
				if(crec.contact_type_id.equals(11)) { //RA
					vo_ids.add(crec.vo_id);	
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA/sponsor information", e1);
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
    
    public CertificateRequestUserRecord requestUsertWithNOCSR(Integer vo_id, ContactRecord requester, String cn, String request_comment) throws SQLException, CertificateRequestException {
    	
    	//TODO -- check access

		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();
    	request(vo_id, rec, requester, cn, request_comment);
    	return rec;
    }
       
    //returns insertec request record if successful. if not, null
    public CertificateRequestUserRecord requestGuestWithNOCSR(Integer vo_id, ContactRecord requester, String passphrase, String request_comment) throws SQLException, CertificateRequestException { 
    	//TODO -- check access
    	
		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();		
		String salt = BCrypt.gensalt(12);//let's hard code this for now..
		rec.requester_passphrase_salt = salt;
		rec.requester_passphrase = BCrypt.hashpw(passphrase, salt);
    	request(vo_id, rec, requester, null, request_comment);//cn is not known until we check the contact
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
    
    private String getTicketUrl(CertificateRequestUserRecord rec) {
    	String base;
    	if(StaticConfig.isDebug()) {
    		base = "https://oim-itb.grid.iu.edu/oim/";
    	} else {
    		base = "https://oim.grid.iu.edu/oim/";
    	}
		return base + "certificateuser?id=" + rec.id;
    }
    
    //NO-AC 
    //return true for success
    private void request(Integer vo_id, CertificateRequestUserRecord rec, ContactRecord requester, String cn, String request_comment) throws SQLException, CertificateRequestException 
    {
		///////////////////////////////////////////////////////////////////////////////////////////
		// Check conditions & finalize DN (register contact if needed)
    	String note = "";
    
		/*
		//make sure there are no other certificate already requested or renew_requested
		CertificateRequestUserRecord existing_rec = getByDN(rec.dn);
		if(existing_rec.status.equals(CertificateRequestStatus.REQUESTED) || existing_rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			throw new CertificateRequestException("There is already another certificate request pending with the same DN. ID: U"+existing_rec.id);
		}
		*/
		
		if(auth.isUser()) {
			//for oim user
			
			//we can generate dn immediately
			X500Name name = generateDN(cn);
			rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
			
			CertificateRequestUserRecord existing_rec = getByDN(rec.dn);
			if(existing_rec == null) {
				//user is requesting another user certificate.. proceed
				note += "NOTE: Additional user certificate request\n";
			} else {
				//oops.. there is duplicate request
				throw new CertificateRequestException("There is already another user certificate with identical DN (U"+existing_rec.id+"). Please choose different CN, or contact GOC for more assistance.");
			}
		} else {
			//for guest
			
			//Find contact record with the same email address including disabled one
			ContactModel cmodel = new ContactModel(context);
			ContactRecord existing_crec = cmodel.getByemail(requester.primary_email);//this is from the form, so I just have to check against primary_email
			if(existing_crec == null) {
				//register new disabled contact
				requester.disable = true; //don't enable until the request gets approved
				requester.id = cmodel.insert(requester);
				
				//and generate dn
		    	cn = requester.name + " " + requester.id;
		    	X500Name name = generateDN(cn);
				rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
				
				note += "NOTE: User is registering OIM contact & requesting new certificate: contact id:"+requester.id+"\n";
		    	
			} else {
				//generate dn (with existing_crec id)
		    	cn = requester.name + " " + existing_crec.id;
		    	X500Name name = generateDN(cn);
				rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
				
				//find if there is any DN associated with the contact
				DNModel dnmodel = new DNModel(context);
				ArrayList<DNRecord> dnrecs = dnmodel.getEnabledByContactID(existing_crec.id);
				if(dnrecs.isEmpty()) {
					//do contact record take over
					
					//pre-registered contact - just let user associate with this contact id
					requester.id = existing_crec.id;
					requester.disable = false;
					requester.person = true;
					
					//update contact information with information that user just gave me
					cmodel.update(requester);
					
					note += "NOTE: User is claiming unused contact id: "+existing_crec.id+"\n";
				} else {
					//we can't take over contact record that already has DN attached.
					//find proper error message
					CertificateRequestUserRecord existing_rec = getByDN(rec.dn);
					if(existing_rec != null) {
						//oim cert is already associated.
						throw new CertificateRequestException("Provided email address is already associated with existing certificate (U"+existing_rec.id+"). If you are already registered to OIM, please login before making your request. Please contact GOC for more assistance.");						
					} else {
						//probably non digicert DN
						throw new CertificateRequestException("Provided email address is already associated with existing non OIM certificate. If you are already registered to OIM, please login before making your request. Please contact GOC for more assistance.");
					}
				}
			}
			
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
		
		///////////////////////////////////////////////////////////////////////////////////////////
		// Notification
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		
		//Create ra & sponsor list
		String ranames = "";
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs;
		try {
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //primary, secondary, and sponsors
					ticket.ccs.add(contactrec.primary_email);
					
					if(ranames.length() != 0) {
						ranames += ", ";
					}
					ranames += contactrec.name;
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA/sponsor information - ignoring", e1);
		}
    	
		//submit goc ticket
		VOModel vmodel = new VOModel(context);
		VORecord vrec = vmodel.get(rec.vo_id);
		
		ticket.title = "OSG:"+vrec.name+" User Certificate Request for "+requester.name;
		String auth_status = "An unauthenticated user; ";
		if(auth.isUser()) {
			auth_status = "An OIM Authenticated user; ";
		}
		ticket.description = "Dear " + ranames + " (" + vrec.name + " VO RA/Sponsors),\n\n";
		ticket.description += auth_status + requester.name + " <"+requester.primary_email+"> has requested a user certificate. ";
		ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + getTicketUrl(rec);
		ticket.description += "\n\n"+note;
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
		
		//do open ticket.
		String ticket_id = fp.open(ticket);

		//update request record with goc ticket id
		rec.goc_ticket_id = ticket_id;
		context.setComment("Opened GOC Ticket " + ticket_id);
		super.update(get(rec.id), rec);
    }
    
    
    //no-ac
    public boolean rerequest(CertificateRequestUserRecord rec) throws CertificateRequestException 
    {    	
		rec.status = CertificateRequestStatus.REQUESTED;
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
			
			//Create ra & sponsor list
			String ranames = "";
			VOContactModel model = new VOContactModel(context);
			ContactModel cmodel = new ContactModel(context);
			ArrayList<VOContactRecord> crecs;
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11)) { //primary, secondary, and sponsors
					ticket.ccs.add(contactrec.primary_email);
					
					if(ranames.length() != 0) {
						ranames += ", ";
					}
					ranames += contactrec.name;
				}
			}
			
			ContactRecord requester = cmodel.get(rec.requester_contact_id);
			//ticket.title = "User Certificate Re-request for "+requester.name;s
			String auth_status = "An unauthenticated user; ";
			if(auth.isUser()) {
				auth_status = "An OIM Authenticated user; ";
			}
			VOModel vmodel = new VOModel(context);
			VORecord vrec = vmodel.get(rec.vo_id);
			ticket.description = "Dear " + ranames + " (" + vrec.name + " VO RA/Sponsors),\n\n";
			ticket.description += auth_status + requester.name + " <"+requester.primary_email+"> has re-requested a user certificate. ";
			ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + getTicketUrl(rec);
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
