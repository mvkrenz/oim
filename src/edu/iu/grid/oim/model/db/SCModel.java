package edu.iu.grid.oim.model.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;

public class SCModel extends SmallTableModelBase<SCRecord> {
    static Logger log = Logger.getLogger(SCModel.class);  
    
    public SCModel(Context context) 
    {
    	super(context, "sc");
    }    
    public String getName()
    {
    	return "Support Center";
    }
    SCRecord createRecord() throws SQLException
	{
		return new SCRecord();
	}

	public Collection<SCRecord> getAllEditable() throws SQLException
	{		
		ArrayList<SCRecord> list = new ArrayList();

    	//only select record that is editable
	    for(RecordBase rec : getCache()) {
	    	SCRecord screc = (SCRecord)rec;
	    	if(canEdit(screc.id)) {
	    		list.add(screc);
	    	} 	
	    }
	    return list;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		return canEdit(id);
	}
	public boolean canEdit(int id)
	{
		if(auth.allows("admin")) return true;
		try {
			HashSet<Integer> ints = getEditableIDs();
			if(ints.contains(id)) return true;
		} catch (SQLException e) {
			//TODO - something?
		}
		return false;
	}
	
	//returns all record id that the user has access to
	private HashSet<Integer> getEditableIDs() throws SQLException
	{
		HashSet<Integer> list = new HashSet<Integer>();
		ResultSet rs = null;

		PreparedStatement stmt = null;

		String sql = "SELECT * FROM sc_contact WHERE contact_id = ?";
		stmt = getConnection().prepareStatement(sql); 
		stmt.setInt(1, auth.getContactID());

		rs = stmt.executeQuery();
		while(rs.next()) {
			SCContactRecord rec = new SCContactRecord(rs);
			list.add(rec.sc_id);
		}
		
		return list;
	}
	
	public SCRecord get(int id) throws SQLException {
		SCRecord keyrec = new SCRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	
	public ArrayList<SCRecord> getAll() throws SQLException
	{
		ArrayList<SCRecord> list = new ArrayList<SCRecord>();
		for(RecordBase it : getCache()) {
			list.add((SCRecord)it);
		}
		return list;
	}
	
	public void insertDetail(SCRecord rec, 
			ArrayList<SCContactRecord> contacts) throws Exception
	{
		try {
			
			//process detail information
			getConnection().setAutoCommit(false);
			
			//insert SC itself and get the new ID
			insert(rec);
			
			//process contact information
			SCContactModel cmodel = new SCContactModel(context);
			//reset sc_id on all contact records
			for(SCContactRecord sccrec : contacts) {
				sccrec.sc_id = rec.id;
			}
			cmodel.insert(contacts);
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back SC insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(SCRecord rec,
			ArrayList<SCContactRecord> contacts) throws Exception
	{
		// update to DB
		try {
			//process detail information
			getConnection().setAutoCommit(false);
			
			update(get(rec), rec);
			
			//process contact information
			SCContactModel cmodel = new SCContactModel(context);
			//reset vo_id on all contact records
			for(SCContactRecord sccrec : contacts) {
				sccrec.sc_id = rec.id;
			}
			cmodel.update(cmodel.getBySCID(rec.id), contacts);
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back SC update-insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}			
	}
		
}

