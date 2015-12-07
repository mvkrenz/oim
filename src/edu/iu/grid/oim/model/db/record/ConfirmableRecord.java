package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import edu.iu.grid.oim.lib.StaticConfig;

public class ConfirmableRecord extends RecordBase {
	public Timestamp confirmed;
	
	public ConfirmableRecord(ResultSet rs) throws SQLException { super(rs); }
	public ConfirmableRecord() {}
	
	public boolean isConfirmationExpired() {
		Date when = new Date();
		when.setTime(when.getTime()-1000L*3600*24*StaticConfig.getConfirmationExpiration());
		return confirmed.before(when);
	}
	public ConfirmableRecord clone() {
		ConfirmableRecord rec = new ConfirmableRecord();
		rec.confirmed = (Timestamp) confirmed.clone();
		return rec;
	}
}
