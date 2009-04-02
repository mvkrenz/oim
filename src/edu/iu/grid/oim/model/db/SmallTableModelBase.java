package edu.iu.grid.oim.model.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.sql.PreparedStatement;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.RecordBase;

public abstract class SmallTableModelBase<T extends RecordBase> {
    static Logger log = Logger.getLogger(SmallTableModelBase.class);  
	private static HashMap<String, ArrayList<RecordBase>> cache = new HashMap();
	abstract T createRecord(ResultSet rs) throws SQLException;
	
    protected Connection con;
    protected Authorization auth;
    protected String table_name;
   
    public SmallTableModelBase(Connection _con, Authorization _auth, String _table_name) 
    {
    	con = _con;
    	auth = _auth;
    	table_name = _table_name;
    }    

    protected void fillCache() throws SQLException
	{
		if(!cache.containsKey(table_name)) {
			ArrayList<RecordBase> list = new ArrayList<RecordBase>();
			ResultSet rs = null;
			Statement stmt = con.createStatement();
		    if (stmt.execute("SELECT * FROM "+table_name)) {
		    	 rs = stmt.getResultSet();
		    }
		    while(rs.next()) {
		    	RecordBase rec = createRecord(rs);
		    	list.add(rec);
		    }	
		    cache.put(table_name, list);
		}
	}
    protected void emptyCache() //used after we do insert/update
    {
   		cache.remove(table_name);
    }
	public ArrayList<RecordBase> getCache() throws SQLException {
		fillCache();
		return cache.get(table_name);
	}
    public T get(RecordBase keyrec) throws SQLException
	{
		fillCache();
		for(RecordBase rec : getCache()) {
			if(rec.compareKeysTo(keyrec) == 0) return (T)rec;
		}
		return null;
	}
  
    //why does client need to supply oldrecs? because ModelBase doesn't know how the
    //list is created.. is it list with same contact_id? or same contact_type? etc..
    public void update(Collection<T> oldrecs, Collection<T> newrecs) throws SQLException, AuthorizationException 
    {
		auth.check("write_"+table_name);
	  	
		//if auto commit is true, then do rollback, if caller is handling commit, 
		//then don't do rollback here and let caller do the rollback.
		Boolean rollback = con.getAutoCommit(); 
		
		if(rollback) {
			con.setAutoCommit(false);
		}
		
		try {
			//find removed records
			for(T oldrec : oldrecs) {
				Boolean found = false;
				for(RecordBase newrec : newrecs) {
					if(oldrec.compareTo(newrec) == 0) {
						found = true;
						break;
					}
				}
				if(!found) {
					remove(oldrec);
					logRemove(oldrec);
				}
			}
			
			//find new / updated records
	    	for(T newrec : newrecs) {
	    		T oldrec = get(newrec);
	    		if(oldrec == null) {
	    			insert(newrec);
	    			logInsert(newrec);
	    		} else {
	    	    	if(oldrec.diff(newrec).size() > 0) {
	    	    		update(oldrec, newrec);
	    	    		logUpdate(oldrec, newrec);
	    	    	}
	    		}
	    	}
		} catch (SQLException e) {
			if(rollback) {
				con.rollback();
				con.setAutoCommit(true);
			}
			throw new SQLException(e);
		}
		
		if(rollback) {
			con.commit();
			con.setAutoCommit(true);
		}
		emptyCache();
	}
    
