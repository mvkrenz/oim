package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class ResourceModel extends SmallTableModelBase<ResourceRecord> {
    static Logger log = Logger.getLogger(ResourceModel.class);  
    
    public ResourceModel(Authorization auth) 
    {
    	super(auth, "resource");
    }
    ResourceRecord createRecord(ResultSet rs) throws SQLException
	{
		return new ResourceRecord(rs);
	}
	
	public ArrayList<ResourceRecord> getByGroupID(int group_id) throws SQLException
	{
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord>();
		for(RecordBase rec : getCache()) {
			ResourceRecord vcrec = (ResourceRecord)rec;
			if(vcrec.resource_group_id == group_id) list.add(vcrec);
		}
		return list;
	}
}

