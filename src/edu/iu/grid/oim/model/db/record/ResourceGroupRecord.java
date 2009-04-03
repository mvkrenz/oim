package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ResourceGroupRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	public Integer site_id;
	public Integer osg_grid_type_id;

	public Boolean active;
	public Boolean disable;
	
	//load from existing record
	public ResourceGroupRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceGroupRecord() {}
}
