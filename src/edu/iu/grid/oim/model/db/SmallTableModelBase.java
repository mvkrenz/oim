package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;

public abstract class SmallTableModelBase<T extends RecordBase> extends ModelBase<T> {
    static Logger log = Logger.getLogger(SmallTableModelBase.class);  
	private static HashMap<String, TreeSet<RecordBase>> cache = new HashMap();
    
    static private String NonPublicInformation = "(Non-public information)";
    
    public SmallTableModelBase(UserContext user, String _table_name) 
    {
    	super(user, _table_name);
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
		    stmt.close();
		    conn.close();
		    cache.put(table_name, list);
		}
	}
    
    //used after we do insert/update
    protected void emptyCache() 
    {
   		cache.remove(table_name);
    }
    
    //force all SmallTableModelBase derived model's cache to be emptied
    public static void emptyAllCache()
    {
    	cache = new HashMap();
    }
    
	protected TreeSet<RecordBase> getCache() throws SQLException {
		fillCache();
		return cache.get(table_name);
	}
    public T get(T keyrec) throws SQLException
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
			conn.close();
			throw new SQLException(e);
		}
		
		if(rollback) {
			conn.commit();
			conn.setAutoCommit(true);
		}
		emptyCache();
		conn.close();
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
			conn.close();
			throw new SQLException(e);
		}
		
		if(rollback) {
			conn.commit();
			conn.setAutoCommit(true);
		}
		emptyCache();
		conn.close();
	}
  
    public void remove(T rec) throws SQLException {
    	super.remove(rec);
		emptyCache();
    }

    //returns *one of* primary key for record inserted
    public Integer insert(T rec) throws SQLException {
    	Integer a_id = super.insert(rec);
    	emptyCache();
    	return a_id;
    }
   
    public void update(T newrec) throws SQLException {
    	super.update(super.get(newrec),  newrec);
    	emptyCache();
    }
    
    @Deprecated
    //I am deprecating this temporarly.. I should be able to use update(T newrec) now.
    public void update(T oldrec, T newrec) throws SQLException {
    	super.update(oldrec,  newrec);
    	emptyCache();
    }
}
