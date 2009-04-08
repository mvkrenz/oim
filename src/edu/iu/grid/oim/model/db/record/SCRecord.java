package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ActionModel;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;

public class SCRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String long_name;
	public String description;
	public String community;
	public Boolean active;
	public Boolean disable;
	public String footprints_id;
	
	//load from existing record
	public SCRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SCRecord() {}
}
