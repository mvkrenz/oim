package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateAuthorityRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String file_id;
	public String md5sum;
	public Integer disable;
		
	//load from existing record
	public CertificateAuthorityRecord(ResultSet rs) throws SQLException { super(rs); }	
	//for creating new record
	public CertificateAuthorityRecord() {}
	/*
	public int compareKeysTo(RecordBase o) {
		if(this == o) return 0;
		CertificateAuthorityRecord you = (CertificateAuthorityRecord)o;
		if(id.compareTo(you.id) == 0) return 0;
		return 1;
	}
	*/
}
