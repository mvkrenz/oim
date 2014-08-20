package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigParamRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class MeshConfigModel extends SmallTableModelBase<MeshConfigRecord>
{	
    static Logger log = Logger.getLogger(MeshConfigModel.class);  

    public MeshConfigModel(UserContext context) 
    {
    	super(context, "mesh_config");
    }
    MeshConfigRecord createRecord() throws SQLException
	{
		return new MeshConfigRecord();
	}
    public String getName()
    {
    	return "Mesh Config";
    }

	public MeshConfigRecord get(int id) throws SQLException {
		MeshConfigRecord keyrec = new MeshConfigRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		if(auth.allows("admin_meshconig")) {
			return true;
		}
		return false;
	}
	/*
	public boolean canEdit(int meshconfig_id)
	{
		if(auth.allows("admin")) return true;
		if(auth.allows("admin_meshconfig")) return true;
		return false;
	}
	*/
	public ArrayList<MeshConfigRecord> getAll() throws SQLException
	{
		ArrayList<MeshConfigRecord> list = new ArrayList<MeshConfigRecord>();
		for(RecordBase it : getCache()) {
			list.add((MeshConfigRecord)it);
		}
		return list;
	}
	/*
	public ArrayList<MeshConfigTestRecord> getByMeshconfigID(Integer id) throws SQLException {
		ArrayList<MeshConfigTestRecord> list = new ArrayList<MeshConfigTestRecord>();
		for(RecordBase it : getCache()) {
			MeshConfigTestRecord t = (MeshConfigTestRecord)it;
			if(t.meshconfig_id.equals(id)) {
				list.add(t);
			}
		}
		return list;		
	}
	*/
}