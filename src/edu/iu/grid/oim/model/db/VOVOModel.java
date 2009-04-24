package edu.iu.grid.oim.model.db;

import java.sql.SQLException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.db.record.VOVORecord;

public class VOVOModel extends SmallTableModelBase<VOVORecord> {
    static Logger log = Logger.getLogger(VOVOModel.class);  
	
    public VOVOModel(edu.iu.grid.oim.lib.Authorization _auth) 
    {
    	super(_auth, "vo_vo");
    }
    VOVORecord createRecord() throws SQLException
	{
		return new VOVORecord();
	}
	public VOVORecord get(int id) throws SQLException {
		VOVORecord keyrec = new VOVORecord();
		keyrec.child_vo_id = id;
		return get(keyrec);
	}
    public String getName()
    {
    	return "Parent Virtual Organization";
    }
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='child_vo_id']/Value", doc, XPathConstants.STRING));
		VOModel model = new VOModel(auth);
		return model.canEdit(id);
	}
}
