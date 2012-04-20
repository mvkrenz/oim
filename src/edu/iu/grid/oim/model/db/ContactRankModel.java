package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class ContactRankModel extends SmallTableModelBase<ContactRankRecord> {	
	public ContactRankModel(UserContext _context) {
		super(_context, "contact_rank");
	}
	ContactRankRecord createRecord() throws SQLException
	{
		return new ContactRankRecord();
	}
	
	public HashMap<Integer, ContactRankRecord> getAll() throws SQLException
	{ 
		HashMap<Integer, ContactRankRecord> list = new HashMap<Integer, ContactRankRecord>();
		for(RecordBase rec : getCache()) {
			ContactRankRecord vcrec = (ContactRankRecord)rec;
			list.put(vcrec.id, vcrec);
		}
		return list;
	}	
	
	public ContactRankRecord get(int id) throws SQLException {
		ContactRankRecord keyrec = new ContactRankRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Contact Rank";
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
