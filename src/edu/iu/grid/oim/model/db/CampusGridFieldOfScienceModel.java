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
import edu.iu.grid.oim.model.db.record.CampusGridFieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.CampusGridRecord;

public class CampusGridFieldOfScienceModel extends SmallTableModelBase<CampusGridFieldOfScienceRecord> {
    static Logger log = Logger.getLogger(CampusGridFieldOfScienceModel.class); 
	
	public CampusGridFieldOfScienceModel(UserContext context) {
		super(context, "campusgrid_field_of_science");
	}
	CampusGridFieldOfScienceRecord createRecord() throws SQLException
	{
		return new CampusGridFieldOfScienceRecord();
	}
	public ArrayList<CampusGridFieldOfScienceRecord> getByCampusGridID(int cg_id) throws SQLException
	{
		ArrayList<CampusGridFieldOfScienceRecord> list = new ArrayList<CampusGridFieldOfScienceRecord>();
		for(RecordBase rec : getCache()) {
			CampusGridFieldOfScienceRecord vcrec = (CampusGridFieldOfScienceRecord)rec;
			if(vcrec.campusgrid_id.compareTo(cg_id) == 0) list.add(vcrec);
		}		
		return list;
	}
    public String getName()
    {
    	return "Campus Grid / Field Of Science";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			CampusGridModel model = new CampusGridModel(context);
			CampusGridRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("field_of_science_id")) {
			FieldOfScienceModel model = new FieldOfScienceModel(context);
			FieldOfScienceRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}    
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='vo_id']/Value", doc, XPathConstants.STRING));
		VOModel model = new VOModel(context);
		return model.canEdit(id);
	}
}
