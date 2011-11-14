package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceModel;

public class ResourceServiceDetailRecord extends RecordBase {

	@Key public Integer service_id;
	@Key public Integer resource_id;
	@Key public String key;
	public String value;
	
	//load from existing record
	public ResourceServiceDetailRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceServiceDetailRecord() {}
}
