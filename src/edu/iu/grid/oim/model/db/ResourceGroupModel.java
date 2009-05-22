package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.FacilityModel.AlphabeticalComparator;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class ResourceGroupModel extends SmallTableModelBase<ResourceGroupRecord> {
    static Logger log = Logger.getLogger(ResourceGroupModel.class); 

	public ResourceGroupModel(Context context) {
		super(context, "resource_group");
	}
	ResourceGroupRecord createRecord() throws SQLException
	{
		return new ResourceGroupRecord();
	}
	public ResourceGroupRecord get(int id) throws SQLException {
		ResourceGroupRecord keyrec = new ResourceGroupRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceGroupRecord> getAll() throws SQLException
	{
		ArrayList<ResourceGroupRecord> list = new ArrayList<ResourceGroupRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceGroupRecord)it);
		}
		return list;
	}
	public ArrayList<ResourceGroupRecord> getAllAlphabetized() throws SQLException
	{
		ArrayList<ResourceGroupRecord> list = getAll();
		Collections.sort(list, new AlphabeticalComparator ());
		return list;
	}
	public ArrayList<ResourceGroupRecord> getBySiteID(int site_id) throws SQLException
	{
		ArrayList<ResourceGroupRecord> list = new ArrayList<ResourceGroupRecord>();
		for(RecordBase rec : getCache()) {
			ResourceGroupRecord srec = (ResourceGroupRecord)rec;
			if(srec.site_id.compareTo(site_id) == 0) list.add(srec);
		}
		return list;
	}
    public String getName()
    {
    	return "Resource Group";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("site_id")) {
			SiteModel model = new SiteModel(context);
			SiteRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("osg_grid_type_id")) {
			OsgGridTypeModel model = new OsgGridTypeModel(context);
			OsgGridTypeRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.description + ")";
		}
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
	class AlphabeticalComparator implements Comparator<ResourceGroupRecord> {
	    // Comparator interface requires defining compare method. 
		public int compare(ResourceGroupRecord a, ResourceGroupRecord b) {
			// We are comparing based on name
			return a.getName().compareToIgnoreCase(b.getName());
		}
	}
}
