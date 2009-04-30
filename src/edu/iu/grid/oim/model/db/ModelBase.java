package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;

public abstract class ModelBase<T extends RecordBase> {
    static Logger log = Logger.getLogger(ModelBase.class); 
    
    protected Context context;
	protected Authorization auth;
	
    protected String table_name;
    
	protected ModelBase(Context _context, String _table_name)
	{
		context = _context;
		auth = _context.getAuthorization();
		
    	table_name = _table_name;
	}
	protected Connection getConnection()
	{
		return context.getConnection();
	}
	
	//override this to provide human readable value
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		return value;
	}
	
    abstract T createRecord() throws SQLException;
    
	public String getName()
	{
		return getClass().getName();
	}

	//override this to reveal the log to particular user
	abstract public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException;
}
