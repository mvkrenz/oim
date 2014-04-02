package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.CpuInfoRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class CpuInfoModel extends SmallTableModelBase<CpuInfoRecord> {
    static Logger log = Logger.getLogger(CpuInfoModel.class);  
	
    public CpuInfoModel(UserContext _context) 
    {
    	super(_context, "cpu_info");
    }
    CpuInfoRecord createRecord() throws SQLException
	{
		return new CpuInfoRecord();
	}
	public ArrayList<CpuInfoRecord> getAll() throws SQLException
	{
		ArrayList<CpuInfoRecord> list = new ArrayList<CpuInfoRecord>();
		for(RecordBase it : getCache()) {
			list.add((CpuInfoRecord)it);
		}
		return list;
	}
    public String getName()
    {
    	return "CPU Info";
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
