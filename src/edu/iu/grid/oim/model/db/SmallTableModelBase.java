package edu.iu.grid.oim.model.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;
import java.sql.PreparedStatement;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;

public abstract class SmallTableModelBase<T extends RecordBase> extends ModelBase<T> {
    static Logger log = Logger.getLogger(SmallTableModelBase.class);  
	private static HashMap<String, TreeSet<RecordBase>> cache = new HashMap();
	abstract T createRecord() throws SQLException;
    
    static private String NonPublicInformation = "(Non-public information)";
    
    public SmallTableModelBase(Context _context, String _table_name) 
    {
    	super(_context, _table_name);
    }    
    
	protected class KeyComparator implements Comparator<RecordBase>
	{
		public int compare(RecordBase o1, RecordBase o2) {
			return o1.compareKeysTo(o2);
		}	
	}
	
    protected void fillCache() throws SQLException
	{
		if(!cache.containsKey(table_name)) {
			TreeSet<RecordBase> list = new TreeSet<RecordBase>(new KeyComparator());
			ResultSet rs = null;
			Connection conn = connectOIM();
			Statement stmt = conn.createStatement();
		    if (stmt.execute("SELECT * FROM "+table_name)) {
		    	rs = stmt.getResultSet();
		    	while(rs.next()) {
		    		RecordBase rec = createRecord();
		    		rec.set(rs);
		    		list.add(rec);
				}
		    }	
		    cache.put(table_name, list);
		}
	}
    protected void emptyCache() //used after we do insert/update
    {
   		cache.remove(table_name);
    }
	protected TreeSet<RecordBase> getCache() throws SQLException {
		fillCache();
		return cache.get(table_name);
	}
    public T get(RecordBase keyrec) throws SQLException
	{
		fillCache();
		TreeSet<RecordBase> mycache = getCache();
		RecordBase candidate = mycache.ceiling(keyrec);
		if(candidate == null) return null;
		if(candidate.compareKeysTo(keyrec) == 0) return (T)candidate;
		return null;
	} 

    public void insert(Collection<T> recs) throws SQLException 
    {
    	Connection conn = connectOIM();
		Boolean rollback = conn.getAutoCommit(); 
		
		if(rollback) {
			conn.setAutoCommit(false);
		}
		try {			
			//find new / updated records
	    	for(T rec : recs) {
	    		insert(rec);
	    	}
		} catch (Exception e) {
			if(rollback) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw new SQLException(e);
		}
		
		if(rollback) {
			conn.commit();
			conn.setAutoCommit(true);
		}
		emptyCache();
    }
    
    //why does client need to supply oldrecs? because ModelBase doesn't know how the
    //list is created.. is it list with same contact_id? or same contact_type? etc..
    public void update(Collection<T> oldrecs, Collection<T> newrecs) throws SQLException 
    {
		//auth.check("write_"+table_name);
	  	
		//if auto commit is true, then do rollback, if caller is handling commit, 
		//then don't do rollback here and let caller do the rollback.
    	Connection conn = connectOIM();
		Boolean rollback = conn.getAutoCommit(); 
		
		if(rollback) {
			conn.setAutoCommit(false);
		}
		
		try {
			//find removed records
			for(T oldrec : oldrecs) {
				Boolean found = false;
				for(RecordBase newrec : newrecs) {
					if(oldrec.compareKeysTo(newrec) == 0) {
						found = true;
						break;
					}
				}
				if(!found) {
					remove(oldrec);
				}
			}
			
			//find new / updated records
	    	for(T newrec : newrecs) {
    			T oldrec = get(newrec);
	    		if(oldrec == null) {
	    			insert(newrec);
	    		} else {
	    	    	if(oldrec.diff(newrec).size() > 0) {
	    	    		update(oldrec, newrec);
	    	    	}
	    		}
	    	}
		} catch (Exception e) {
			if(rollback) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			throw new SQLException(e);
		}
		
		if(rollback) {
			conn.commit();
			conn.setAutoCommit(true);
		}
		emptyCache();
	}
    
