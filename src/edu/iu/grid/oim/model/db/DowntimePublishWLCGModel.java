package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.DowntimePublishWLCGRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DowntimePublishWLCGModel extends SmallTableModelBase<DowntimePublishWLCGRecord> {
    static Logger log = Logger.getLogger(DowntimePublishWLCGModel.class);  
    
    public DowntimePublishWLCGModel(Context context) 
    {
    	super(context, "downtime_publish_wlcg");
    }
    DowntimePublishWLCGRecord createRecord() throws SQLException
	{
		return new DowntimePublishWLCGRecord();
	}
	public ArrayList<DowntimePublishWLCGRecord> getAll() throws SQLException
	{
		ArrayList<DowntimePublishWLCGRecord> list = new ArrayList<DowntimePublishWLCGRecord>();
		for(RecordBase it : getCache()) {
			list.add((DowntimePublishWLCGRecord)it);
		}
		return list;
	}
	public DowntimePublishWLCGRecord get(int resource_downtime_id, int downtime_action_id) throws SQLException {
		DowntimePublishWLCGRecord keyrec = new DowntimePublishWLCGRecord();
		keyrec.resource_downtime_id = resource_downtime_id;
		keyrec.downtime_action_id = downtime_action_id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Downtime Publish WLCG";
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