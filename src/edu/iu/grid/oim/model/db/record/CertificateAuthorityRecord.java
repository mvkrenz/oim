package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateAuthorityRecord extends RecordBase {

	public Integer id;
	public String name;
	public String file_id;
	public String md5sum;
	public Integer disable;
		
	//load from existing record
	public CertificateAuthorityRecord(ResultSet rs) throws SQLException { super(rs); }	
	//for creating new record
	public CertificateAuthorityRecord() {}
}