    public void remove(RecordBase rec) throws SQLException
    {
		//auth.check("write_"+table_name);
		
    	try {
			//remove all current contacts
	    	String keysql = "";
	    	for(Field key : rec.getRecordKeys()) {
	    		if(keysql.length() != 0) keysql += " and ";
	    		keysql += key.getName() + "=?";
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
    
    //generated keys are inserted back to rec
    public void insert(RecordBase rec) throws SQLException
    { 	
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
		ResultSet ids = stmt.getGeneratedKeys();  
		if(ids.next()) {
			int count = 1;
	    	for(Field key : rec.getRecordKeys()) {
	    		try {
	    			Integer value = ids.getInt(count);
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
		emptyCache();
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
    		keysql += key.getName() + "=?";
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
		
		emptyCache();
    }

    private String formatValue(Object obj)
    {
    	if(obj == null) return LogModel.NULL_TOKEN;
    	return StringEscapeUtils.escapeXml(obj.toString());
    }
    
    protected void logInsert(RecordBase rec) throws SQLException 
    {
    	try {
    		//String plog = "By " + auth.getContact().name;;
        	String xml = "<Log>\n";
        	xml += "<Type>Insert</Type>\n";
	    	
    		//show key fields
        	//plog += "<table width=\"100%\">";
	    	xml += "<Keys>\n";
	    	ArrayList<Field> keys = rec.getRecordKeys();
	    	for(Field key : keys) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(rec);
/*
	    		plog += "<tr><th>"+StringEscapeUtils.escapeHtml(name)+"</th>"+
	    			"<td>"+StringEscapeUtils.escapeHtml(rec.toString(key, auth))+"</td></tr>";
*/    		
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
	    		Object value = f.get(rec);
	    		
	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Field>\n";
	    		/*
	    		if(rec.isRestricted(f)) {
	    			value = NonPublicInformation;
	    		}
	    		*/
/*
    			plog += "<tr><th>"+StringEscapeUtils.escapeHtml(name)+"</th>"+
    			"<td>"+StringEscapeUtils.escapeHtml(rec.toString(f, auth))+"</td></tr>";	
 */
	    	}
	    	//plog += "</table>";
	    	xml += "</Fields>\n";
	    	xml += "</Log>";
	    	
			LogModel lmodel = new LogModel(context);
			int logid = lmodel.insert("insert", getClass().getName(), xml);	    
			/*
			plog += "Log ID " + logid;

			try {
				PublicNotification.publish("Inserted " + rec.getTitle(), plog, rec.getLables());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
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
	    	//plog += "<table width=\"100%\">";
	    	xml += "<Keys>\n";
	    	ArrayList<Field> keys = rec.getRecordKeys();
	    	for(Field key : keys) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(rec);
	    		/*
	    		plog += "<tr><th>"+StringEscapeUtils.escapeHtml(name)+"</th>"+
	    			"<td>"+StringEscapeUtils.escapeHtml(rec.toString(key, auth))+"</td></tr>";
	    		*/
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
	    		Object value = f.get(rec);

	    		xml += "<Field>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Field>\n";
	    		/*
	    		if(rec.isRestricted(f)) {
	    			value = NonPublicInformation;
	    		}
	    		*/
	    		/*
    			plog += "<tr><th>"+StringEscapeUtils.escapeHtml(name)+"</th>"+
    			"<td>"+StringEscapeUtils.escapeHtml(rec.toString(f, auth))+"</td></tr>";
    			*/
	    	}
	    	//plog += "</table>";
	    	xml += "</Fields>\n";
	    	xml += "</Log>";
			LogModel lmodel = new LogModel(context);
			int logid = lmodel.insert("remove", getClass().getName(), xml);	  
			/*
			plog += "Log ID " + logid;
			
			try {
				PublicNotification.publish("Removed " + rec.getTitle(), plog, rec.getLables());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
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
        	//plog += "<table width=\"100%\"><tr><th>Updated Field</th><th>Old Value</th><th>New Value</th></tr>";
	    	xml += "<Keys>\n";
	    	for(Field key : oldrec.getRecordKeys()) {
	    		String name = key.getName();
	    		Object value = (Object) key.get(oldrec);
	    		
	    		//don't need to show the record key for updates
	    		/*	
	    		plog += "<tr>";
	    		plog += "<th>"+StringEscapeUtils.escapeHtml(name)+"</th>"+
    			"<td>"+StringEscapeUtils.escapeHtml(oldrec.toString(value, auth))+"</td>";
	    		plog += "<td></td></tr>";
	    		*/
	    		
	    		xml += "<Key>\n";
	    		xml += "\t<Name>" + StringEscapeUtils.escapeXml(name) + "</Name>\n";
	    		xml += "\t<Value>" + formatValue(value) + "</Value>\n";
	    		xml += "</Key>\n";
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
	    		/*
	    		if(oldrec.isRestricted(f)) {
	    			oldvalue = NonPublicInformation;
	    			newvalue = NonPublicInformation;
	    		}
	    		*/
	    		/*
	    		plog += "<tr><th>"+StringEscapeUtils.escapeHtml(name)+"</th>"+
    			"<td>"+StringEscapeUtils.escapeHtml(oldrec.toString(f, auth))+"</td>" +
    			"<td>"+StringEscapeUtils.escapeHtml(newrec.toString(f, auth))+"</td></tr>";
    			*/
	    	}
	    	//plog += "</table>";
	    	xml += "</Fields>\n";
	    	xml += "</Log>";
			LogModel lmodel = new LogModel(context);
			int logid = lmodel.insert("update", getClass().getName(), xml);
			/*
			plog += "Log ID " + logid;
			try {
				PublicNotification.publish("Updated " + oldrec.getTitle(), plog, oldrec.getLables());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
		} catch (IllegalArgumentException e) {
			throw new SQLException(e);
		} catch (IllegalAccessException e) {
			throw new SQLException(e);
		} catch (SecurityException e) {
			throw new SQLException(e);
		} 
    }
   
}
