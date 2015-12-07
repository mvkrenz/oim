package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class VOSupportsCaRecord_Unused extends RecordBase  
{
	@Key public Integer ca_id;
	@Key public Integer vo_id;
	
	//load from existing record
	public VOSupportsCaRecord_Unused(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public VOSupportsCaRecord_Unused() {}
}
