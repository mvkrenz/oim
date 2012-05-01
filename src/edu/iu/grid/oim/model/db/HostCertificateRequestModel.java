package edu.iu.grid.oim.model.db;

import java.io.IOException;
import java.security.KeyStore;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.SSLEngineResult.Status;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.ICertificateSigner;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;


public class HostCertificateRequestModel extends ModelBase<CertificateRequestHostRecord> {
    static Logger log = Logger.getLogger(HostCertificateRequestModel.class);  
    static String NOT_ISSUED_TOKEN = "__NOT_ISSUED__";
    
    public class HostCertificateRequestException extends Exception {
		private static final long serialVersionUID = 1L;
		public HostCertificateRequestException(String message) {
    		super(message);
    	}
    	public HostCertificateRequestException(String message, Exception e) {
    		super(message, e);
    	}
    }
    
	private UserContext contect;
    public HostCertificateRequestModel(UserContext _context) {
		super(_context, "certificate_request_host");
		context = _context;
	}
    
    //return list of gridadmins who can approve certificate request for given domain
    public ArrayList<Integer> lookupGridAdmins(String fqdn) {
    	ArrayList<Integer> gridadmins = new ArrayList<Integer>();
    	
    	//TODO ---
    	gridadmins.add(238);//Soichi
    	
    	return gridadmins;
    	
    }
    
