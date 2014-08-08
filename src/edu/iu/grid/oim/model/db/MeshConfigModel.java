package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.MeshConfigRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

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
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
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
	
	//search case insensitively (TODO - use sql!)
	public MeshConfigRecord getByName(String name) throws SQLException
	{
		String upname = name.toUpperCase();
		for(MeshConfigRecord rec : getAll()) {
			String recname = rec.name.toUpperCase();
			if(recname.equals(upname)) return rec;
		}
		return null;
	}
}