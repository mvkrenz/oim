package edu.iu.grid.oim.model.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.util.Store;
import org.bouncycastle.util.encoders.Base64;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.BCrypt;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.StaticConfig;
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
import edu.iu.grid.oim.model.db.record.RecordBase;
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
		/*
		if(auth.isGuest()) {
			//right now, guest can't view certificate requests
			//TODO - we need to allow guest user to somehow gain access to ones own request
		} else if(auth.isUser()) {
			//super ra can see all requests
			if(auth.allows("admin_all_user_cert_requests")) return true;
			
			//is user the requester?
			ContactRecord contact = auth.getContact();
			if(rec.requester_contact_id.equals(contact.id)) return true;
			
			//ra or sponsor for specified vo can view it
			VOContactModel model = new VOContactModel(context);
			ContactModel cmodel = new ContactModel(context);
			ArrayList<VOContactRecord> crecs;
			try {
				crecs = model.getByVOID(rec.vo_id);
				for(VOContactRecord crec : crecs) {
					ContactRecord contactrec = cmodel.get(crec.contact_id);
					if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(1)) { //primary
						if(contactrec.id.equals(contact.id)) return true;
					}
					
					if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(3)) { //sponsor
						if(contactrec.id.equals(contact.id)) return true;
					}
				}
			} catch (SQLException e1) {
				log.error("Failed to lookup RA/sponsor information", e1);
			}
		}
		return false;
		*/
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
	
	//true if user can approve request
	public boolean canApprove(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
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
			//rec.status.equals(CertificateRequestStatus.APPROVED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				//requester can cancel one's own request
				if(rec.requester_contact_id == contact.id) return true;
				
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
	
	public boolean canRequestRenew(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED)) {
			if(auth.isUser()) {
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
				/*
				if(auth.allows("revoke_all_certificate")) {
					return true;
				}
				*/
				
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
	
	//convert comma delimited DN (RFC1779) to apache format (delimited by /)
	private String RFC1779_to_ApacheDN(String dn) {
		String tokens[] = dn.split(",");
		String out = StringUtils.join(tokens, "/");
		return "/"+out;
	}
	
	//convert apache format (delimited by /) to comma delimited DN (RFC1779)
	private String ApacheDN_to_RFC1779(String dn) {
		String tokens[] = dn.split("/");
		tokens = (String[]) ArrayUtils.remove(tokens, 0);//remove first one which is empty
		String out = StringUtils.join(tokens, ",");
		return out;
	}
	
	
	//NO-AC
	//return true if success
	public boolean approve(CertificateRequestUserRecord rec) {
		rec.status = CertificateRequestStatus.APPROVED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to approve user certificate request: " + rec.id);
			return false;
		}
		
		try {
			//Then insert a new DN record
			DNRecord dnrec = new DNRecord();
			dnrec.contact_id = rec.requester_contact_id;
			dnrec.dn_string = rec.dn;
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
			ticket.description += "Your user certificate request has been approved. Please issue & download your certificate.\n\n";
			ticket.description += "> " + context.getComment();
			ticket.nextaction = "Requester to download certificate"; // NAD will be set 7 days from today by default
			fp.update(ticket, rec.goc_ticket_id);
			
		} catch (SQLException e) {
			log.error("Failed to associate new DN with requeter contact", e);
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
		} catch (SQLException e) {
			log.error("Failed to cancel user certificate request:" + rec.id);
			return false;
		}
		
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
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean reject(CertificateRequestUserRecord rec) {
		rec.status = CertificateRequestStatus.REJECTED;
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
		
    	//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canRequestUserCert()) {
    		throw new CertificateRequestException("Can't request any more user certificate.");
    	}
    	
		rec.status = CertificateRequestStatus.RENEW_REQUESTED;
		try {
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to request user certificate request renewal: " + rec.id);
			throw new CertificateRequestException("Failed to update request status", e);
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has requested renewal for this certificate request.\n\n";
		ticket.description += "> " + context.getComment();
		ticket.nextaction = "RA/Sponsor to verify&approve"; //nad will be set to 7 days from today by default
		
		//Update CC ra & sponsor (it might have been changed since last time request was made)
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<VOContactRecord> crecs;
		try {
			crecs = model.getByVOID(rec.vo_id);
			for(VOContactRecord crec : crecs) {
				ContactRecord contactrec = cmodel.get(crec.contact_id);
				if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(1)) { //primary
					//rec.ra_contact_id = crec.contact_id;
					ticket.ccs.add(contactrec.primary_email);
				}
				if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(3)) { //sponsor
					ticket.ccs.add(contactrec.primary_email);
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA/sponsor information - ignoring", e1);
		}
		
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
	
		Authorization auth = context.getAuthorization();
		if(auth.isUser()) {
			ContactRecord contact = auth.getContact();
			ticket.description = contact.name + " has requested revocation of this certificate request.";
		} else {
			ticket.description = "Guest user with IP:" + context.getRemoteAddr() + " has requested revocation of this certificate request.";		
		}
		ticket.nextaction = "RA to process"; //nad will be set to 7 days from today by default
		fp.update(ticket, rec.goc_ticket_id);
	}
	
	//NO-AC
	//return true if success
	public void revoke(CertificateRequestUserRecord rec) throws CertificateRequestException {
		//revoke
		CertificateManager cm = new CertificateManager();
		try {
			cm.revokeUserCertificate(rec.cert_serial_id);
		} catch (CertificateProviderException e1) {
			log.error("Failed to revoke user certificate", e1);
			throw new CertificateRequestException("Failed to revoke user certificate", e1);
		}	
		
		rec.status = CertificateRequestStatus.REVOKED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to update user certificate status: " + rec.id);
			throw new CertificateRequestException("Failed to update user certificate status", e);
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has revoked this certificate.";
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
	}

	// NO-AC
	// return true if success
	public void startissue(final CertificateRequestUserRecord rec, final String password) throws CertificateRequestException {
		
		//verify passphrase if necessary
		if(rec.requester_passphrase != null) {
			String hashed = BCrypt.hashpw(password, rec.requester_passphrase_salt);
			if(!hashed.equals(rec.requester_passphrase)) {
				throw new CertificateRequestException("Failed to match passphrase.");
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
			public void failed(String message, Throwable e) {
				log.error(message, e);
				rec.status = CertificateRequestStatus.FAILED;
				try {
					context.setComment(message + " :: " + e.getMessage());
					CertificateRequestUserModel.super.update(get(rec.id), rec);
				} catch (SQLException e1) {
					log.error("Failed to update request status while processing failed condition :" + message, e1);
				}
			}
			public void run() {
				try {
					String dn = ApacheDN_to_RFC1779(rec.dn);
					log.debug("RF1779 dn: " + dn);
					X500Name name = new X500Name(dn);
					RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
					String cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
					
					//if csr is not set, we need to create one and private key for user
					if (rec.csr == null) {
						GenerateCSR csrgen = new GenerateCSR(name);
						rec.csr = csrgen.getCSR();
						context.setComment("Generated CSR and private key");
						CertificateRequestUserModel.super.update(get(rec.id),rec);
	
						// store private key in memory to be used to create pkcs12 later
						HttpSession session = context.getSession();
						session.setAttribute("PRIVATE_USER:" + rec.id, csrgen.getPrivateKey());
						session.setAttribute("PASS_USER:" + rec.id, password);
					}
					
					//now we can sign it
					CertificateManager cm = new CertificateManager();
					ICertificateSigner.Certificate cert = cm.signUserCertificate(rec.csr, cn);
					rec.cert_certificate = cert.certificate;
					rec.cert_intermediate = cert.intermediate;
					rec.cert_pkcs7 = cert.pkcs7;
					rec.cert_serial_id = cert.serial;
					
					//all done at this point
					rec.status = CertificateRequestStatus.ISSUED;
					context.setComment("Certificate has been issued by signer");
					CertificateRequestUserModel.super.update(get(rec.id), rec);
	
					// update ticket
					Footprints fp = new Footprints(context);
					FPTicket ticket = fp.new FPTicket();
					Authorization auth = context.getAuthorization();
					if(auth.isUser()) {
						ContactRecord contact = auth.getContact();
						ticket.description = contact.name + " has issued certificate.";
					} else {
						ticket.description = "Guest with IP:" + context.getRemoteAddr() + " has issued certificate.";
					}
					ticket.status = "Resolved";
					fp.update(ticket, rec.goc_ticket_id);
					
				} catch (ICertificateSigner.CertificateProviderException e) {
					failed("Failed to sign certificate -- CertificateProviderException ", e);
				} catch(Exception e) {
					failed("Failed to sign certificate -- unhandled", e);	
				}
			}
		}).start();
	}

	public PrivateKey getPrivateKey(Integer id) {
		HttpSession session = context.getSession();
		return (PrivateKey)session.getAttribute("PRIVATE_USER:"+id);	
	}
	public String getPassword(Integer id) {
		HttpSession session = context.getSession();
		return (String)session.getAttribute("PASS_USER:"+id);		
	}
	
	//return null if unsuccessful - errors are logged
	public KeyStore getPkcs12(CertificateRequestUserRecord rec) {			
		//pull certificate chain from pkcs7

		try {
			//need to strip first and last line (-----BEGIN PKCS7-----, -----END PKCS7-----)
			String []lines = rec.cert_pkcs7.split("\n");
			String payload = "";
			for(String line : lines) {
				if(line.startsWith("-----")) continue;
				payload += line;
			}
			
			//convert cms to certificate chain
			CMSSignedData cms = new CMSSignedData(Base64.decode(payload));
			Store s = cms.getCertificates();
			Collection collection = s.getMatches(null);
			java.security.cert.Certificate[] chain = new java.security.cert.Certificate[collection.size()];
			Iterator itr = collection.iterator(); 
			int i = 0;
		    CertificateFactory cf = CertificateFactory.getInstance("X.509"); 
			while(itr.hasNext()) {
				X509CertificateHolder it = (X509CertificateHolder)itr.next();
				Certificate c = it.toASN1Structure();
				
				//convert to java.security certificate
			    InputStream is1 = new ByteArrayInputStream(c.getEncoded()); 
				chain[i++] = cf.generateCertificate(is1);
			}
			
			HttpSession session = context.getSession();
			String password = getPassword(rec.id);
			
			KeyStore p12 = KeyStore.getInstance("PKCS12");
			p12.load(null, null);  //not sure what this does.
			PrivateKey private_key = getPrivateKey(rec.id);
			//p12.setKeyEntry("USER"+rec.id, private_key.getEncoded(), chain); 
			
			/*
			//DEBUG -- fake it to test
			password = "password";
			try {
				GenerateCSR gcsr = new GenerateCSR(
							new X500Name("CN=\"Soichi Hayashi/emailAddress=hayashis@indiana.edu\", OU=PKITesting, O=OSG, L=Bloomington, ST=IN, C=United States"));
		    	private_key = gcsr.getPrivateKey();
				
			} catch (OperatorCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/

			
			p12.setKeyEntry("USER"+rec.id, private_key, password.toCharArray(), chain); 
			//p12.setKeyEntry("USER"+rec.id, private_key, null, chain); 
			
			return p12;
		} catch (IOException e) {
			log.error("Failed to get encoded byte array from bouncy castle certificate.");
		} catch (CertificateException e) {
			log.error("Failed to generate java security certificate from byte array");
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	//return requests that I have submitted
	public ArrayList<CertificateRequestUserRecord> getMine(Integer id) throws SQLException {
		ArrayList<CertificateRequestUserRecord> ret = new ArrayList<CertificateRequestUserRecord>();
		
		//list all vo that user is ra of
		HashSet<Integer> vo_ids = new HashSet<Integer>();
		VOContactModel model = new VOContactModel(context);
		ArrayList<VOContactRecord> crecs;
		try {
			for(VOContactRecord crec : model.getByContactID(id)) {
				if(crec.contact_type_id.equals(11) && //RA
						(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2))) { //primary or secondary
					vo_ids.add(crec.vo_id);	
				}
			}
		} catch (SQLException e1) {
			log.error("Failed to lookup RA/sponsor information", e1);
		}	
		String vo_ids_str = StringUtils.join(vo_ids, ",");
		
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		if(vo_ids.size() == 0) {
			stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id = " + id);	
		} else {
			stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id = " + id + " OR vo_id IN ("+vo_ids_str+")");	
		}
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		ret.add(new CertificateRequestUserRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return ret;
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
    
    public CertificateRequestUserRecord requestUsertWithNOCSR(Integer vo_id, ContactRecord requester) throws SQLException, CertificateRequestException {
    	
    	//TODO -- check access

		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();
    	request(vo_id, rec, requester);
    	return rec;
    }
       
    //returns insertec request record if successful. if not, null
    public CertificateRequestUserRecord requestGuestWithNOCSR(Integer vo_id, ContactRecord requester, String passphrase) throws SQLException, CertificateRequestException { 
    	//TODO -- check access
 
		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();		
		String salt = BCrypt.gensalt(12);//let's hard code this for now..
		rec.requester_passphrase_salt = salt;
		rec.requester_passphrase = BCrypt.hashpw(passphrase, salt);
    	request(vo_id, rec, requester);
    	return rec;
    } 
    
    private X500Name generateDN(String fullname, Integer serial) {
        X500NameBuilder x500NameBld = new X500NameBuilder(BCStyle.INSTANCE);

        //DigiCert overrides the DN, so none of these matters - except CN which is used to send common_name parameter
        //We are creating this so that we can create private key
        x500NameBld.addRDN(BCStyle.DC, "com");
        x500NameBld.addRDN(BCStyle.DC, "DigiCert-Grid");
        x500NameBld.addRDN(BCStyle.OU, "People");   
        x500NameBld.addRDN(BCStyle.CN, fullname + " " + serial.toString()); //don't use "," or "/" which is used for DN delimiter
        
        return x500NameBld.build();
    }
    
    /*
    private Integer getMaxID() throws SQLException {
		CertificateRequestUserRecord rec = null;
		ResultSet rs = null;
		Integer max = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT max(id) FROM "+table_name)) {
	    	rs = stmt.getResultSet();
	    	if(rs.next()) {
	    		max = rs.getInt(1);
			}
	    }	
	    stmt.close();
	    conn.close();
	    return max;
    }
    */
    
    //NO-AC NO-QUOTA
    //return true for success
    private void request(Integer vo_id, CertificateRequestUserRecord rec, ContactRecord requester) throws SQLException, CertificateRequestException 
    {
    	//check quota
    	CertificateQuotaModel quota = new CertificateQuotaModel(context);
    	if(!quota.canRequestUserCert()) {
    		throw new CertificateRequestException("Can't request any more user certificate.");
    	}
    	
		Date current = new Date();
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		
		X500Name name = generateDN(requester.name, requester.id);
		rec.dn = RFC1779_to_ApacheDN(name.toString());
		rec.requester_contact_id = requester.id;
		rec.vo_id = vo_id;
		
		String ranames = "";
		//CC ra & sponsor
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
				
		context.setComment("Making Request for " + requester.name);
    	super.insert(rec);
		quota.incrementUserCertRequest();
    	
		//submit goc ticket
		ticket.title = "User Certificate Request for "+requester.name;
		String auth_status = "An unauthenticated user; ";
		if(auth.isUser()) {
			auth_status = "An OIM Authenticated user; ";
		}
		VOModel vmodel = new VOModel(context);
		VORecord vrec = vmodel.get(rec.vo_id);
		ticket.description = "Dear " + ranames + " (" + vrec.name + " VO RA),\n\n";
		ticket.description += auth_status + requester.name + " <"+requester.primary_email+"> has requested a user certificate. ";
		String url = StaticConfig.getApplicationBase() + "/certificateuser?id=" + rec.id;
		ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + url;
		/*
		if(StaticConfig.isDebug()) {
			ticket.assignees.add("hayashis");
		} else {
			ticket.assignees.add("adeximo");
		}
		*/
		ticket.assignees.add(StaticConfig.conf.getProperty("certrequest.user.assignee"));
		
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
    
	//prevent low level access - please use model specific actions
    @Override
    public Integer insert(RecordBase rec) throws SQLException
    { 
    	throw new UnsupportedOperationException("Please use model specific actions instead (request, approve, reject, etc..)");
    }
    @Override
    public void update(RecordBase oldrec, RecordBase newrec) throws SQLException
    {
    	throw new UnsupportedOperationException("Please use model specific actions insetead (request, approve, reject, etc..)");
    }
    @Override
    public void remove(RecordBase rec) throws SQLException
    {
    	throw new UnsupportedOperationException("disallowing remove cert request..");
    }
    
}
