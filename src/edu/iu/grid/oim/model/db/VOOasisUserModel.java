package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.VOOasisUserRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOOasisUserModel extends SmallTableModelBase<VOOasisUserRecord> {
    static Logger log = Logger.getLogger(VOOasisUserModel.class); 
	
	public VOOasisUserModel(UserContext context) {
		super(context, "vo_oasis_user");
	}
	
	@Override
	VOOasisUserRecord createRecord() throws SQLException {
		return new VOOasisUserRecord();
	}
	public ArrayList<VOOasisUserRecord> getByVOID(int vo_id) throws SQLException
	{ 
		ArrayList<VOOasisUserRecord> list = new ArrayList<VOOasisUserRecord>();
		for(RecordBase rec : getCache()) {
			VOOasisUserRecord vcrec = (VOOasisUserRecord)rec;
			if(vcrec.vo_id == vo_id) list.add(vcrec);
		}
		return list;
	}	

    public String getName()
    {
    	return "Virtual Organization OASIS Users";
    }
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("vo_id")) {
			VOModel model = new VOModel(context);
			VORecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_id")) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
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
