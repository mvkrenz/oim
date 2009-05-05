package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;

public class ResourceWLCGModel extends SmallTableModelBase<ResourceWLCGRecord> {
    static Logger log = Logger.getLogger(ResourceWLCGModel.class);  
	
    public ResourceWLCGModel(Context context) 
    {
    	super(context, "resource_wlcg");
    }
    ResourceWLCGRecord createRecord() throws SQLException
	{
		return new ResourceWLCGRecord();
	}
	public ResourceWLCGRecord get(int id) throws SQLException {
		ResourceWLCGRecord keyrec = new ResourceWLCGRecord();
		keyrec.resource_id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceWLCGRecord> getAll() throws SQLException
	{
		ArrayList<ResourceWLCGRecord> list = new ArrayList<ResourceWLCGRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceWLCGRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "WLCG Information";
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
}
