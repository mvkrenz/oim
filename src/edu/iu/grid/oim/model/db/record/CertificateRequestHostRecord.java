package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CertificateRequestHostRecord extends RecordBase {

	@Key public Integer id;
	
	//either one of these should be set
	public Integer requester_contact_id; //for oim user
	public String requester_name; //for guest
	
	public String fqdn_csrs; //list of fqdn:csr lines
	
	//return from DigiCert
	public String cert_certificate;
	public String cert_intermediate;
	public String cert_pkcs7;
	
	//book keeping
	public Timestamp request_time;
	public Timestamp update_time;
	
	public String status;
	public String goc_ticket_id;
	
	//load from existing record
	public CertificateRequestHostRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestHostRecord() {}

}
