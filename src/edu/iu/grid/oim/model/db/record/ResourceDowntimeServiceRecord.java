package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceModel;

public class ResourceDowntimeServiceRecord extends RecordBase {

	@Key public Integer resource_downtime_id;
	@Key public Integer service_id;
	
	//load from existing record
	public ResourceDowntimeServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceDowntimeServiceRecord() {}

}
