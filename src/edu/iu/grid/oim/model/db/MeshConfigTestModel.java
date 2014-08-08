package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigMemberRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigParamRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigTestRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class MeshConfigTestModel extends SmallTableModelBase<MeshConfigTestRecord>
{	
    static Logger log = Logger.getLogger(MeshConfigTestModel.class);  

    public MeshConfigTestModel(UserContext context) 
    {
    	super(context, "mesh_config_test");
    }
    MeshConfigTestRecord createRecord() throws SQLException
	{
		return new MeshConfigTestRecord();
	}
    public String getName()
    {
    	return "Mesh Config Tests";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("groupa_id")) {
			MeshConfigGroupModel model = new MeshConfigGroupModel(context);
			MeshConfigGroupRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} 
		if(field_name.equals("groupb_id")) {
			MeshConfigGroupModel model = new MeshConfigGroupModel(context);
			MeshConfigGroupRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} 
		if(field_name.equals("param_id")) {
			MeshConfigParamModel model = new MeshConfigParamModel(context);
			MeshConfigParamRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}	
		return value;
	} 
	
	public MeshConfigTestRecord get(int id) throws SQLException {
		MeshConfigTestRecord keyrec = new MeshConfigTestRecord();
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
	public ArrayList<MeshConfigTestRecord> getAll() throws SQLException
	{
		ArrayList<MeshConfigTestRecord> list = new ArrayList<MeshConfigTestRecord>();
		for(RecordBase it : getCache()) {
			list.add((MeshConfigTestRecord)it);
		}
		return list;
	}
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
}