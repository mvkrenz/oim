package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;

public class ContactTypeModel extends SmallTableModelBase<ContactTypeRecord> {

	public static HashMap<Integer, ContactTypeRecord> cache = null;
		
	public ContactTypeModel(Authorization _auth) {
		super(_auth, "contact_type");
	}
	ContactTypeRecord createRecord() throws SQLException
	{
		return new ContactTypeRecord();
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
