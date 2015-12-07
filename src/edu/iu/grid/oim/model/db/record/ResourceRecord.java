package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ResourceRecord extends ConfirmableRecord {

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
	public ResourceRecord() {
		confirmed = new Timestamp(Calendar.getInstance().getTimeInMillis());
	}
	public String getName() {
		return name;
	}
}
