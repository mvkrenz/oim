package edu.iu.grid.oim.model.db.record;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.servlet.ServletBase;

public abstract class RecordBase implements Comparable<RecordBase> {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Key {}

	static Logger log = Logger.getLogger(ServletBase.class);  
	static HashMap<Class, ArrayList<Field>> keys = new HashMap<Class, ArrayList<Field>>();
    
    //use reflection to figure out how to read this record
	public RecordBase(ResultSet rs) throws SQLException
	{		
    	try {
			Field[] fields = getClass().getFields();
			for(Field fld : fields) {
				String name = fld.getName();
				Class type = fld.getType();
		        if(type == String.class) {
					fld.set(this, rs.getString(name));
		        } else if(type == Integer.class) {
		        	fld.set(this, rs.getInt(name));
		        	if(rs.wasNull()) {
		        		fld.set(this, null);
		        	}
		        } else if(type == Float.class) {
		        	fld.set(this, rs.getFloat(name));
		        } else if(type == Double.class) {
		        	fld.set(this, rs.getDouble(name));
		        } else if(type == Timestamp.class) {
		        	fld.set(this, rs.getTimestamp(name));
		        } else if(type == Boolean.class) {
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
	
	public RecordBase() 
	{
	}
	
	public ArrayList<Field> getKeys()
	{
		if(!keys.containsKey(getClass())) {
			ArrayList<Field> ks = new ArrayList<Field>();
			Field[] fields = getClass().getFields();
			for(Field field : fields) {
				Annotation[] annotations = field.getDeclaredAnnotations();
				for(Annotation annotation : annotations){
				    if(annotation instanceof Key){
				        ks.add(field);
				        break;
				    }
				}
			}
			keys.put(getClass(), ks);
		}
		return keys.get(getClass());
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
			for(Field fld : getKeys()) {
				//this is the key field - let's compare
				Comparable me = (Comparable)fld.get(this);
				Comparable you = (Comparable)fld.get(o);				
				if(me == you) continue;
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
