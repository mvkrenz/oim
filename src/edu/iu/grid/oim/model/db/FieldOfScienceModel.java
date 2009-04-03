package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class FieldOfScienceModel extends SmallTableModelBase<FieldOfScienceRecord> {
    static Logger log = Logger.getLogger(FieldOfScienceModel.class);  
	
    public FieldOfScienceModel(Authorization _auth) 
    {
    	super(_auth, "field_of_science");
    }
    FieldOfScienceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new FieldOfScienceRecord(rs);
	}
	public ArrayList<FieldOfScienceRecord> getAll() throws SQLException
	{
		ArrayList<FieldOfScienceRecord> list = new ArrayList<FieldOfScienceRecord>();
		for(RecordBase it : getCache()) {
			list.add((FieldOfScienceRecord)it);
		}
		return list;
	}
}
