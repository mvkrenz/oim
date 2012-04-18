package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CertificateRequestUserRecord extends RecordBase {

	@Key public Integer id;
	
	//either one of these should be set
	public Integer requester_contact_id; //for oim user
	public String requester_name; //for guest
	
	public String requester_passphrase; //only used if guest submits request & user doesn't provide CSR
	public String csr; //only set if user provides csr
	public String dn; 
	public Timestamp request_time;
	//public Timestamp update_time;
	public String status;
	public Integer vo_id;
	public String goc_ticket_id;
	
	//load from existing record
	public CertificateRequestUserRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestUserRecord() {}

}
