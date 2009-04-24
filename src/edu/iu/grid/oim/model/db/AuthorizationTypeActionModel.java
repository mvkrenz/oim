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
import edu.iu.grid.oim.model.db.record.AuthorizationTypeActionRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class AuthorizationTypeActionModel extends SmallTableModelBase<AuthorizationTypeActionRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeActionModel.class);  
    
    public AuthorizationTypeActionModel(Authorization auth) 
    {
    	super(auth, "authorization_type_action");
    }
    AuthorizationTypeActionRecord createRecord() throws SQLException
	{
		return new AuthorizationTypeActionRecord();
	}
	
	public Collection<Integer> getActionByAuthTypeID(Integer authorization_type_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet();
		for(RecordBase it : getCache()) 
		{
			AuthorizationTypeActionRecord rec = (AuthorizationTypeActionRecord)it;
			if(rec.authorization_type_id.compareTo(authorization_type_id) == 0) {
				list.add(rec.action_id);
			}
		}
		return list;
	}
	public Collection<Integer> getTypeByActionID(Integer action_id) throws SQLException
	{
		HashSet<Integer> list = new HashSet();
		for(RecordBase it : getCache()) 
		{
			AuthorizationTypeActionRecord rec = (AuthorizationTypeActionRecord)it;
			if(rec.action_id.compareTo(action_id) == 0) {
				list.add(rec.authorization_type_id);
			}
		}
		return list;		
	}
	public ArrayList<AuthorizationTypeActionRecord> getAll() throws SQLException
	{
		ArrayList<AuthorizationTypeActionRecord> list = new ArrayList<AuthorizationTypeActionRecord>();
		for(RecordBase it : getCache()) {
			list.add((AuthorizationTypeActionRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "Action Matrix Entry";
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