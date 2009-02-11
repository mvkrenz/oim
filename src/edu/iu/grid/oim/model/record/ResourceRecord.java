package edu.iu.grid.oim.model.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceRecord implements IRecord {

	public Integer id;
	public String name;
	public String description;
	public String fqdn;
	public String url;
	public Integer resource_group_id;
	
	public Boolean interop_bdii;
	public Boolean interop_monitoring;
	public Boolean interop_accounting;
	public String wlcg_accounting_name;

	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public ResourceRecord(ResultSet rs) throws SQLException {
		id = rs.getInt("id");
		name = rs.getString("name");
		description = rs.getString("description");
		fqdn = rs.getString("fqdn");
		url = rs.getString("url");
		resource_group_id = rs.getInt("resource_group_id");

		interop_bdii = rs.getBoolean("interop_bdii");
		interop_monitoring = rs.getBoolean("interop_monitoring");
		interop_accounting = rs.getBoolean("interop_accounting");
		wlcg_accounting_name = rs.getString("wlcg_accounting_name");
		
		active = rs.getBoolean("active");
		disable = rs.getBoolean("disable");	
	}
	
	//for creating new record
	public ResourceRecord()
	{
	}


}
