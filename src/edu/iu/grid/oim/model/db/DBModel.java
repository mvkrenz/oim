package edu.iu.grid.oim.model.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.PreparedStatement;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class DBModel {
    static Logger log = Logger.getLogger(DBModel.class);  
    protected Connection con;
    protected Authorization auth;
    
    public DBModel(Connection _con, Authorization _auth) 
    {
    	con = _con;
    	auth = _auth;
    }    
    
    public void updateChangedFields(String table_name, RecordBase oldrec, RecordBase newrec) throws SQLException
    {
    	ArrayList<Field> changed_fields = oldrec.diff(newrec);
    	if(changed_fields.size() == 0) {
    		//if nothing has being changed, don't update
    		return;
    	}
    	
    	//construct SQL to do the update
    	String values = ""; 	
    	for(Field f : changed_fields) {
    		if(values.length() != 0) values += ", ";
    		values += f.getName() + "=?";
    	}
    	String sql = "UPDATE " + table_name + " SET " + values + " WHERE id=?";
    	PreparedStatement stmt;
    	for(Field f : changed_fields) {
    		if(values.length() != 0) values += ", ";
    		values += f.getName() + "=?";
    	}  	
    	
    	stmt = con.prepareStatement(sql);
    	String log = "";
    	
    	//catch all reflection related exceptions
    	try {
    		//This completely break the OO principle..
        	Integer id = (Integer) oldrec.getClass().getField("id").get(oldrec);
    		
        	//set values for updates
	    	int count = 1;
	       	for(Field f : changed_fields) {
	       		Object value;
				
				value = f.get(newrec);
	       		stmt.setObject(count, value);
	    		++count;
	    	}    
	       	//set key value
	       	stmt.setObject(count, id);
	    	
			//construct the log and insert it to log table
	    	log += "<Update>\n";
	    	log += "<TableName>" + table_name + "</TableName>\n";
	    	log += "<ID>" + id + "</ID>\n";
	    	//StringEscapeUtils.escapeXml(str)
	    	for(Field f : changed_fields) {
	    		String name = f.getName();
	    		String oldvalue = f.get(oldrec).toString();
	    		String newvalue = f.get(newrec).toString();
	    		log += "<Field>\n";
	    		log += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		log += "\t<OldValue>" + StringEscapeUtils.escapeXml(oldvalue) + "</OldValue>\n";
	    		log += "\t<NewValue>" + StringEscapeUtils.escapeXml(newvalue) + "</NewValue>\n";
	    		log += "</Field>\n";
	    	}  
	    	log += "</Update>";
			LogModel lmodel = new LogModel(con, auth);
			lmodel.insert("update_" + table_name, id, log);
	    	
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		stmt.executeUpdate(); 
		stmt.close(); 	
    }
}
