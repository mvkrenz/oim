package edu.iu.grid.oim.model.db.record;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.servlet.ServletBase;

public class RecordBase {
    static Logger log = Logger.getLogger(ServletBase.class);  
    
    //use reflection to figure out how to read this record
	public RecordBase(ResultSet rs) throws SQLException
	{
    	try {
			Field[] fields = getClass().getFields();
			for(Field fld : fields) {
				String name = fld.getName();
				String type = fld.getType().toString();
		        if(type.compareTo("class java.lang.String") == 0) {
					fld.set(this, rs.getString(name));
		        } else if(type.compareTo("class java.lang.Integer") == 0) {
		        	fld.set(this, rs.getInt(name));
		        	if(rs.wasNull()) {
		        		fld.set(this, null);
		        	}
		        } else if(type.compareTo("class java.lang.Float") == 0) {
		        	fld.set(this, rs.getFloat(name));
		        } else if(type.compareTo("class java.lang.Double") == 0) {
		        	fld.set(this, rs.getDouble(name));
		        } else if(type.compareTo("class java.sql.Timestamp") == 0) {
		        	fld.set(this, rs.getTimestamp(name));
		        } else if(type.compareTo("class java.lang.Boolean") == 0) {
		        	fld.set(this, rs.getBoolean(name));
		        } else {
		        	log.error("Uknown record variable type (ctor):" + type + " called " + name);
		        }
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public RecordBase() {}
	
	//list fields that are different
	public ArrayList<Field> diff(RecordBase rec)
	{
		ArrayList<Field> diff = new ArrayList();
		try {
			Field[] fields = getClass().getFields();
			for(Field fld : fields) {
				String name = fld.getName();
				String type = fld.getType().getName();
				Comparable me = (Comparable)fld.get(this);
				Comparable you = (Comparable)fld.get(rec);				
	        	if(me != you || me.compareTo(you) != 0) {
	        		diff.add(fld);
	        	}
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return diff;
	}

}
