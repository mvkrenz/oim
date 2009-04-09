package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.lib.Authorization;

public class CpuInfoModel extends SmallTableModelBase<CpuInfoRecord> {
    static Logger log = Logger.getLogger(CpuInfoModel.class);  
	
    public CpuInfoModel(Authorization _auth) 
    {
    	super(_auth, "cpu_info");
    }
    CpuInfoRecord createRecord(ResultSet rs) throws SQLException
	{
		return new CpuInfoRecord(rs);
	}
	public ArrayList<CpuInfoRecord> getAll() throws SQLException
	{
		ArrayList<CpuInfoRecord> list = new ArrayList<CpuInfoRecord>();
		for(RecordBase it : getCache()) {
			list.add((CpuInfoRecord)it);
		}
		return list;
	}
}
