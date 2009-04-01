package edu.iu.grid.oim.model.db.record;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.servlet.ServletBase;

public class RecordBase implements Comparable, IKeyComparable {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Key {}
	
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
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		}
	}
	public RecordBase() {}
	
	public ArrayList<Field> getKeys()
	{
		ArrayList<Field> keys = new ArrayList<Field>();
		Field[] fields = getClass().getFields();
		for(Field field : fields) {
			Annotation[] annotations = field.getDeclaredAnnotations();
			for(Annotation annotation : annotations){
			    if(annotation instanceof Key){
			        keys.add(field);
			        break;
			    }
			}
		}
		return keys;
	}
	
	//list fields that are different
	public ArrayList<Field> diff(RecordBase rec)
	{
		ArrayList<Field> diff = new ArrayList<Field>();
		try {
			Field[] fields = getClass().getFields();
			for(Field fld : fields) {
				Comparable me = (Comparable)fld.get(this);
				Comparable you = (Comparable)fld.get(rec);				
	        	if(me != you && me.compareTo(you) != 0) {
	        		diff.add(fld);
	        	}
			}
		} catch (IllegalArgumentException e) {
			//what can I do??
		} catch (IllegalAccessException e) {
			//what can I do??
		}
		return diff;
	}
	
	public int compareTo(Object o) {
		ArrayList<Field> diff = diff((RecordBase)o);
		if(diff.size() == 0) return 0;
		return 1;
	}

	public int compareKeyTo(Object o) {
		ArrayList<Field> keys = getKeys();
		try {
			Field[] fields = getClass().getFields();
			for(Field fld : fields) {
				if(keys.contains(fld)) {
					//this is the key field - let's compare
					Comparable me = (Comparable)fld.get(this);
					Comparable you = (Comparable)fld.get(o);				
		        	if(me != you && me.compareTo(you) != 0) {
		        		return 1; //it's different
		        	}
				}
			}
		} catch (IllegalArgumentException e) {
			//nothing I can do?
		} catch (IllegalAccessException e) {
			//nothing I can do?
		}
		return 0;
	}
}
