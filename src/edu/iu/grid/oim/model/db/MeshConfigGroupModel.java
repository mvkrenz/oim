package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.MeshConfigGroupRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VORecord;

public class MeshConfigGroupModel extends SmallTableModelBase<MeshConfigGroupRecord>
{	
    static Logger log = Logger.getLogger(MeshConfigGroupModel.class);  

    public MeshConfigGroupModel(UserContext context) 
    {
    	super(context, "mesh_config_group");
    }
    MeshConfigGroupRecord createRecord() throws SQLException
	{
		return new MeshConfigGroupRecord();
	}
    public String getName()
    {
    	return "Mesh Config Groups";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	} 
	public MeshConfigGroupRecord get(int id) throws SQLException {
		MeshConfigGroupRecord keyrec = new MeshConfigGroupRecord();
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
	public ArrayList<MeshConfigGroupRecord> getAll() throws SQLException
	{
		ArrayList<MeshConfigGroupRecord> list = new ArrayList<MeshConfigGroupRecord>();
		for(RecordBase it : getCache()) {
			list.add((MeshConfigGroupRecord)it);
		}
		return list;
	}
	
	//search case insensitively (TODO - use sql!)
	public MeshConfigGroupRecord getByName(String name) throws SQLException
	{
		String upname = name.toUpperCase();
		for(MeshConfigGroupRecord rec : getAll()) {
			String recname = rec.name.toUpperCase();
			if(recname.equals(upname)) return rec;
		}
		return null;
	}
}