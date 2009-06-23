package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ResourceRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	public String fqdn;
	public String url;
	public Integer resource_group_id;

	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public ResourceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceRecord() {}
	public String getName() {
		return name;
	}
}
