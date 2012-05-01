package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CertificateRequestHostRecord extends RecordBase {

	@Key public Integer id;

	//used for goc ticket contact, but since we don't store contact_id, we should probably store this in DB
	public String requester_name;
	public String requester_email;
	public String requester_phone;
	
	public String csrs; //list of csrs on each line
	
	//return from DigiCert
	public String cert_certificate;
	public String cert_intermediate;
	public String cert_pkcs7;
	
	//book keeping
	public Timestamp request_time;
	public Timestamp update_time;
	
	public String status;
	public Integer gridadmin_contact_id;
	public String goc_ticket_id;
	
	//load from existing record
	public CertificateRequestHostRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestHostRecord() {}

}