	//NO-AC
	public CertificateRequestHostRecord get(int id) throws SQLException {
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE id = " + id)) {
	    	rs = stmt.getResultSet();
	    	if(rs.next()) {
	    		return new CertificateRequestHostRecord(rs);
			}
	    }	
	    return null;
	}

	@Override
	public Boolean hasLogAccess(XPath xpath, Document doc)
			throws XPathExpressionException {
		// TODO Auto-generated method stub
		return null;
	}  

	//NO-AC 
	//return pem encoded pkcs7
	public String getPkcs7(CertificateRequestHostRecord rec, int idx) throws HostCertificateRequestException {
		String [] pkcs7s = rec.cert_pkcs7.split("\n");
		if(pkcs7s.length > idx) {
			String pkcs7 = pkcs7s[idx];
			if(pkcs7.equals(NOT_ISSUED_TOKEN) && rec.status.equals(CertificateRequestStatus.APPROVED)) {
				pkcs7 = issueCertificate(rec, idx);	
			}
			return pkcs7;
		} else {
			throw new HostCertificateRequestException("Index is larger than the number of CSR requested");
		}
	}
	
	//NO-AC (no check fo idx out-of-bound)
	//issue idx specified certificate, and store back to DB. return pkcs7
	private String issueCertificate(CertificateRequestHostRecord rec, int idx) throws HostCertificateRequestException {
		String [] csrs = rec.csrs.split("\n");
		String csr = csrs[idx];
		
		CertificateManager cm = new CertificateManager();
		try {
			ICertificateSigner.Certificate cert = cm.signHostCertificate(csr);
			
			String [] cert_certificates = rec.cert_certificate.split("\n");
			cert_certificates[idx] = cert.certificate;
			rec.cert_certificate = StringUtils.join(cert_certificates, "\n");
			
			String [] cert_intermediates = rec.cert_intermediate.split("\n");
			cert_intermediates[idx] = cert.intermediate;
			rec.cert_intermediate = StringUtils.join(cert_intermediates, "\n");
			
			String [] cert_pkcs7s = rec.cert_pkcs7.split("\n");
			cert_pkcs7s[idx] = cert.pkcs7;
			rec.cert_pkcs7 = StringUtils.join(cert_pkcs7s, "\n");
			
			try {
				//context.setComment("Certificate Approved");
				rec.status = CertificateRequestStatus.ISSUED;
				HostCertificateRequestModel.super.update(get(rec.id), rec);
			} catch (SQLException e) {
				throw new HostCertificateRequestException("Failed to update status for certificate request: " + rec.id);
			}
			
			//update ticket
			Authorization auth = context.getAuthorization();
			ContactRecord contact = auth.getContact();
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.description = contact.name + " has issued certificate.";
			ticket.status = "Resolved";
			fp.update(ticket, rec.goc_ticket_id);
			
			return cert.pkcs7;
			
		} catch (ICertificateSigner.CertificateProviderException e1) {
			throw new HostCertificateRequestException("Failed to sign certificate", e1);
		}
	
	}
	
	//NO-AC
	//return true if success
    public boolean approve(CertificateRequestHostRecord rec) throws HostCertificateRequestException 
    {
		rec.status = CertificateRequestStatus.APPROVED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to approve host certificate request: " + rec.id);
			return false;
		}
		
		//update ticket
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = "Dear " + rec.requester_name + ",\n\n";
		ticket.description += "Your host certificate request has been approved. Please issue & download your certificate";
		ticket.nextaction = "Requester to download certificate";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		fp.update(ticket, rec.goc_ticket_id);
	
		return true;
    }
    
    //NO-AC
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord request(String[] csrs, String requester_name, String requester_email, String requester_phone) throws HostCertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		Date current = new Date();
    	rec.requester_name = requester_name;
    	rec.requester_email = requester_email;
    	rec.requester_phone = requester_phone;
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;
    	rec.gridadmin_contact_id = null;
    	rec.csrs = "";
    	rec.cert_certificate = "";
    	rec.cert_intermediate = "";
    	rec.cert_pkcs7 = "";
    	
    	for(String csr_string : csrs) {
    		String cn;
			try {
	    		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(Base64.decode(csr_string));

	    		//pull CN
	    		X500Name name = csr.getSubject();
	    		RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
	    		cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
			} catch (IOException e) {
				log.error("Failed to base64 decode CSR", e);
				throw new HostCertificateRequestException("Failed to base64 decode CSR:"+csr_string, e);
			} catch (NullPointerException e) {
				log.error("(probably) couldn't find CN inside the CSR:"+csr_string, e);
				throw new HostCertificateRequestException("Failed to base64 decode CSR", e);	
			}
			
			//make sure there is 1 and only 1 gridadmin who can approve this host
			ArrayList<Integer> gridadmins = lookupGridAdmins(cn);
			if(gridadmins.size() == 0) {
				throw new HostCertificateRequestException("No GridAdmin can approve host:" + cn);	
			}
			if(gridadmins.size() > 1) {
				throw new HostCertificateRequestException("Multiple GridAdmin can approve host:" + cn + "(must be 1)");	
			}
			
			//make sure single gridadmin approves all host
			if(rec.gridadmin_contact_id == null) {
				rec.gridadmin_contact_id = gridadmins.get(0);
			} else {
				if(!rec.gridadmin_contact_id.equals(gridadmins.get(0))) {
					throw new HostCertificateRequestException("All host must be approved by the same GridAdmin. Different for " + cn);	
				}
			}
				
			rec.csrs += csr_string + "\n";
	    	rec.cert_certificate += NOT_ISSUED_TOKEN;
	    	rec.cert_intermediate += NOT_ISSUED_TOKEN;
	    	rec.cert_pkcs7 += NOT_ISSUED_TOKEN;
    	}
    	
    	try {
    		//insert request record
			Integer request_id = super.insert(rec);
			
			//open goc ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			ticket.name = requester_name;
			ticket.email = requester_email;
			ticket.phone = requester_phone;
			
			ticket.title = "Host Certificate Request by " + requester_name;
			/*
			VOModel vmodel = new VOModel(context);
			VORecord vrec = vmodel.get(rec.vo_id);
			ticket.description = "Dear " + vrec.name + " VO RA,\n\n";
			*/
			
			ContactModel cmodel = new ContactModel(context);
			ContactRecord ga = cmodel.get(rec.gridadmin_contact_id);
			ticket.description = "Dear " + ga.name + "; the GridAdmin, \n";
			ticket.description += requester_name + " <"+requester_email+"> has requested a user certificate. ";
			String url = StaticConfig.getApplicationBase() + "/certificate?type=user&id=" + rec.id;
			ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + url;
			if(StaticConfig.isDebug()) {
				ticket.assignees.add("hayashis");
			} else {
				ticket.assignees.add("adeximo");
				ticket.ccs.add(ga.primary_email);
			}
			ticket.nextaction = "RA/Sponsors to verify requester";
			Calendar nad = Calendar.getInstance();
			nad.add(Calendar.DATE, 7);
			ticket.nad = nad.getTime();
			
			//set metadata
			ticket.metadata.put("SUBMITTED_VIA", "OIM/CertManager(host)");
			if(auth.isUser()) {
				ticket.metadata.put("SUBMITTER_DN", auth.getUserDN());
			} 
			ticket.metadata.put("SUBMITTER_NAME", requester_name);
			String ticket_id = fp.open(ticket);

			//update request record with goc ticket id
			rec.goc_ticket_id = ticket_id;
			context.setComment("Opened GOC Ticket " + ticket_id);
			super.update(get(request_id), rec);
		} catch (SQLException e) {
			throw new HostCertificateRequestException("Failed to insert host certificate request record");	
		}
    	
    	/*
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		*/
		
    	return rec;
    }
    
	//determines if user should be able to view request details, logs, and download certificate (pkcs12 is session specific)
	public boolean canView(CertificateRequestHostRecord rec) {
		return true;
		/*
		if(auth.isGuest()) {
			//right now, guest can't view any certificate requests
		} else if(auth.isUser()) {
			//super ra can see all requests
			if(auth.allows("admin_all_user_cert_requests")) return true;
			
			//is user the requester?
			ContactRecord contact = auth.getContact();
			if(rec.requester_id.equals(contact.id)) return true;
			
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
	
	//true if user can approve request
	public boolean canApprove(CertificateRequestHostRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			if(auth.isUser()) {
				//grid admin can appove it
				ContactRecord user = auth.getContact();
				if(rec.gridadmin_contact_id.equals(user.id)) {
					return true;
				}
			}
		}
		return false;
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
    /*
    //Test class
    public static void main(String [] args) {
    	UserContext context = UserContext.getGuestContext();
    	HostCertificateRequestModel model = new HostCertificateRequestModel(context);
    	
    	//test request
    	String[] csrs = new String[2];
    	csrs[0] = "MIIC5DCCAcwCAQAwgZ4xCzAJBgNVBAYTAlVTMRAwDgYDVQQIEwdJbmRpYW5hMRQwEgYDVQQHEwtCbG9vbWluZ3RvbjEbMBkGA1UEChMSSW5kaWFuYSBVbml2ZXJzaXR5MQ0wCwYDVQQLEwRVSVRTMRswGQYDVQQDExJzb2ljaGkuZ3JpZC5pdS5lZHUxHjAcBgkqhkiG9w0BCQEWD2hheWFzaGlzQGl1LmVkdTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAM6TXTvVBUl2Rw1cLaJuF0zqOXxHmtizF/BRE16RxPS88AyAYgnMg5Aa+emqJXaMfeh2zXifoi0yPKsRJwztLrxSU8IXlzcUZ0mBEK+gzfK7GtFV5sRL4ecdYR1R9XVlj2iL0FpLknBJHQb9I7+WQ6rC9yhwKoH7Sm5EaNWo2ty4YVcarNw7pptizRVAUW972+jvcCNJWZyNJJtyKJOR0zkulYyXPohW5ovcT0hyCs9XTYNNg/O02fI1sEzEyOfoBNoHy06UH0L0xw9AkxwUmlzyZr+NB2OuhCEjm/QUefMgh+c8PFxbcW69M0lGR4A20ZJsd+2hui1Cz1wWfSIqqBMCAwEAAaAAMA0GCSqGSIb3DQEBBQUAA4IBAQC26RTFELEb0NhkmIarz/t1IciqbuYN1WIwfRpn5yIR7WiSquA6BwR1Ie6mpOlJNzj5PflQU1FJNeMxWUc7vFsUsupv9y1Pv0MpMXX9iSYPYjym3IA0I2D/CIdVVwpOpjTJJhCI/r5LGiZIKWxv4prjMc47ctWm8rPu1TmH3fEX8ZeQ2ZNU/VMJgymaT3CFIWanYbJnkWCFigzBkrh7aaE1zDWrDKV3EQs3N+i5NzFhM6pg6Ix/5lLs8skR/aR4v2OMI/8JawkWkgn9WqCY6dIm+1af9zikTlPRehbt4VzYsLJijOCPXkUVNb4jr2oKlBc4Vqo4OjfpakA4n6yseH0F";
    	csrs[1] = "MIIC5TCCAc0CAQAwgZ8xCzAJBgNVBAYTAlVTMRAwDgYDVQQIEwdJbmRpYW5hMRQwEgYDVQQHEwtCbG9vbWluZ3RvbjEbMBkGA1UEChMSSW5kaWFuYSBVbml2ZXJzaXR5MQ0wCwYDVQQLEwRVSVRTMRwwGgYDVQQDExNzb2ljaGkyLmdyaWQuaXUuZWR1MR4wHAYJKoZIhvcNAQkBFg9oYXlhc2hpc0BpdS5lZHUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCdA5DsTY2MkQ2LD4NktKeEeC++mkqb4zxxUQOcyNQneb0P9DY1PCo1JJgRWTTk8EbM9neR+cIezqE3eB0/iwn/lF4eG2tVIwlwST8x76YUJNdn61WephiHlX6TrBDTMz+LJqWfA3AyEXth4dS6TPEkoMtG8erfogOjb+tcATpviHFCOazvyRHIyRRxfxOYgrDfAWFDyRFTmBhzQ0U6xjmVsyfRjiPc4OvMYZqRyEmL0dlOg/V+FWciGo2AnAq2TfJTja7WI6/sBNu3G7ZltgWtx8+2eO5e4KPQOZao+gAj+YgCd/2GFllaQvtyDeUL5yXgQijDLREQ1uV9bLk9CSMTAgMBAAGgADANBgkqhkiG9w0BAQUFAAOCAQEAQSl/eQ7EJ/8QGu7Nuj7NSEDBDB6/zkCkjAlseGjwv4p0AJLuqIXbYCzYFIJfRfChXEN56aipFsoR6v5GhaZTag1yw6KIzFHl8WB4GQd9rYFhWbJIQzJib4WJ0e1M2FpFAuW6uyO9BCSXwliAdbUCh8TfyCoPfk/EHiSUnT3Yj2YDLof2Eh2v4cqhZv86AoA0rj5hlvCm/k8NMcKX2c4DrUQv5677oI0ah1gmpVfQPyrT49R6EREUmLWaJ34lgbRu/NaE0XKy8f11pll1cUBcC9AgqdLE3w09UobOpqQyPOzhiJAFIgj0FsmmO+VNu7ezROGuJZYdeMkNiNpmeDCltg==";
    	try {
			model.request(csrs, "Soichi Hayashi", "hayashis@iu.edu", "111-222-3333");
		} catch (HostCertificateRequestException e) {
			log.error("HostCertificateRequestException", e);
		}
    }
    */
}
