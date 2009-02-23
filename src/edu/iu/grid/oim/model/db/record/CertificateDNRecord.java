package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateDNRecord implements IRecord {

	public Integer id;
	public String dn_string;
	public Integer person_id;
	public Integer auth_type_id;
	
	public String getTableName()
	{
		return "certificate_dn";
	}
	
	//load from existing record
	public CertificateDNRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		dn_string = rs.getString("dn_string");
		person_id = rs.getInt("person_id");
		auth_type_id = rs.getInt("auth_type_id");
	}
	
	//for creating new record
	public CertificateDNRecord()
	{
	}
}
