package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;


public class GridAdminModel extends SmallTableModelBase<GridAdminRecord> {
    static Logger log = Logger.getLogger(GridAdminModel.class);  
    
    public GridAdminModel(UserContext context) 
    {
    	super(context, "grid_admin");
    }
    
    GridAdminRecord createRecord() throws SQLException
	{
		return new GridAdminRecord();
	}
	public ArrayList<GridAdminRecord> getAll() throws SQLException
	{
		ArrayList<GridAdminRecord> list = new ArrayList<GridAdminRecord>();
		for(RecordBase it : getCache()) {
			list.add((GridAdminRecord)it);
		}
		return list;
	}
	public GridAdminRecord get(int id) throws SQLException {
		GridAdminRecord keyrec = new GridAdminRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	//search for gridadmin with most specific domain name registered for given fqdn.
	//return null if not found
	public ContactRecord getGridAdminByFQDN(String fqdn) throws SQLException {

		Integer contact_id = null;
		String domain = null;
		ArrayList<GridAdminRecord> recs = getAll();
		for(GridAdminRecord rec : recs) {
			if(fqdn.endsWith(rec.domain)) {
				//keep - if we find more specific domain
				if(contact_id == null || domain.length() < rec.domain.length()) {
					contact_id = rec.contact_id;
					domain = rec.domain;
				} 
			}
		}
		
		if(contact_id == null) {
			return null;
		}
		ContactModel cmodel = new ContactModel(context);
		return cmodel.get(contact_id);
	}
	
    public String getName()
    {
    	return "GridAdmin";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
}