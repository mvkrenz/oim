package edu.iu.grid.oim.model.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.HashHelper;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.GenerateCSR;
import edu.iu.grid.oim.model.cert.ICertificateSigner.Certificate;
import edu.iu.grid.oim.model.cert.ICertificateSigner.CertificateProviderException;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class CertificateRequestUserModel extends ModelBase<CertificateRequestUserRecord> {
    static Logger log = Logger.getLogger(CertificateRequestUserModel.class);  
    
	private UserContext contect;
    public CertificateRequestUserModel(UserContext _context) {
		super(_context, "certificate_request_user");
		context = _context;
	}

	@Override
	public Boolean hasLogAccess(XPath xpath, Document doc)
			throws XPathExpressionException {
		// TODO Auto-generated method stub
		return null;
	}  
	
	//determines if user should be able to view request details and logs
	public boolean canView(CertificateRequestUserRecord rec) {
		if(auth.isGuest()) {
			//all guest see all other guest's certificates
			if(rec.requester_contact_id == null) return true;
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
	}
	
	//true if user can approve request
	public boolean canApprove(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				//super ra can see all requests
				if(auth.allows("admin_all_user_cert_requests")) return true;
				
				//Is user RA agent for specified vo?
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
			rec.status.equals(CertificateRequestStatus.APPROVED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				
				//requester can cancel one's own request
				if(rec.requester_contact_id == contact.id) return true;
				
				//super ra can cancel all requests
				if(auth.allows("admin_all_user_cert_requests")) return true;
				
				//ra can cancel
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
			//revocation request is only for guest
			if(auth.isGuest()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean canRevoke(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.ISSUED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			//super ra can admin all requests
			if(auth.allows("admin_all_user_cert_requests")) return true;
			
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
					ContactRecord contactrec = cmodel.get(crec.contact_id);
					if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(1)) { //primary
						if(contactrec.id.equals(contact.id)) return true;
					}
				}
			} catch (SQLException e1) {
				log.error("Failed to lookup RA/sponsor information", e1);
			}
		}
		return false;
	}
	
	public boolean canIssue(CertificateRequestUserRecord rec) {
		if(!canView(rec)) return false;
		
		if(	rec.status.equals(CertificateRequestStatus.APPROVED) ||
			rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			//super ra can issue certificate
			if(auth.allows("admin_all_user_cert_requests")) return true;
			
			//requester oneself can issue
			ContactRecord contact = auth.getContact();
			if(rec.requester_contact_id.equals(contact.id)) return true;
		}
		return false;
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
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = "Dear " + rec.requester_name + ",\n\n";
		ticket.description += "Your user certificate request has been approved. Please issue & download your certificate";
		ticket.nextaction = "Requester to download certificate";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		fp.update(ticket, rec.goc_ticket_id);
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean cancel(CertificateRequestUserRecord rec) {
		rec.status = CertificateRequestStatus.CANCELED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to cancel user certificate request:" + rec.id);
			return false;
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has canceled this certificate request.";
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
		ticket.description = contact.name + " has rejected this certificate request.";
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean requestRenew(CertificateRequestUserRecord rec) {
		rec.status = CertificateRequestStatus.RENEW_REQUESTED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to request user certificate request renewal: " + rec.id);
			return false;
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has requested renewal for this certificate request.";
		ticket.nextaction = "RA/Sponsor to verify&approve";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		fp.update(ticket, rec.goc_ticket_id);
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean requestRevoke(CertificateRequestUserRecord rec) {
		rec.status = CertificateRequestStatus.REVOCATION_REQUESTED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to request revocation of user certificate: " + rec.id);
			return false;
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has requested recocation of this certificate request.";
		ticket.nextaction = "RA to process";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		fp.update(ticket, rec.goc_ticket_id);
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean revoke(CertificateRequestUserRecord rec) {
		
		//TODO revoke certificate
		
		
		rec.status = CertificateRequestStatus.REVOKED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to revoke user certificate: " + rec.id);
			return false;
		}
		
		Authorization auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.description = contact.name + " has revoked this certificate.";
		ticket.status = "Resolved";
		fp.update(ticket, rec.goc_ticket_id);
		
		return true;
	}
	
	//NO-AC
	//return true if success
	public boolean startissue(final CertificateRequestUserRecord rec, final String password) {
		
		try {
			//mark the requet as "issuing.."
			rec.status = CertificateRequestStatus.ISSUING;
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to issue user certificate for request:" + rec.id);
			return false;
		}
		
		new Thread(new Runnable() {
			   public void run() {
				if(rec.csr == null) {
					X500Name name;
					name = new X500Name(rec.dn);
					
					GenerateCSR csrgen;
					try {
						csrgen = new GenerateCSR(name);
					} catch (Exception e) {
						log.error("Failed to generate privatekey/csr for user certificate request: " + rec.id, e);
						return;
					}
					
					rec.csr = csrgen.getCSR();
					try {
						CertificateRequestUserModel.super.update(get(rec.id), rec);
					} catch (SQLException e1) {
						log.error("Failed to store generated CSR for request: " + rec.id, e1);
						return;
					}
					
					//TODO - store private key somewhere in memory
					/*
					HttpSession session = context.getSession();
					try {
						session.setAttribute("USER"+rec.id+"_PRIVATE", csrgen.getEncryptedPrivateKey(password));
					} catch (Exception e) {
						log.error("Failed to obtain encrypted private key for user certiricate request: " + rec.id, e);
						return;
					}
					*/					
				}
				
				CertificateManager cm = new CertificateManager();
				try {
					Certificate cert = cm.signUserCertificate(rec.csr);
					rec.cert_certificate = cert.certificate;
					rec.cert_intermediate = cert.intermediate;
					rec.cert_pkcs7 = cert.pkcs7;
				} catch (CertificateProviderException e1) {
					log.error("Failed to sign certificate", e1);
					return;
				}
				
				try {
					//context.setComment("Certificate Approved");
					rec.status = CertificateRequestStatus.ISSUED;
					CertificateRequestUserModel.super.update(get(rec.id), rec);
				} catch (SQLException e) {
					log.error("Failed to update status for certificate request: " + rec.id);
					return;
				}
				
				//update ticket
				Authorization auth = context.getAuthorization();
				ContactRecord contact = auth.getContact();
				Footprints fp = new Footprints(context);
				FPTicket ticket = fp.new FPTicket();
				ticket.description = contact.name + " has issued certificate.";
				ticket.status = "Resolved";
				fp.update(ticket, rec.goc_ticket_id);
				
			}
		}).start();
		
		return true;
	}
	
	
	//NO-AC
	public CertificateRequestUserRecord get(int id) throws SQLException {
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE id = " + id)) {
	    	rs = stmt.getResultSet();
	    	if(rs.next()) {
	    		return new CertificateRequestUserRecord(rs);
			}
	    }	
	    return null;
	}
	
	//return requests that I have submitted
	public ArrayList<CertificateRequestUserRecord> getMine(Integer id) throws SQLException {
		ArrayList<CertificateRequestUserRecord> ret = new ArrayList<CertificateRequestUserRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id = " + id);	
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		ret.add(new CertificateRequestUserRecord(rs));
    	}
	    stmt.close();
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
	    return ret;
	}
	
	public class Log {
		public ContactRecord contact; //user who made this action
		public String ip; //ip address
		public String status; //new status
		public String comment; //from the log
		public Date time;
	}
	
	//NO-AC
	public ArrayList<Log> getLogs(Integer id) throws SQLException {
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	factory.setNamespaceAware(false);
    	factory.setValidating(false);
    	DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			XPath xpath = XPathFactory.newInstance().newXPath();
			ContactModel cmodel = new ContactModel(context);
			
			ArrayList<Log> logs = new ArrayList<Log>();
			LogModel model = new LogModel(context);
			Collection<LogRecord> raws = model.getByModel(CertificateRequestUserModel.class, id.toString());
			for(LogRecord raw : raws) {
				Log log = new Log();
				log.comment = raw.comment;
				log.time = raw.timestamp;
				log.ip = raw.ip;
				if(raw.contact_id != null) {
					log.contact = cmodel.get(raw.contact_id);
				}
				//parse the xml
				byte[] bArray = raw.xml.getBytes();
				ByteArrayInputStream bais = new ByteArrayInputStream(bArray);
				Document doc = builder.parse(bais);
				
				String type = (String)xpath.evaluate("//Type", doc, XPathConstants.STRING);
				if(type.equals("Insert")) {
					log.status = (String)xpath.evaluate("//Field[Name='status']/Value", doc, XPathConstants.STRING);
				} else if(type.equals("Update")) {
					log.status = (String)xpath.evaluate("//Field[Name='status']/NewValue", doc, XPathConstants.STRING);
				}
				logs.add(log);
			}
			return logs;
		} catch (ParserConfigurationException e) {
			log.error("Failed to instantiate xml parser to parse log", e);
		} catch (SAXException e) {
			log.error("Failed to parse log", e);
		} catch (IOException e) {
			log.error("Failed to parse log", e);
		} catch (XPathExpressionException e) {
			log.error("Failed to apply xpath on log", e);
		}

		return null;
	}
	
    public boolean requestWithCSR(String csr, String fullname, Integer vo_id) throws SQLException
    { 
    	//TODO
    	
    	return false;
    }
    
 
    public boolean requestWithX500(Integer vo_id, X500Name name) throws SQLException
    { 
    	//TODO -- check access
    	
    	//TODO -- check quota
    	
		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();
		ContactRecord contact = auth.getContact();
		rec.requester_contact_id = contact.id;
		rec.requester_name = contact.name;
		rec.dn = name.toString(); //RFC1779
		rec.vo_id = vo_id;
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = contact.name;
		ticket.email = contact.primary_email;
		ticket.phone = contact.primary_phone;
		
    	return request(rec, ticket);
    }
    
    //true - success
    public boolean requestGuestWithCSR(Integer vo_id, String csr, String fullname) throws SQLException
    { 
    	
    	return false;
    }
    
 
    //true - success
    public boolean requestGuestWithX500(Integer vo_id, X500Name name, String requester_passphrase, String fullname, String email, String phone) throws SQLException
    { 
    	//TODO -- check access
    	
    	//TODO -- check quota
 
		CertificateRequestUserRecord rec = new CertificateRequestUserRecord();
		rec.requester_name = fullname;
		rec.dn = name.toString(); //RFC1779
		rec.requester_passphrase = requester_passphrase;
		rec.vo_id = vo_id;
		rec.requester_name = fullname;
		
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = fullname;
		ticket.email = email;
		ticket.phone = phone;
		
    	return request(rec, ticket);
    } 
    
    //NO-AC NO-QUOTA
    private boolean request(CertificateRequestUserRecord rec, FPTicket ticket) throws SQLException 
    {
		Date current = new Date();
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;
		
		//CC ra & sponsor
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
			log.error("Failed to lookup RA/sponsor information", e1);
		}
				
		context.setComment("Making Request for " + rec.requester_name);
    	Integer request_id = super.insert(rec);
    	
		//submit goc ticket
		ticket.title = "User Certificate Request for "+rec.requester_name;
		String auth_status = "An unauthenticated user; ";
		if(auth.isUser()) {
			auth_status = "An OIM Authenticated user; ";
		}
		VOModel vmodel = new VOModel(context);
		VORecord vrec = vmodel.get(rec.vo_id);
		ticket.description = "Dear " + vrec.name + " VO RA,\n\n";
		ticket.description += auth_status + rec.requester_name + " has requested a user certificate. ";
		String url = StaticConfig.getApplicationBase() + "/certificate?type=user&id=" + rec.id;
		ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + url;
		if(StaticConfig.isDebug()) {
			ticket.assignees.add("hayashis");
		} else {
			ticket.assignees.add("adeximo");
		}
		ticket.nextaction = "RA/Sponsors to verify requester";
		Calendar nad = Calendar.getInstance();
		nad.add(Calendar.DATE, 7);
		ticket.nad = nad.getTime();
		
		//set metadata
		ticket.metadata.put("ASSOCIATED_VO_ID", vrec.id.toString());
		ticket.metadata.put("ASSOCIATED_VO_NAME", vrec.name);
		SCModel scmodel = new SCModel(context);
		SCRecord screc = scmodel.get(vrec.sc_id);
		ticket.metadata.put("SUPPORTING_SC_ID", screc.id.toString());
		ticket.metadata.put("SUPPORTING_SC_NAME", screc.name);
		ticket.metadata.put("SUBMITTED_VIA", "OIM/CertManager");
		if(auth.isUser()) {
			ticket.metadata.put("SUBMITTER_DN", auth.getUserDN());
		} 
		ticket.metadata.put("SUBMITTER_NAME", rec.requester_name);
		
		//do open ticket.
		Footprints fp = new Footprints(context);
		String ticket_id = fp.open(ticket);

		//update request record with goc ticket id
		rec.goc_ticket_id = ticket_id;
		context.setComment("Opened GOC Ticket " + ticket_id);
		super.update(get(request_id), rec);

		return true;
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
