package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MeshConfigRecord extends RecordBase  
{
	@Key public Integer id;
	public String name; //part of json url
	public String desc; //json / description field
	public Boolean disable;
	
	//load from existing record
	public MeshConfigRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigRecord() {}
	
}
