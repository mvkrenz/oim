package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;

public class CampusGridSubmitNodeRecord extends RecordBase {

	@Key public Integer campusgrid_id;
	@Key public String fqdn;
	
	//load from existing record
	public CampusGridSubmitNodeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public CampusGridSubmitNodeRecord() {}
}
