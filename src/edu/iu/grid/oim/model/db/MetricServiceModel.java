package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.MetricRecord;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class MetricServiceModel extends SmallTableModelBase<MetricServiceRecord> {
    static Logger log = Logger.getLogger(MetricServiceModel.class);  
    
    public MetricServiceModel(Context context) 
    {
    	super(context, "metric_service");
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("metric_id")) {
			MetricModel model = new MetricModel(context);
			MetricRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("service_id")) {
			ServiceModel model = new ServiceModel(context);
			ServiceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
    MetricServiceRecord createRecord() throws SQLException
	{
		return new MetricServiceRecord();
	}
	public MetricServiceRecord get(int metric_id, int service_id) throws SQLException {
		MetricServiceRecord keyrec = new MetricServiceRecord();
		keyrec.metric_id = metric_id;
		keyrec.service_id = service_id;
		return get(keyrec);
	}
	public Collection<MetricServiceRecord> getAll() throws SQLException
	{
		ArrayList<MetricServiceRecord> list = new ArrayList<MetricServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((MetricServiceRecord)it);
		}
		return list;
	}
	
	public Collection<MetricServiceRecord> getAllByServiceID(int service_id) throws SQLException
	{
		ArrayList<MetricServiceRecord> list = new ArrayList<MetricServiceRecord>();
		for(RecordBase it : getCache()) {
			MetricServiceRecord rec = (MetricServiceRecord)it;
			if(rec.service_id.compareTo(service_id) == 0) {
				list.add(rec);
			}
		}
		return list;		
	}
    public String getName()
    {
    	return "Metric Service";
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