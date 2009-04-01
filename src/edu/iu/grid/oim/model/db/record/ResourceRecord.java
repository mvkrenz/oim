package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceRecord extends RecordBase {

	@Key public Integer id;
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
	public ResourceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceRecord() {}
}
