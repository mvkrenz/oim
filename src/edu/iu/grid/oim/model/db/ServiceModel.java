package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class ServiceModel extends SmallTableModelBase<ServiceRecord> {
    static Logger log = Logger.getLogger(ServiceModel.class);  
	
    public ServiceModel(Context context) 
    {
    	super(context, "service");
    }
    ServiceRecord createRecord() throws SQLException
	{
		return new ServiceRecord();
	}
	public ServiceRecord get(int id) throws SQLException {
		ServiceRecord keyrec = new ServiceRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ServiceRecord> getAll() throws SQLException
	{
		ArrayList<ServiceRecord> list = new ArrayList<ServiceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ServiceRecord)it);
		}
		return list;
	}
	public ArrayList<ServiceRecord> getByServiceGroupID(int service_group_id) throws SQLException
	{
		ArrayList<ServiceRecord> list = new ArrayList<ServiceRecord>();
		for(RecordBase rec : getCache()) {
			ServiceRecord srec = (ServiceRecord)rec;
			if(srec.service_group_id.compareTo(service_group_id) == 0) list.add(srec);
		}
		return list;
	}	
	public void insertDetail(ServiceRecord rec, 
			ArrayList<MetricServiceRecord> metrics) throws Exception
	{
		Connection conn = null;
		try {			
			conn = connectOIM();
			conn.setAutoCommit(false);
			
			insert(rec);
			
			//process metric information
			MetricServiceModel cmodel = new MetricServiceModel(context);
			for(MetricServiceRecord msrec : metrics) {
				msrec.service_id = rec.id;
			}
			cmodel.insert(metrics);
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back Service Detail insert transaction.");
			if(conn != null) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(ServiceRecord rec, 
			ArrayList<MetricServiceRecord> metrics) throws Exception
	{
		Connection conn = null;
		try {
			//process detail information
			conn = connectOIM();
			conn.setAutoCommit(false);
			
			update(get(rec), rec);
			
			MetricServiceModel cmodel = new MetricServiceModel(context);
			for(MetricServiceRecord msrec : metrics) {
				msrec.service_id = rec.id;
			}
			cmodel.update(cmodel.getAllByServiceID(rec.id), metrics);
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			if(conn != null) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			//re-throw original exception
			throw new Exception(e);
		}			
	}
    public String getName()
    {
    	return "Service";
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
