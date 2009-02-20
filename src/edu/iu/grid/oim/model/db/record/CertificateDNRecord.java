package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateDNRecord implements IRecord {

	private Integer id;
	private String dn_string;
	private Integer person_id;
	private Integer auth_type_id;
	
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
	
	public Integer getID() {
		return id;
	}
	public Integer getAuthTypeID()
	{
		return auth_type_id;
	}
}
