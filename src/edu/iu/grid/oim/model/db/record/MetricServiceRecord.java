package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.MetricModel;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.ServiceModel;


public class MetricServiceRecord extends RecordBase {

	@Key public Integer metric_id;
	@Key public Integer service_id;
	public Boolean critical;

	//load from existing record
	public MetricServiceRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MetricServiceRecord() {}
	/*
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == metric_id) {
				MetricModel model = new MetricModel(auth);
				MetricRecord rec = model.get(metric_id);
				return rec.common_name;
			} else if(field == service_id) {
				ServiceModel model = new ServiceModel(auth);
				ServiceRecord rec = model.get(service_id);
				return rec.name;	
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
	*/
}
