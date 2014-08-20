package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.MeshConfigWLCGMemberRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.WLCGEndpointRecord;

public class MeshConfigWLCGMemberModel extends SmallTableModelBase<MeshConfigWLCGMemberRecord>
{	
    static Logger log = Logger.getLogger(MeshConfigWLCGMemberModel.class);  

    public MeshConfigWLCGMemberModel(UserContext context) 
    {
    	super(context, "mesh_config_wlcg_member");
    }
    MeshConfigWLCGMemberRecord createRecord() throws SQLException
	{
		return new MeshConfigWLCGMemberRecord();
	}
    public String getName()
    {
    	return "Mesh Config WLCG Group Members";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("group_id")) {
			MeshConfigGroupModel model = new MeshConfigGroupModel(context);
			MeshConfigGroupRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} 
		if(field_name.equals("primary_key")) {
			WLCGEndpointModel model = new WLCGEndpointModel(context);
			WLCGEndpointRecord rec = model.get(value);
			return value + " (" + rec.hostname + ")";
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
	public ArrayList<MeshConfigWLCGMemberRecord> getAll() throws SQLException
	{
		ArrayList<MeshConfigWLCGMemberRecord> list = new ArrayList<MeshConfigWLCGMemberRecord>();
		for(RecordBase it : getCache()) {
			list.add((MeshConfigWLCGMemberRecord)it);
		}
		return list;
	}
	public ArrayList<MeshConfigWLCGMemberRecord> getByGroupID(Integer id) throws SQLException {
		ArrayList<MeshConfigWLCGMemberRecord> list = new ArrayList<MeshConfigWLCGMemberRecord>();
		for(RecordBase it : getCache()) {
			MeshConfigWLCGMemberRecord rec = (MeshConfigWLCGMemberRecord)it;
			if(rec.group_id.equals(id)) {
				list.add(rec);
			}
		}
		return list;
	}
}