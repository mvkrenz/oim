package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import org.apache.log4j.Logger;
import edu.iu.grid.oim.model.db.record.VOVORecord;

public class VOVOModel extends SmallTableModelBase<VOVORecord> {
    static Logger log = Logger.getLogger(VOVOModel.class);  
	
    public VOVOModel(edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_auth, "vo_vo");
    }
    VOVORecord createRecord() throws SQLException
	{
		return new VOVORecord();
	}
	public VOVORecord get(int id) throws SQLException {
		VOVORecord keyrec = new VOVORecord();
		keyrec.child_vo_id = id;
		return get(keyrec);
	}
}
