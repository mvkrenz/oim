package edu.iu.grid.oim.model.db.record;

import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CertificateRequestUserRecord extends RecordBase {

	@Key public Integer id;
	
	//either one of these should be set
	public Integer requester_contact_id; //for oim user
	
	public String requester_passphrase; //only used if guest submits request & user doesn't provide CSR
	public String requester_passphrase_salt;
	public String csr; //only set if user provides csr
	public String cert_certificate;
	public String cert_intermediate;
	public String cert_pkcs7;
	public String cert_serial_id;
	public String dn; 
	public Timestamp request_time;
	//public Timestamp update_time;
	public String status;
	public Integer vo_id;
	public String goc_ticket_id;
	
	public Date cert_notbefore;
	public Date cert_notafter;
	
	//load from existing record
	public CertificateRequestUserRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestUserRecord() {}

}
