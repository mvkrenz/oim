package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateAuthorityRecord implements IRecord {

	public Integer id;
	public String name;
	public String file_id;
	public String md5sum;
	public Integer disable;
	
	public String getTableName()
	{
		return "certificate_authority";
	}
	
	//load from existing record
	public CertificateAuthorityRecord(ResultSet rs) throws SQLException {
		id 		 = rs.getInt("id");
		name 	 = rs.getString("name");
		file_id	 = rs.getString("file_id");
		md5sum	 = rs.getString("md5sum");
		disable  = rs.getInt("dsiable");
	}	
	
	//for creating new record
	public CertificateAuthorityRecord()
	{
	}
}
