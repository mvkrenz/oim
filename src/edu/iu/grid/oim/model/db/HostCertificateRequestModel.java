package edu.iu.grid.oim.model.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.xpath.XPath;

import javax.xml.xpath.XPathExpressionException;

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
import edu.iu.grid.oim.model.CertificateRequestException;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.ICertificateSigner;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class HostCertificateRequestModel extends CertificateRequestModelBase<CertificateRequestHostRecord> {
    static Logger log = Logger.getLogger(HostCertificateRequestModel.class);  
    
	private UserContext contect;
    public HostCertificateRequestModel(UserContext _context) {
		super(_context, "certificate_request_host");
		context = _context;
	}
    
	//NO-AC
	public CertificateRequestHostRecord get(int id) throws SQLException {
		CertificateRequestHostRecord rec = null;
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name+ " WHERE id = " + id)) {
	    	rs = stmt.getResultSet();
	    	if(rs.next()) {
	    		rec = new CertificateRequestHostRecord(rs);
			}
	    }	
	    stmt.close();
	    conn.close();
	    return rec;
	}
	
	//return requests that I have submitted
	public ArrayList<CertificateRequestHostRecord> getMine(Integer id) throws SQLException {
		ArrayList<CertificateRequestHostRecord> ret = new ArrayList<CertificateRequestHostRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
		stmt.execute("SELECT * FROM "+table_name + " WHERE requester_contact_id = " + id + " OR gridadmin_contact_id = "+id);	
    	rs = stmt.getResultSet();
    	while(rs.next()) {
    		ret.add(new CertificateRequestHostRecord(rs));
    	}
	    stmt.close();
	    conn.close();
	    return ret;
	}

	@Override
	public Boolean hasLogAccess(XPath xpath, Document doc)
			throws XPathExpressionException {
		// TODO Auto-generated method stub
		return null;
	}  

	//NO-AC 
	//return pem encoded pkcs7
	public String getPkcs7(CertificateRequestHostRecord rec, int idx) throws CertificateRequestException {
		StringArray pkcs7s = new StringArray(rec.cert_pkcs7);
		if(pkcs7s.length() > idx) {
			String pkcs7 = pkcs7s.get(idx);
			if(pkcs7 == null) {
				if(rec.status.equals(CertificateRequestStatus.APPROVED) || rec.status.equals(CertificateRequestStatus.ISSUING) ) {	
					pkcs7 = issueCertificate(rec, idx);
				}
			}
			return pkcs7;
		} else {
			throw new CertificateRequestException("Index is larger than the number of CSR requested");
		}
	}
	
	//NO-AC (no check fo idx out-of-bound)
	//issue idx specified certificate, and store back to DB. return pkcs7
	private String issueCertificate(CertificateRequestHostRecord rec, int idx) throws CertificateRequestException {
		StringArray csrs = new StringArray(rec.csrs);
		String csr = csrs.get(idx);

		//pull CN
		String cn;
		try {
			PKCS10CertificationRequest pkcs10 = new PKCS10CertificationRequest(Base64.decode(csr));
			X500Name name = pkcs10.getSubject();
			RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
			cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
		} catch (IOException e2) {
			throw new CertificateRequestException("Failed to obtain cn from given csr", e2);
		}
		
		CertificateManager cm = new CertificateManager();
		try {
			ICertificateSigner.Certificate cert = cm.signHostCertificate(csr, cn);
			
			StringArray cert_certificates = new StringArray(rec.cert_certificate);
			cert_certificates.set(idx, cert.certificate);
			rec.cert_certificate = cert_certificates.toXML();
			
			StringArray cert_intermediates = new StringArray(rec.cert_intermediate);
			cert_intermediates.set(idx, cert.intermediate);
			rec.cert_intermediate = cert_intermediates.toXML();
			
			StringArray cert_pkcs7s = new StringArray(rec.cert_pkcs7);
			cert_pkcs7s.set(idx, cert.pkcs7);
			rec.cert_pkcs7 = cert_pkcs7s.toXML();
			
			try {
				//if all certificate is issued, change status to ISSUED
				boolean issued = true;
				for(String s : cert_pkcs7s.getAll()) {
					if(s == null) {
						issued = false;
						break;
					}
				}
				if(issued) {
					rec.status = CertificateRequestStatus.ISSUED;
				} else {
					rec.status = CertificateRequestStatus.ISSUING;	//TODO - I am not sure if this really makes sense.
				}
				
				HostCertificateRequestModel.super.update(get(rec.id), rec);
			} catch (SQLException e) {
				throw new CertificateRequestException("Failed to update status for certificate request: " + rec.id);
			}
			
			//update ticket
			Footprints fp = new Footprints(context);
			FPTicket ticket = fp.new FPTicket();
			Authorization auth = context.getAuthorization();
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				ticket.description = contact.name + " has issued certificate.";
			} else {
				ticket.description = "Someone with IP address: " + context.getRemoteAddr() + " has issued certificate";
			}
			ticket.status = "Resolved";
			fp.update(ticket, rec.goc_ticket_id);
			
			return cert.pkcs7;
			
		} catch (ICertificateSigner.CertificateProviderException e1) {
			log.error("Failed to sign certificate", e1);
			throw new CertificateRequestException("Failed to sign certificate", e1);
		}
	
	}
	
	//NO-AC
	//return true if success
    public void approve(CertificateRequestHostRecord rec) throws CertificateRequestException 
    {
		rec.status = CertificateRequestStatus.APPROVED;
		try {
			//context.setComment("Certificate Approved");
			super.update(get(rec.id), rec);
		} catch (SQLException e) {
			log.error("Failed to approve host certificate request: " + rec.id);
			throw new CertificateRequestException("Failed to update certificate request record");
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
    }
    
    //NO-AC (for authenticated user)
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord requestAsUser(String[] csrs, ContactRecord requester) throws CertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		Date current = new Date();
    	rec.requester_contact_id = requester.id;
	 	rec.requester_name = requester.name;
    	rec.requester_email = requester.primary_email;
    	rec.requester_phone = requester.primary_phone;
    	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester.name;
		ticket.email = requester.primary_email;
		ticket.phone = requester.primary_phone;
		ticket.title = "Host Certificate Request by " + requester.name + "(OIM user)";
		ticket.metadata.put("SUBMITTER_NAME", requester.name);
		
    	return request(csrs, rec, ticket);
    }
    
    //NO-AC (for guest user)
	//return request record if successful, otherwise null
    public CertificateRequestHostRecord requestAsGuest(String[] csrs, String requester_name, String requester_email, String requester_phone) throws CertificateRequestException 
    {
    	CertificateRequestHostRecord rec = new CertificateRequestHostRecord();
		Date current = new Date();
	 	rec.requester_name = requester_name;
    	rec.requester_email = requester_email;
    	rec.requester_phone = requester_phone;
    	
		Footprints fp = new Footprints(context);
		FPTicket ticket = fp.new FPTicket();
		ticket.name = requester_name;
		ticket.email = requester_email;
		ticket.phone = requester_phone;
		ticket.title = "Host Certificate Request by " + requester_name + "(Guest)";
		ticket.metadata.put("SUBMITTER_NAME", requester_name);
		
    	return request(csrs, rec, ticket);
    }
    
    //NO-AC
	//return request record if successful, otherwise null (guest interface)
    private CertificateRequestHostRecord request(String[] csrs, CertificateRequestHostRecord rec, FPTicket ticket) throws CertificateRequestException 
    {
		Date current = new Date();
		rec.request_time = new Timestamp(current.getTime());
		rec.status = CertificateRequestStatus.REQUESTED;
    	rec.gridadmin_contact_id = null;
    	
    	GridAdminModel gmodel = new GridAdminModel(context);
    	StringArray csrs_sa = new StringArray(csrs.length);
    	StringArray cns_sa = new StringArray(csrs.length);
    	int idx = 0;
    	for(String csr_string : csrs) {
    		String cn;
			try {
	    		PKCS10CertificationRequest csr = new PKCS10CertificationRequest(Base64.decode(csr_string));

	    		//pull CN
	    		X500Name name = csr.getSubject();
	    		RDN[] cn_rdn = name.getRDNs(BCStyle.CN);
	    		cn = cn_rdn[0].getFirst().getValue().toString(); //wtf?
	    		
	    		cns_sa.set(idx, cn);
			} catch (IOException e) {
				log.error("Failed to base64 decode CSR", e);
				throw new CertificateRequestException("Failed to base64 decode CSR:"+csr_string, e);
			} catch (NullPointerException e) {
				log.error("(probably) couldn't find CN inside the CSR:"+csr_string, e);
				throw new CertificateRequestException("Failed to base64 decode CSR", e);	
			} catch(Exception e) {
				throw new CertificateRequestException("Failed to decode CSR", e);
			}
			
			//lookup gridadmin
			ContactRecord ga;
			try {
				ga = gmodel.getGridAdminByFQDN(cn);
			} catch (SQLException e) {
				throw new CertificateRequestException("Failed to lookup GridAdmin to approve host:" + cn, e);	
			}
			if(ga == null) {
				throw new CertificateRequestException("No GridAdmin can approve host:" + cn);	
			}
			
			//make sure single gridadmin approves all host
			if(rec.gridadmin_contact_id == null) {
				rec.gridadmin_contact_id = ga.id;
			} else {
				if(!rec.gridadmin_contact_id.equals(ga.id)) {
					throw new CertificateRequestException("All host must be approved by the same GridAdmin. Different for " + cn);	
				}
			}
				
			csrs_sa.set(idx++, csr_string);
    	}
    	
    	rec.csrs = csrs_sa.toXML();
    	rec.cns = cns_sa.toXML();
    	
    	StringArray ar = new StringArray(csrs.length);
    	rec.cert_certificate = ar.toXML();
    	rec.cert_intermediate = ar.toXML();
    	rec.cert_pkcs7 = ar.toXML();
    	
    	try {
    		//insert request record
			Integer request_id = super.insert(rec);
			
			ContactModel cmodel = new ContactModel(context);
			ContactRecord ga = cmodel.get(rec.gridadmin_contact_id);
			ticket.description = "Dear " + ga.name + "; the GridAdmin, \n";
			ticket.description += "Host certificate request has been submitted.";
			String url = StaticConfig.getApplicationBase() + "/certificatehost?id=" + rec.id;
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
			Footprints fp = new Footprints(context);
			String ticket_id = fp.open(ticket);

			//update request record with goc ticket id
			rec.goc_ticket_id = ticket_id;
			context.setComment("Opened GOC Ticket " + ticket_id);
			super.update(get(request_id), rec);
		} catch (SQLException e) {
			throw new CertificateRequestException("Failed to insert host certificate request record");	
		}
		
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
}
