package edu.iu.grid.oim.model.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.ICertificateSigner;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class HostCertificateRequestModel extends ModelBase<CertificateRequestHostRecord> {
    static Logger log = Logger.getLogger(HostCertificateRequestModel.class);  
    //static String NOT_ISSUED_TOKEN = "__NOT_ISSUED__";

    //provide String[] with XML serialization capability
    class StringArray  {
    	private String[] strings;
    	public String get(int idx) { return strings[idx]; }
    	public void set(int idx, String str) { strings[idx] = str; }
    	public String[] getAll() { return strings; }
    	public StringArray(String xml) {
    		//deserialize from xml
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		try {
    			DocumentBuilder db = dbf.newDocumentBuilder();
    			InputStream is = new ByteArrayInputStream(xml.getBytes());
    			Document dom = db.parse(is);
    			
    			//create string array
    			Node size_nl = dom.getElementsByTagName("Size").item(0);
    			String size_str = size_nl.getTextContent();
    			int size = Integer.parseInt(size_str);
    			strings = new String[size];
    			
    			//populate data
    			NodeList string_nl = dom.getElementsByTagName("String");
    			for(int i = 0;i < string_nl.getLength(); ++i) {
    				Node n = string_nl.item(i);
    				Node null_mark = n.getAttributes().getNamedItem("null");
    				if(null_mark != null) {
    					strings[i] = null;
    				} else {
    					strings[i] = n.getTextContent();
    				}
    			}
    			
    		}catch(ParserConfigurationException pce) {
    			pce.printStackTrace();
    		}catch(SAXException se) {
    			se.printStackTrace();
    		}catch(IOException ioe) {
    			ioe.printStackTrace();
    		}
    	}
    	public StringArray(int size) {
    		strings = new String[size];
    	}
    	public String toXML() {
    		StringBuffer out = new StringBuffer();
    		out.append("<StringArray>");
    		out.append("<Size>"+strings.length+"</Size>");
    		for(String s : strings) {
    			if(s == null) {
        			out.append("<String null=\"true\"></String>");
    			} else {
    				out.append("<String>"+StringEscapeUtils.escapeXml(s)+"</String>");
    			}
    		}
    		out.append("</StringArray>");
    		return out.toString();
    	}
    	public int length() { return strings.length; }
    
    }
    
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
			throw new HostCertificateRequestException("Index is larger than the number of CSR requested");
		}
	}
	
	//NO-AC (no check fo idx out-of-bound)
	//issue idx specified certificate, and store back to DB. return pkcs7
	private String issueCertificate(CertificateRequestHostRecord rec, int idx) throws HostCertificateRequestException {
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
			throw new HostCertificateRequestException("Failed to obtain cn from given csr", e2);
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
				throw new HostCertificateRequestException("Failed to update status for certificate request: " + rec.id);
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
    	
    	GridAdminModel gmodel = new GridAdminModel(context);
    	StringArray csrs_sa = new StringArray(csrs.length);
    	int idx = 0;
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
			/*
			//make sure there is 1 and only 1 gridadmin who can approve this host
			ArrayList<Integer> gridadmins = lookupGridAdmins(cn);
			if(gridadmins.size() == 0) {
				throw new HostCertificateRequestException("No GridAdmin can approve host:" + cn);	
			}
			if(gridadmins.size() > 1) {
				throw new HostCertificateRequestException("Multiple GridAdmin can approve host:" + cn + "(must be 1)");	
			}
			*/
			
			//lookup gridadmin
			ContactRecord ga;
			try {
				ga = gmodel.getGridAdminByFQDN(cn);
			} catch (SQLException e) {
				throw new HostCertificateRequestException("Failed to lookup GridAdmin to approve host:" + cn, e);	
			}
			if(ga == null) {
				throw new HostCertificateRequestException("No GridAdmin can approve host:" + cn);	
			}
			
			//make sure single gridadmin approves all host
			if(rec.gridadmin_contact_id == null) {
				rec.gridadmin_contact_id = ga.id;
			} else {
				if(!rec.gridadmin_contact_id.equals(ga.id)) {
					throw new HostCertificateRequestException("All host must be approved by the same GridAdmin. Different for " + cn);	
				}
			}
				
			csrs_sa.set(idx++, csr_string);
    	}
    	
    	rec.csrs = csrs_sa.toXML();
    	
    	StringArray ar = new StringArray(csrs.length);
    	rec.cert_certificate = ar.toXML();
    	rec.cert_intermediate = ar.toXML();
    	rec.cert_pkcs7 = ar.toXML();
    	
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
			String url = StaticConfig.getApplicationBase() + "/certificate?type=host&id=" + rec.id;
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
