package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.CampusGridSubmitNodeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VORecord;

public class CampusGridSubmitNodeModel extends SmallTableModelBase<CampusGridSubmitNodeRecord> {
    static Logger log = Logger.getLogger(CampusGridSubmitNodeModel.class); 

	public CampusGridSubmitNodeModel(UserContext context) {
		super(context, "campusgrid_submitnode");
	}
	CampusGridSubmitNodeRecord createRecord() throws SQLException
	{
		return new CampusGridSubmitNodeRecord();
	}
	public ArrayList<CampusGridSubmitNodeRecord> getAll() throws SQLException
	{
		ArrayList<CampusGridSubmitNodeRecord> list = new ArrayList<CampusGridSubmitNodeRecord>();
		for(RecordBase it : getCache()) {
			list.add((CampusGridSubmitNodeRecord)it);
		}
		return list;
	}
	public ArrayList<CampusGridSubmitNodeRecord> getAllByCampusGridID(int campusgrid_id) throws SQLException
	{
		ArrayList<CampusGridSubmitNodeRecord> list = new ArrayList<CampusGridSubmitNodeRecord>();
		for(CampusGridSubmitNodeRecord it : getAll()) {
			if(it.campusgrid_id.compareTo(campusgrid_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
	/*
    public String getName()
    {
    	return "CampusGrid SubmitNode";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='resource_id']/Value", doc, XPathConstants.STRING));
		ResourceModel model = new ResourceModel(context);
		return model.canEdit(id);
	}
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("resource_id")) {
			ResourceModel model = new ResourceModel(context);
			ResourceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
	*/
}
