package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;


public class ServiceRecord extends RecordBase {

	@Key public Integer id;
	public String name;
	public String description;
	public Integer port;
	public Integer service_group_id;
	
	//load from existing record
	public ServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ServiceRecord() {}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == service_group_id) {
				ServiceGroupModel model = new ServiceGroupModel(auth);
				ServiceGroupRecord rec = model.get(service_group_id);
				return rec.name;
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
}
