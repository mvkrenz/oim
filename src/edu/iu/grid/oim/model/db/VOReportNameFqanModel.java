package edu.iu.grid.oim.model.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;

public class VOReportNameFqanModel extends SmallTableModelBase<VOReportNameFqanRecord> {
    static Logger log = Logger.getLogger(VOReportNameFqanModel.class); 

	public VOReportNameFqanModel(Context context) {
		super(context, "vo_report_name_fqan");
	}
	VOReportNameFqanRecord createRecord() throws SQLException
	{
		return new VOReportNameFqanRecord();
	}
	public Collection<VOReportNameFqanRecord> getAll() throws SQLException
	{
		ArrayList<VOReportNameFqanRecord> list = new ArrayList<VOReportNameFqanRecord>();
		for(RecordBase it : getCache()) {
			list.add((VOReportNameFqanRecord)it);
		}
		return list;
	}
	public Collection<VOReportNameFqanRecord> getAllByVOReportNameID(int vo_report_name_id) throws SQLException
	{
		ArrayList<VOReportNameFqanRecord> list = new ArrayList<VOReportNameFqanRecord>();
		for(VOReportNameFqanRecord it : getAll()) {
			if(it.vo_report_name_id.compareTo(vo_report_name_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
	
    public String getName()
    {
    	return "Virtual Organization / Report Name FQAN";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer report_name_id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='vo_report_name_id']/Value", doc, XPathConstants.STRING));
		VOReportNameModel vornmodel = new VOReportNameModel(context);
		VOReportNameRecord vornrec;
		try {
			vornrec = vornmodel.get(report_name_id);
			VOModel model = new VOModel(context);
			return model.canEdit(vornrec.vo_id);
		} catch (SQLException e) {
			log.error(e);
		}
		return false;
	}
}
