package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;


public class DNAuthorizationTypeModel extends SmallTableModelBase<DNAuthorizationTypeRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeModel.class);  
    
    public DNAuthorizationTypeModel(Authorization auth) 
    {
    	super(auth, "dn_authorization_type");
    }
    DNAuthorizationTypeRecord createRecord() throws SQLException
	{
		return new DNAuthorizationTypeRecord();
	}
	public Collection<Integer> getAuthorizationTypesByDNID(Integer dn_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet();
		for(RecordBase it : getCache()) 
		{
			DNAuthorizationTypeRecord rec = (DNAuthorizationTypeRecord)it;
			if(rec.dn_id.compareTo(dn_id) == 0) {
				list.add(rec.authorization_type_id);
			}
		}
		return list;
	}
	public Collection<DNAuthorizationTypeRecord> getAllByDNID(Integer dn_id) throws SQLException
	{
		ArrayList<DNAuthorizationTypeRecord> list = new ArrayList();
		for(RecordBase it : getCache()) 
		{
			DNAuthorizationTypeRecord rec = (DNAuthorizationTypeRecord)it;
			if(rec.dn_id.compareTo(dn_id) == 0) {
				list.add(rec);
			}
		}
		return list;		
	}
    public String getName()
    {
    	return "DN Authorization Type";
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
