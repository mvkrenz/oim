package edu.iu.grid.oim.model.db.record;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;
import edu.iu.grid.oim.servlet.ServletBase;

public abstract class RecordBase implements Comparable<RecordBase> {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Key {}
	
	static Logger log = Logger.getLogger(ServletBase.class);  
    
    //use reflection to figure out how to read this record
	public RecordBase(ResultSet rs) throws SQLException
	{		
		set(rs);
	}
	
	public void set(ResultSet rs) throws SQLException
	{
    	try {
			Field[] fields = getClass().getFields();
			for(Field fld : fields) {
				String name = fld.getName();
				Class type = fld.getType();
				
				//We can't use switch for Class type
		        if(type == String.class) {
					fld.set(this, rs.getString(name));
		        } else if(type == Integer.class) {
		        	fld.set(this, rs.getInt(name));
		        } else if(type == Float.class) {
		        	fld.set(this, rs.getFloat(name));
		        } else if(type == Double.class) {
		        	fld.set(this, rs.getDouble(name));
		        } else if(type == Timestamp.class) {
		        	fld.set(this, rs.getTimestamp(name));
		        } else if(type == Boolean.class) {
		        	fld.set(this, rs.getBoolean(name));
		        } else if(type == Date.class) {
		        	fld.set(this, rs.getDate(name));
		        } else {
		        	log.error("Uknown record variable type (ctor):" + type + " called " + name);
		        }
	        	if(rs.wasNull()) {
	        		fld.set(this, null);
	        	}
			}
		} catch (IllegalArgumentException e) {
			throw new SQLException(getClass().getName() + " " + e.toString());
		} catch (IllegalAccessException e) {
			throw new SQLException(getClass().getName() + " " + e.toString());
		}
	}
	
	public RecordBase() 
	{
	}
	
	//cache key field since looking up annotation is slow
	static HashMap<Class, ArrayList<Field>> record_keys = new HashMap<Class, ArrayList<Field>>();
	static HashMap<Class, ArrayList<Field>> record_fields = new HashMap<Class, ArrayList<Field>>();
	public ArrayList<Field> getRecordKeys()
	{
		if(!record_keys.containsKey(getClass())) {
			cacheRecordInfo();
		}
		return record_keys.get(getClass());
	}
	public ArrayList<Field> getRecordFields()
	{
		if(!record_fields.containsKey(getClass())) {
			cacheRecordInfo();
		}
		return record_fields.get(getClass());
	}
	private void cacheRecordInfo()
	{
		ArrayList<Field> keys = new ArrayList<Field>();
		ArrayList<Field> fields = new ArrayList<Field>();
		Field[] all_fields = getClass().getFields();
		for(Field field : all_fields) {
			//all variables are considered to be a field
			fields.add(field);
			
			Annotation[] annotations = field.getDeclaredAnnotations();
			for(Annotation annotation : annotations){	
				//add @key variables to key list
				if(annotation instanceof Key){
			    	keys.add(field);	
			    }
			}
		}
		record_keys.put(getClass(), keys);
		record_fields.put(getClass(), fields);
	}

	//list fields that are different
	public ArrayList<Field> diff(RecordBase rec)
	{
		ArrayList<Field> diff = new ArrayList<Field>();
		try {
			ArrayList<Field> fields = getRecordFields();
			for(Field fld : fields) {
				Comparable me = (Comparable)fld.get(this);
				Comparable you = (Comparable)fld.get(rec);
				if(me == you) continue;
				if(me == null || you == null) {
					diff.add(fld);
				} else if(me.compareTo(you) != 0) {
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
	
	//this is super slow.. override this if it becomes a problem
	public int compareTo(RecordBase o) {
		ArrayList<Field> diff = diff(o);
		if(diff.size() == 0) return 0;
		return 1;
	}

	public int compareKeysTo(RecordBase o)
	{
		try {
			for(Field fld : getRecordKeys()) {
				//this is the key field - let's compare
				Comparable me = (Comparable)fld.get(this);
				Comparable you = (Comparable)fld.get(o);				
				if(me == you) continue;
				if(me == null) {
					//me can't  be null since we are doing me.compareTo later
					return -1;
				}
				if(you == null) {
					//you can't be null since I can't pass null object to compareTo
					return 1;
					//throw new NullPointerException("Key Field [" + fld.getName() + "] in [" + o.getClass().getName()+ "] is null while comparing keys");
				}				
	        	int cmp = me.compareTo(you);
	        	if(cmp != 0) return cmp;
			}
		} catch (IllegalArgumentException e) {
			//nothing I can do?
		} catch (IllegalAccessException e) {
			//nothing I can do?
		}	
		return 0;	
	}
	
	//override to return the record title to use for various human readable places 
	public String getTitle()
	{
		//by default's return the ugly class name
		return getClass().getName();
	}
	//return to set labels for public log
	public ArrayList<String> getLables()
	{
		ArrayList<String> lables = new ArrayList();
		return lables;
	}

}
