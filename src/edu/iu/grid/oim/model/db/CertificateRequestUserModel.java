package edu.iu.grid.oim.model.db;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import sun.security.x509.X500Name;

import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.HashHelper;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.LogRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class CertificateRequestUserModel extends ModelBase<CertificateRequestUserRecord> {
    static Logger log = Logger.getLogger(CertificateRequestUserModel.class);  
	private Context contect;
    public CertificateRequestUserModel(Context _context) {
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
			if(rec.requester_contact_id == null) return true;
		} else if(auth.isUser()) {
			//ra can see all requests
			if(auth.allows("view_all_user_cert_requests")) return true;
			
			//is user the requester?
			ContactRecord contact = auth.getContact();
			if(rec.requester_contact_id.equals(contact.id)) return true;
			
			//maybe sponsor who belongs to the requesteed vo
			VOContactModel model = new VOContactModel(context);
			ContactModel cmodel = new ContactModel(context);
			ArrayList<VOContactRecord> crecs;
			try {
				crecs = model.getByVOID(rec.vo_id);
				for(VOContactRecord crec : crecs) {
					ContactRecord contactrec = cmodel.get(crec.contact_id);
					/*
					if(crec.contact_type_id.equals(11) && crec.contact_rank_id.equals(1)) { //primary
						rec.ra_contact_id = crec.contact_id;
						ticket.ccs.add(contactrec.primary_email);
					}
					*/
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
		rec.status = "REQUESTED";
		
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
		String auth_status = "(Unauthenticated)";
		if(auth.isUser()) {
			auth_status = "(OIM Authenticated)";
		}
		VOModel vmodel = new VOModel(context);
		VORecord vrec = vmodel.get(rec.vo_id);
		ticket.description = "Dear " + vrec.name + " VO RA & Sponsors,\n\n";
		ticket.description = rec.requester_name + " " + auth_status + " has requested a user certificate. \n";
		String url = StaticConfig.getApplicationBase() + "/certificate?id=" + rec.id;
		ticket.description += "Please determine this request's authenticity, and approve / disapprove at " + url;
		if(StaticConfig.isDebug()) {
			ticket.assignees.add("hayashis");
		} else {
			ticket.assignees.add("adeximo");
		}
		ticket.nextaction = "RA/Sponsors to verify requester";
		
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
