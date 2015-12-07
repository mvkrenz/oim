package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.mysql.jdbc.Field;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.OsgGridTypeRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.lib.Authorization;

public class OsgGridTypeModel extends SmallTableModelBase<OsgGridTypeRecord> {
    static Logger log = Logger.getLogger(OsgGridTypeModel.class);  
	
    public OsgGridTypeModel(UserContext context) 
    {
    	super(context, "osg_grid_type");
    }
    OsgGridTypeRecord createRecord() throws SQLException
	{
		return new OsgGridTypeRecord();
	}
	public ArrayList<OsgGridTypeRecord> getAll() throws SQLException
	{
		ArrayList<OsgGridTypeRecord> list = new ArrayList<OsgGridTypeRecord>();
		for(RecordBase it : getCache()) {
			list.add((OsgGridTypeRecord)it);
		}
		return list;
	}
	public OsgGridTypeRecord get(Integer id) throws SQLException {
		OsgGridTypeRecord keyrec = new OsgGridTypeRecord();
		keyrec.id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "OSG Grid Type";
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
