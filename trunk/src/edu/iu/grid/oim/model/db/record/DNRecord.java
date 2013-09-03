package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;

public class DNRecord extends RecordBase {

	@Key public Integer id;
	public String dn_string;
	public Integer contact_id;
	public Boolean disable;
	
	//load from existing record
	public DNRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public DNRecord() {}
}
