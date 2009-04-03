package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VORecord;

public class AuthorizationTypeModel extends SmallTableModelBase<AuthorizationTypeRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeModel.class);  
    
    public AuthorizationTypeModel(Authorization auth) 
    {
    	super(auth, "authorization_type");
    }
    AuthorizationTypeRecord createRecord(ResultSet rs) throws SQLException
	{
		return new AuthorizationTypeRecord(rs);
	}
	public ArrayList<AuthorizationTypeRecord> getAll() throws SQLException
	{
		ArrayList<AuthorizationTypeRecord> list = new ArrayList<AuthorizationTypeRecord>();
		for(RecordBase it : getCache()) {
			list.add((AuthorizationTypeRecord)it);
		}
		return list;
	}
}