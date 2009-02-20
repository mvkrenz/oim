package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VORecord implements IRecord {

	public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String primary_url;
	public String aup_url;
	public String membership_services_url;
	public String purpose_url;
	public String support_url;
	public String app_description;
	public String community;
	public Integer sc_id;
	public Integer parent_vo_id;
	public Boolean active;
	public Boolean disable;
	public String footprints_id;
	
	//load from existing record
	public VORecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		description = rs.getString("description");
		long_name = rs.getString("long_name");
		primary_url = rs.getString("primary_url");
		aup_url = rs.getString("aup_url");
		membership_services_url = rs.getString("membership_services_url");
		purpose_url = rs.getString("purpose_url");
		support_url = rs.getString("support_url");
		app_description = rs.getString("app_description");
		community = rs.getString("community");
		sc_id = rs.getInt("sc_id");
		parent_vo_id = rs.getInt("parent_vo_id");
		active = rs.getBoolean("active");
		disable = rs.getBoolean("disable");
		footprints_id = rs.getString("footprints_id");		 
	}
	
	//for creating new record
	public VORecord()
	{
	}
}
