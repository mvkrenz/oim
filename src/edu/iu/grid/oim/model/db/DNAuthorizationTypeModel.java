package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;


public class DNAuthorizationTypeModel extends SmallTableModelBase<DNAuthorizationTypeRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeModel.class);  
    
    public DNAuthorizationTypeModel(Authorization auth) 
    {
    	super(auth, "dn_authorization_type");
    }
    DNAuthorizationTypeRecord createRecord(ResultSet rs) throws SQLException
	{
		return new DNAuthorizationTypeRecord(rs);
	}
	public Collection<Integer> getAuthorizationTypesByDNID(Integer dn_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet();
		for(RecordBase it : getCache()) 
		{
			DNAuthorizationTypeRecord rec = (DNAuthorizationTypeRecord)it;
			if(rec.dn_id.compareTo(dn_id) == 0) {
				list.add(rec.authorization_type_id);
			}
		}
		return list;
	}
}
