package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOReportContactModel extends SmallTableModelBase<VOReportContactRecord> {
    static Logger log = Logger.getLogger(VOReportContactModel.class); 
	
	public VOReportContactModel(Authorization _auth) {
		super(_auth, "vo_report_contact");
		// TODO Auto-generated constructor stub
	}
	VOReportContactRecord createRecord() throws SQLException
	{
		return new VOReportContactRecord();
	}

	public ArrayList<VOReportContactRecord> getByVOReportNameID(int vo_report_name_id) throws SQLException
	{ 
		ArrayList<VOReportContactRecord> list = new ArrayList<VOReportContactRecord>();
		for(RecordBase record : getCache()) {
			VOReportContactRecord vorc_record = (VOReportContactRecord)record;
			if(vorc_record.vo_report_name_id == vo_report_name_id) list.add(vorc_record);
		}
		return list;
	}	
}
