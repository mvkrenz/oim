package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;

public class VOReportNameModel extends SmallTableModelBase<VOReportNameRecord> {
    static Logger log = Logger.getLogger(VOReportNameModel.class); 

	public VOReportNameModel(Authorization _auth) {
		super(_auth, "vo_report_name");
	}
		VOReportNameRecord createRecord() throws SQLException
	{
		return new VOReportNameRecord();
	}
	public ArrayList<VOReportNameRecord> getAll() throws SQLException
	{
		ArrayList<VOReportNameRecord> list = new ArrayList<VOReportNameRecord>();
		for(RecordBase it : getCache()) {
			list.add((VOReportNameRecord)it);
		}
		return list;
	}
	public ArrayList<VOReportNameRecord> getAllByVOID(int vo_id) throws SQLException
	{
		ArrayList<VOReportNameRecord> list = new ArrayList<VOReportNameRecord>();
		for(VOReportNameRecord it : getAll()) {
			if(it.vo_id.compareTo(vo_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
}
