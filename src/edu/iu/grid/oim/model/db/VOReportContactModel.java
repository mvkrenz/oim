package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;

public class VOReportContactModel extends SmallTableModelBase<VOReportContactRecord> {
    static Logger log = Logger.getLogger(VOReportContactModel.class); 
	
	public VOReportContactModel(Authorization _auth) {
		super(_auth, "vo_report_contact");
		// TODO Auto-generated constructor stub
	}
	VOReportContactRecord createRecord() throws SQLException
	{
		return new VOReportContactRecord();
	}

	public Collection<VOReportContactRecord> getAllByVOReportNameID(int vo_report_name_id) throws SQLException
	{ 
		ArrayList<VOReportContactRecord> list = new ArrayList<VOReportContactRecord>();
		for(RecordBase record : getCache()) {
			VOReportContactRecord vorc_record = (VOReportContactRecord)record;
			if(vorc_record.vo_report_name_id == vo_report_name_id) list.add(vorc_record);
		}
		return list;
	}	
    public String getName()
    {
    	return "Virtual Organization Report Contact";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer report_name_id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='vo_report_name_id']/Value", doc, XPathConstants.STRING));
		VOReportNameModel vornmodel = new VOReportNameModel(auth);
		VOReportNameRecord vornrec;
		try {
			vornrec = vornmodel.get(report_name_id);
			VOModel model = new VOModel(auth);
			return model.canEdit(vornrec.vo_id);
		} catch (SQLException e) {
			log.error(e);
		}
		return false;
	}
}
