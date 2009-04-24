package edu.iu.grid.oim.model.db;

import java.sql.SQLException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;

public class ContactRankModel extends SmallTableModelBase<ContactRankRecord> {	
	public ContactRankModel(Context _context) {
		super(_context, "contact_rank");
	}
	ContactRankRecord createRecord() throws SQLException
	{
		return new ContactRankRecord();
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
