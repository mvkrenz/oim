package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;

public class ContactTypeModel extends SmallTableModelBase<ContactTypeRecord> {

	public static HashMap<Integer, ContactTypeRecord> cache = null;
		
	public ContactTypeModel(UserContext _context) {
		super(_context, "contact_type");
	}
	ContactTypeRecord createRecord() throws SQLException
	{
		return new ContactTypeRecord();
	}
	
	public HashMap<Integer, ContactTypeRecord> getAll() throws SQLException
	{ 
		HashMap<Integer, ContactTypeRecord> list = new HashMap<Integer, ContactTypeRecord>();
		for(RecordBase rec : getCache()) {
			ContactTypeRecord vcrec = (ContactTypeRecord)rec;
			list.put(vcrec.id, vcrec);
		}
		return list;
	}	
	
	public ContactTypeRecord get(int id) throws SQLException {
		ContactTypeRecord keyrec = new ContactTypeRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Contact Type";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
}
