package edu.iu.grid.oim.model.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;

public abstract class ModelBase<T extends RecordBase> {
    static Logger log = Logger.getLogger(ModelBase.class); 
    
    protected UserContext context;
	protected Authorization auth;
    protected String table_name;
    
	protected ModelBase(UserContext context, String _table_name)
	{
		this.context = context;
		this.auth = context.getAuthorization();
    	table_name = _table_name;
	}

	protected Connection connectOIM() throws SQLException {
		return context.getConnection();
	}
	
	//override this to provide human readable value
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		return value;
	}
	
    //abstract T createRecord() throws SQLException;
    
	public String getName()
	{
		return getClass().getName();
	}
	
    public void remove(RecordBase rec) throws SQLException
    {
		//auth.check("write_"+table_name);
		
    	try {
			//remove all current contacts
	    	String keysql = "";
	    	for(Field key : rec.getRecordKeys()) {
	    		if(keysql.length() != 0) keysql += " and ";
	    		keysql += "`"+key.getName()+"`" + "=?";
	    	}
			String sql = "DELETE FROM "+table_name+" where " + keysql;
			Connection conn = connectOIM();
			PreparedStatement stmt = conn.prepareStatement(sql);
			int count = 1;
			for(Field key : rec.getRecordKeys()) {
	       		Object value = key.get(rec);
	       		stmt.setObject(count, value);
	    		++count;
			}
			stmt.executeUpdate();
			stmt.close();
			conn.close();
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		}
		
		logRemove(rec);
    }
    
    //generated keys are inserted back to rec
    //returns *one of* last inserted record's key. If primary key consists of multiple column, then don't use this
    public Integer insert(RecordBase rec) throws SQLException
    { 	
		//insert new contact records in batch
    	String fields = "";
    	String values = "";
    	for(Field field : rec.getClass().getFields()) {
    		if(fields.length() != 0) {
    			fields += ", ";
    			values += ", ";
    		}
    		fields += "`"+field.getName()+"`";
    		values += "?";
    	}
		String sql = "INSERT INTO "+table_name+" ("+fields+") VALUES ("+values+")";
		Connection conn = connectOIM();
		PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
    	try {
	    	//set field values
	    	int count = 1;
	       	for(Field f : rec.getClass().getFields()) {
	       		Object value = f.get(rec);
	       		stmt.setObject(count, value);
	    		++count;
	    	}         
			stmt.executeUpdate(); 
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		}
		
		//attempt to update rec's key fields with newly inserted table keys (if exists) in the order of the key fields.
		//this should work *most of the time*, but if the key fields are not "auto_increment" or if the key is out-of-order
		//(if it's even possible), then this wouldn't work. we could add a new annotation to the record table and
		//do this in more reliable way..
    	Integer a_id = null;
		ResultSet ids = stmt.getGeneratedKeys();  
		if(ids.next()) {
			int count = 1;
	    	for(Field key : rec.getRecordKeys()) {
	    		try {
	    			Integer value = ids.getInt(count);
	    			if(a_id == null) a_id = value;
					key.set(rec, value);
				} catch (IllegalArgumentException e) {
					log.error(e);
				} catch (IllegalAccessException e) {
					log.error(e);
				}
				++count;
	    	}
		}
		
		logInsert(rec);
		stmt.close();
		conn.close();
		return a_id;
    }
    
    //find out which fields are changed and do SQL update on those fields
    public void update(RecordBase oldrec, RecordBase newrec) throws SQLException
    {
		//auth.check("write_"+table_name);
    	
    	ArrayList<Field> changed_fields = oldrec.diff(newrec);

		//if nothing has being changed, don't update
    	if(changed_fields.size() == 0) return;
    
    	//construct sql
    	String values = ""; 	
    	for(Field f : changed_fields) {
    		if(values.length() != 0) values += ", ";
    		values += f.getName() + "=?";
    	}
    	String keysql = "";
    	for(Field key : oldrec.getRecordKeys()) {
    		if(keysql.length() != 0) keysql += " and ";
    		keysql += "`"+key.getName()+"`" + "=?";
    	}
    	String sql = "UPDATE " + table_name + " SET " + values + " WHERE " + keysql;
    	PreparedStatement stmt;
    	for(Field f : changed_fields) {
    		if(values.length() != 0) values += ", ";
    		values += f.getName() + "=?";
    	}  	
    	Connection conn = connectOIM();
    	stmt = conn.prepareStatement(sql);
    	try {
	    	//set field values
	    	int count = 1;
	       	for(Field f : changed_fields) {
	       		Object value;
				
				value = f.get(newrec);
	       		stmt.setObject(count, value);
	    		++count;
	    	}    
	       	
	       	//set key values
	    	for(Field key : oldrec.getRecordKeys()) {
	    		Object value = (Object) key.get(oldrec);
	    		stmt.setObject(count, value);
	    		++count;
	    	}
	      
			stmt.executeUpdate(); 
			stmt.close(); 	
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		}
		
		logUpdate(oldrec, newrec);
		conn.close();
    }
    
    protected void logInsert(RecordBase rec) throws SQLException 
    {
    	try {
        	String xml = "<Log>\n";
        	xml += "<Type>Insert</Type>\n";
	    	
    		//show key fields
	    	xml += "<Keys>\n";
	    	ArrayList<Field> keys = rec.getRecordKeys();
	    	String keystr = "";
	    	for(Field key : keys) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(rec);   
	    		String svalue = formatValue(value);
	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + svalue + "</Value>\n";
	    		xml += "</Key>\n";
	    		
	    		if(keystr.length() > 0) {
	    			keystr += ".";
	    		}
	    		keystr += svalue;
	    	}
	    	xml += "</Keys>\n";
	    	xml += "<Fields>\n";
	    	for(Field f : rec.getClass().getFields()) {
	    		if(keys.contains(f)) continue;	//don't show key field    		
	    		String name = f.getName();
	    		Object value = f.get(rec);
	    		
	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Field>\n";
	    	}
	    	xml += "</Fields>\n";
	    	xml += "</Log>";
	    	
			LogModel lmodel = new LogModel(context);
			int logid = lmodel.insert("insert", getClass(), xml, keystr);	    

		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		}   	
    }
    
    protected void logRemove(RecordBase rec) throws SQLException 
    {
    	try {
	    	//String plog = "By " + auth.getContact().name;
        	String xml = "<Log>\n";
        	xml += "<Type>Remove</Type>\n";
	    	
    		//show key fields
	    	xml += "<Keys>\n";
	    	ArrayList<Field> keys = rec.getRecordKeys();
	    	String keystr = "";
	    	for(Field key : keys) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(rec);
	    		String svalue = formatValue(value);

	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + svalue + "</Value>\n";
	    		xml += "</Key>\n";
	    		
	    		if(keystr.length() > 0) {
	    			keystr += ".";
	    		}
	    		keystr += svalue;
	    	}
	    	xml += "</Keys>\n";
	    	xml += "<Fields>\n";
	    	for(Field f : rec.getClass().getFields()) {
	    		if(keys.contains(f)) continue;	//don't show key field    		
	    		String name = f.getName();
	    		Object value = f.get(rec);

	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Field>\n";
	    	}
	    	//plog += "</table>";
	    	xml += "</Fields>\n";
	    	xml += "</Log>";
			LogModel lmodel = new LogModel(context);
			int logid = lmodel.insert("remove", getClass(), xml, keystr);	  
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		}
    }
    
    protected void logUpdate(RecordBase oldrec, RecordBase newrec) throws SQLException 
    {   	
    	try {
	    	//String plog = "By " + auth.getContact().name;
        	String xml = "<Log>\n";
        	xml += "<Type>Update</Type>\n";
	    	
    		//show key fields
	    	xml += "<Keys>\n";
	    	String keystr = "";
	    	for(Field key : oldrec.getRecordKeys()) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(oldrec);
	    		String svalue = formatValue(value);

	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + svalue + "</Value>\n";
	    		xml += "</Key>\n";
	    		
	    		if(keystr.length() > 0) {
	    			keystr += ".";
	    		}
	    		keystr += svalue;
	 
	    	}
	    	xml += "</Keys>\n";
	    	xml += "<Fields>\n";
	    	for(Field f : oldrec.diff(newrec)) {
	    		String name = f.getName();
	    		Object oldvalue = f.get(oldrec);
	    		Object newvalue = f.get(newrec);
	    		   		
	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<OldValue>" + formatValue(oldvalue) + "</OldValue>\n";
	    		xml += "\t<NewValue>" + formatValue(newvalue) + "</NewValue>\n";
	    		xml += "</Field>\n";
	    	}
	    	//plog += "</table>";
	    	xml += "</Fields>\n";
	    	xml += "</Log>";
			LogModel lmodel = new LogModel(context);
			int logid = lmodel.insert("update", getClass(), xml, keystr);

		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		} 
    }
    
    private String formatValue(Object obj)
    {
    	if(obj == null) return LogModel.NULL_TOKEN;
    	return StringEscapeUtils.escapeXml(obj.toString());
    }
    

	//override this to reveal the log to particular user
	abstract public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException;
}
