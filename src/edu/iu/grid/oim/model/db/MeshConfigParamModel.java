package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigOIMMemberRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigParamRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class MeshConfigParamModel extends SmallTableModelBase<MeshConfigParamRecord>
{	
    static Logger log = Logger.getLogger(MeshConfigParamModel.class);  

    public MeshConfigParamModel(UserContext context) 
    {
    	super(context, "mesh_config_param");
    }
    MeshConfigParamRecord createRecord() throws SQLException
	{
		return new MeshConfigParamRecord();
	}
    public String getName()
    {
    	return "Mesh Config Params";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		return value;
	} 
	
	public MeshConfigParamRecord get(int id) throws SQLException {
		MeshConfigParamRecord keyrec = new MeshConfigParamRecord();
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
	public ArrayList<MeshConfigParamRecord> getAll() throws SQLException
	{
		ArrayList<MeshConfigParamRecord> list = new ArrayList<MeshConfigParamRecord>();
		for(RecordBase it : getCache()) {
			list.add((MeshConfigParamRecord)it);
		}
		return list;
	}
	public ArrayList<MeshConfigParamRecord> getByServiceID(Integer id) throws SQLException {
		ArrayList<MeshConfigParamRecord> list = new ArrayList<MeshConfigParamRecord>();
		for(RecordBase it : getCache()) {
			MeshConfigParamRecord t = (MeshConfigParamRecord)it;
			if(t.service_id.equals(id)) {
				list.add(t);
			}
		}
		return list;		
	}
}