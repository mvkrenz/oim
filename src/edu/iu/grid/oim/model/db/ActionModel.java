package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ActionRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class ActionModel extends SmallTableModelBase<ActionRecord> {
    static Logger log = Logger.getLogger(ActionModel.class);  
    
    public ActionModel(UserContext context) 
    {
    	super(context, "action");
    }
    ActionRecord createRecord() throws SQLException
	{
		return new ActionRecord();
	}
	public ActionRecord get(int id) throws SQLException {
		ActionRecord keyrec = new ActionRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ActionRecord> getAll() throws SQLException
	{
		ArrayList<ActionRecord> list = new ArrayList<ActionRecord>();
		for(RecordBase it : getCache()) {
			list.add((ActionRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "Action";
    }
    /*
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(context.getAuthorization().allows("admin")) {
			return true;
		}
		return false;
	}
	*/
}