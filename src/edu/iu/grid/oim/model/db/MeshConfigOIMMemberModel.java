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
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class MeshConfigOIMMemberModel extends SmallTableModelBase<MeshConfigOIMMemberRecord>
{	
    static Logger log = Logger.getLogger(MeshConfigOIMMemberModel.class);  

    public MeshConfigOIMMemberModel(UserContext context) 
    {
    	super(context, "mesh_config_member");
    }
    MeshConfigOIMMemberRecord createRecord() throws SQLException
	{
		return new MeshConfigOIMMemberRecord();
	}
    public String getName()
    {
    	return "Mesh Config OIM Group Members";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("group_id")) {
			MeshConfigGroupModel model = new MeshConfigGroupModel(context);
			MeshConfigGroupRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} 
		if(field_name.equals("resource_id")) {
			ResourceModel model = new ResourceModel(context);
			ResourceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}	
		if(field_name.equals("service_id")) {
			ServiceModel model = new ServiceModel(context);
			ServiceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}	
		return value;
	} 
	/*
	public MeshConfigMemberRecord get(int id) throws SQLException {
		MeshConfigMemberRecord keyrec = new MeshConfigMemberRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	*/
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
	public ArrayList<MeshConfigOIMMemberRecord> getAll() throws SQLException
	{
		ArrayList<MeshConfigOIMMemberRecord> list = new ArrayList<MeshConfigOIMMemberRecord>();
		for(RecordBase it : getCache()) {
			list.add((MeshConfigOIMMemberRecord)it);
		}
		return list;
	}
	public ArrayList<MeshConfigOIMMemberRecord> getByGroupID(Integer id) throws SQLException {
		ArrayList<MeshConfigOIMMemberRecord> list = new ArrayList<MeshConfigOIMMemberRecord>();
		for(RecordBase it : getCache()) {
			MeshConfigOIMMemberRecord rec = (MeshConfigOIMMemberRecord)it;
			if(rec.group_id.equals(id)) {
				list.add(rec);
			}
		}
		return list;
	}
}