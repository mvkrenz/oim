package edu.iu.grid.oim.model.db;


import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;

public class ServiceGroupModel extends SmallTableModelBase<ServiceGroupRecord> {
    static Logger log = Logger.getLogger(ServiceGroupModel.class);  
	
    public ServiceGroupModel(UserContext context) 
    {
    	super(context, "service_group");
    }
    ServiceGroupRecord createRecord() throws SQLException
	{
		return new ServiceGroupRecord();
	}
	public ServiceGroupRecord get(int id) throws SQLException {
		ServiceGroupRecord keyrec = new ServiceGroupRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ServiceGroupRecord> getAll() throws SQLException
	{
		ArrayList<ServiceGroupRecord> list = new ArrayList<ServiceGroupRecord>();
		for(RecordBase it : getCache()) {
			list.add((ServiceGroupRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "Service Group";
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
