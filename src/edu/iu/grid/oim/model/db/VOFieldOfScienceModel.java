package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOFieldOfScienceRecord;

public class VOFieldOfScienceModel extends SmallTableModelBase<VOFieldOfScienceRecord> {
    static Logger log = Logger.getLogger(VOFieldOfScienceModel.class); 
	
	public VOFieldOfScienceModel(Context context) {
		super(context, "vo_field_of_science");
	}
	VOFieldOfScienceRecord createRecord() throws SQLException
	{
		return new VOFieldOfScienceRecord();
	}

	
	public ArrayList<VOFieldOfScienceRecord> getByVOID(int vo_id) throws SQLException
	{
		ArrayList<VOFieldOfScienceRecord> list = new ArrayList<VOFieldOfScienceRecord>();
		for(RecordBase rec : getCache()) {
			VOFieldOfScienceRecord vcrec = (VOFieldOfScienceRecord)rec;
			if(vcrec.vo_id.compareTo(vo_id) == 0) list.add(vcrec);
		}		
		return list;
	}
    public String getName()
    {
    	return "Virtual Organization / Field Of Science";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='vo_id']/Value", doc, XPathConstants.STRING));
		VOModel model = new VOModel(context);
		return model.canEdit(id);
	}
}
