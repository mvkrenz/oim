package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.record.RecordBase.Key;

public class CertificateLogRecord extends RecordBase {
	
	@Key public Integer id;
	public Integer certificate_id;
	public Integer contact_id;
	public String remote_addr;
	public Timestamp timestamp;
	public String activity;
	public String detail;
	
	//load from existing record
	public CertificateLogRecord(ResultSet rs) throws SQLException {
		super(rs);
	}
	//for creating new record
	public CertificateLogRecord() {}

}
