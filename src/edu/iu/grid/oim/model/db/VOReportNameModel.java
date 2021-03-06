package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;

public class VOReportNameModel extends SmallTableModelBase<VOReportNameRecord> {
    static Logger log = Logger.getLogger(VOReportNameModel.class); 

	public VOReportNameModel(UserContext context) {
		super(context, "vo_report_name");
	}
		VOReportNameRecord createRecord() throws SQLException
	{
		return new VOReportNameRecord();
	}
	public ArrayList<VOReportNameRecord> getAll() throws SQLException
	{
		ArrayList<VOReportNameRecord> list = new ArrayList<VOReportNameRecord>();
		for(RecordBase it : getCache()) {
			list.add((VOReportNameRecord)it);
		}
		return list;
	}
	public VOReportNameRecord get(int vo_report_name_id) throws SQLException
	{
		VOReportNameRecord keyrec = new VOReportNameRecord();
		keyrec.id = vo_report_name_id;
		return get(keyrec);
	}
	public ArrayList<VOReportNameRecord> getAllByVOID(int vo_id) throws SQLException
	{
		ArrayList<VOReportNameRecord> list = new ArrayList<VOReportNameRecord>();
		for(VOReportNameRecord it : getAll()) {
			if(it.vo_id.compareTo(vo_id) == 0) {
				list.add(it);
			}
		}
		return list;		
	}
    public String getName()
    {
    	return "Virtual Organization Report Name";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	} 
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		String str = (String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING);
		Integer id = Integer.parseInt(str);
		VOModel model = new VOModel(context);
		return model.canEdit(id);
	}
}