    public void remove(RecordBase rec) throws SQLException, AuthorizationException
    {
		auth.check("write_"+table_name);
		
    	try {
			//remove all current contacts
	    	String keysql = "";
	    	for(Field key : rec.getKeys()) {
	    		if(keysql.length() != 0) keysql += " and ";
	    		keysql += key.getName() + "=?";
	    	}
			String sql = "DELETE FROM "+table_name+" where " + keysql;
			PreparedStatement stmt = con.prepareStatement(sql);
			int count = 1;
			for(Field key : rec.getKeys()) {
	       		Object value;
				value = key.get(rec);
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
		
		logRemove(rec);
		
		emptyCache();
    }
    
    //returns generated keys
    public ResultSet insert(RecordBase rec) throws SQLException, AuthorizationException
    {
		auth.check("write_"+table_name);
    	
		//insert new contact records in batch
    	String fields = "";
    	String values = "";
    	for(Field field : rec.getClass().getFields()) {
    		if(fields.length() != 0) {
    			fields += ", ";
    			values += ", ";
    		}
    		fields += field.getName();
    		values += "?";
    	}
		String sql = "INSERT INTO "+table_name+" ("+fields+") VALUES ("+values+")";
		PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS); 
    	try {
	    	//set field values
	    	int count = 1;
	       	for(Field f : rec.getClass().getFields()) {
	       		Object value;
				value = f.get(rec);
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
		
		logInsert(rec);
		
		emptyCache();
		
		//return the generated key
		ResultSet ids = stmt.getGeneratedKeys();
		if(ids.next()) {
			return ids;
		} else {
			return null;
		}
    }
    //find out which fields are changed and do SQL update on those fields
    public void update(RecordBase oldrec, RecordBase newrec) throws SQLException, AuthorizationException
    {
		auth.check("write_"+table_name);
    	
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
    	for(Field key : oldrec.getKeys()) {
    		if(keysql.length() != 0) keysql += " and ";
    		keysql += key.getName() + "=?";
    	}
    	String sql = "UPDATE " + table_name + " SET " + values + " WHERE " + keysql;
    	PreparedStatement stmt;
    	for(Field f : changed_fields) {
    		if(values.length() != 0) values += ", ";
    		values += f.getName() + "=?";
    	}  	
    	stmt = con.prepareStatement(sql);
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
	    	for(Field key : oldrec.getKeys()) {
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
		
		emptyCache();
    }

    private String formatValue(Object obj)
    {
    	if(obj == null) return "##null##";
    	return StringEscapeUtils.escapeXml(obj.toString());
    }
    
    protected void logInsert(RecordBase rec) throws SQLException 
    {
    	try {
        	String xml = "<Insert>\n";
	    	
    		//show key fields
	    	xml += "<Keys>\n";
	    	ArrayList<Field> keys = rec.getKeys();
	    	for(Field key : keys) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(rec);
	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Key>\n";
	    	}
	    	xml += "</Keys>\n";
	    	xml += "<Fields>\n";
	    	for(Field f : rec.getClass().getFields()) {
	    		if(keys.contains(f)) continue;	//don't show key field    		
	    		String name = f.getName();
	    		String value = f.get(rec).toString();
	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Field>\n";
	    	}
	    	xml += "</Fields>\n";
	    	xml += "</Insert>";
			LogModel lmodel = new LogModel(con, auth);
			lmodel.insert("insert", rec.getClass().getName(), xml);	    	
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
	    	
        	String xml = "<Remove>\n";
	    	
    		//show key fields
	    	xml += "<Keys>\n";
	    	ArrayList<Field> keys = rec.getKeys();
	    	for(Field key : keys) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(rec);
	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Key>\n";
	    	}
	    	xml += "</Keys>\n";
	    	xml += "<Fields>\n";
	    	for(Field f : rec.getClass().getFields()) {
	    		if(keys.contains(f)) continue;	//don't show key field    		
	    		String name = f.getName();
	    		String value = f.get(rec).toString();
	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Field>\n";
	    	}
	    	xml += "</Fields>\n";
	    	xml += "</Remove>";
			LogModel lmodel = new LogModel(con, auth);
			lmodel.insert("remove", rec.getClass().getName(), xml);	    	
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
	    	
        	String xml = "<Update>\n";
	    	
    		//show key fields
	    	xml += "<Keys>\n";
	    	for(Field key : oldrec.getKeys()) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(oldrec);
	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Key>\n";
	    	}
	    	xml += "</Keys>\n";
	    	xml += "<Fields>\n";
	    	for(Field f : oldrec.diff(newrec)) {
	    		String name = f.getName();
	    		String oldvalue = f.get(oldrec).toString();
	    		String newvalue = f.get(newrec).toString();
	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<OldValue>" + formatValue(oldvalue) + "</OldValue>\n";
	    		xml += "\t<NewValue>" + formatValue(newvalue) + "</NewValue>\n";
	    		xml += "</Field>\n";
	    	}
	    	xml += "</Fields>\n";
	    	
	    	xml += "</Update>";
			LogModel lmodel = new LogModel(con, auth);
			lmodel.insert("update", newrec.getClass().getName(), xml);	    	
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		}
    }
   
}
