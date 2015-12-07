package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ResourceGroupModel;

public class OsgGridTypeRecord extends RecordBase {
	@Key public Integer id;
	public String name;
	public String description;
	
	//load from existing record
	public OsgGridTypeRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public OsgGridTypeRecord() {}
	
	public String getTitle() {
		return "OSG Grid Type " + name;
	}
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("osg_grid_type");
		labels.add("osg_grid_type_"+id);
		return labels;
	}
}
