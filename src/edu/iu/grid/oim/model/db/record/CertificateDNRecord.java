package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateDNRecord implements IRecord {

	public Integer id;
	public String dn_string;
	public Integer contact_id;
	public Integer authorization_type_id;
	
	/*
	public String getTableName()
	{
		return "dn";
	}
	*/
	
	//load from existing record
	public CertificateDNRecord(ResultSet rs) throws SQLException {
		id 			= rs.getInt("id");
		dn_string 	= rs.getString("dn_string");
		contact_id 	= rs.getInt("contact_id");
		authorization_type_id = rs.getInt("authorization_type_id");
	}
	
	//for creating new record
	public CertificateDNRecord()
	{
	}
}
