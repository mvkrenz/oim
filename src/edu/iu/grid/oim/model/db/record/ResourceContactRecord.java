package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCModel;

public class ResourceContactRecord extends RecordBase {

	@Key public Integer resource_id;
	@Key public Integer contact_type_id;
	@Key public Integer contact_rank_id;
	@Key public Integer contact_id;
	
	//load from existing record
	public ResourceContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public ResourceContactRecord() { }
	/*
	public Integer getRank() {
		return contact_rank_id;
	}
	*/
}
