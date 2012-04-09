package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateRecord extends RecordBase {

	@Key public Integer id;
	public String type;
	public Integer requester_contact_id;
	public String guest_name;
	public Integer sponsor_contact_id;
	public String signer_id;
	public String status;
	public String csr;
	public String certificate;
	public String privatekey;
	public String passphrase_hash;
	public Integer resource_id;
	public Integer resource_service_id;

	//load from existing record
	public CertificateRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateRecord() {}

}
