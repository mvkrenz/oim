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
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class AuthorizationTypeModel extends SmallTableModelBase<AuthorizationTypeRecord> {
    static Logger log = Logger.getLogger(AuthorizationTypeModel.class);  
    
    public AuthorizationTypeModel(Context context) 
    {
    	super(context, "authorization_type");
    }
    AuthorizationTypeRecord createRecord() throws SQLException
	{
		return new AuthorizationTypeRecord();
	}
	public ArrayList<AuthorizationTypeRecord> getAll() throws SQLException
	{
		ArrayList<AuthorizationTypeRecord> list = new ArrayList<AuthorizationTypeRecord>();
		for(RecordBase it : getCache()) {
			list.add((AuthorizationTypeRecord)it);
		}
		return list;
	}
	public AuthorizationTypeRecord get(int id) throws SQLException {
		AuthorizationTypeRecord keyrec = new AuthorizationTypeRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Authorization Type";
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