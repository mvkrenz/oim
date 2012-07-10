package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import edu.iu.grid.oim.lib.StringArray;

public class CertificateRequestHostRecord extends RecordBase {

	@Key public Integer id;

	public Integer requester_contact_id; //null if it's submitted by guest
	
	//guest contact info (used for goc ticket)
	public String requester_name;
	public String requester_email;
	public String requester_phone;
	
	public String csrs; //StringArray xml
	public String cns; //StringArray xml
	
	//return from DigiCert
	public String cert_certificate; //StringArray xml
	public String cert_intermediate; //StringArray xml
	public String cert_pkcs7; //StringArray xml
	public String cert_serial_ids; //StringArray xml
	
	//book keeping
	public Timestamp request_time;
	public Timestamp update_time;
	
	public String status;
	public String status_note;
	//public Integer gridadmin_contact_id;
	public String goc_ticket_id;
	
	//load from existing record
	public CertificateRequestHostRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRequestHostRecord() {}
	
	public String[] getCNs() {
		StringArray sa = new StringArray(cns);
		return sa.getAll();
	}
	public String[] getPKCS7s() {
		StringArray sa = new StringArray(cert_pkcs7);
		return sa.getAll();
	}
	public String[] getCertificates() {
		StringArray sa = new StringArray(cert_certificate);
		return sa.getAll();
	}
	public String[] getCSRs() {
		StringArray sa = new StringArray(csrs);
		return sa.getAll();
	}
	public String[] getSerialIDs() {
		StringArray sa = new StringArray(cert_serial_ids);
		return sa.getAll();
	}

}
