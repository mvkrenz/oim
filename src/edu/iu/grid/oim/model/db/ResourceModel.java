package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class ResourceModel extends SmallTableModelBase<ResourceRecord> {
    static Logger log = Logger.getLogger(ResourceModel.class);  
    
    public ResourceModel(Context context) 
    {
    	super(context, "resource");
    }
    public String getName()
    {
    	return "Resource";
    }
    public ResourceRecord createRecord() throws SQLException
	{
		return new ResourceRecord();
	}
	
	public ArrayList<ResourceRecord> getByGroupID(int group_id) throws SQLException
	{
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord>();
		for(RecordBase rec : getCache()) {
			ResourceRecord vcrec = (ResourceRecord)rec;
			if(vcrec.resource_group_id == group_id) list.add(vcrec);
		}
		return list;
	}
	public Collection<ResourceRecord> getAllEditable() throws SQLException
	{	   
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord>();
    	//only select record that is editable
	    for(RecordBase id : getCache()) {
	    	ResourceRecord rec = (ResourceRecord)id;
	    	if(canEdit(rec.id)) {
	    		list.add(rec);
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
		VOContactModel model = new VOContactModel(context);
		Collection<VOContactRecord> vcrecs = model.getByContactID(auth.getContactID());
		for(VOContactRecord rec : vcrecs) {
			list.add(rec.vo_id);
		}
		return list;
	}
	public ResourceRecord get(int id) throws SQLException {
		ResourceRecord keyrec = new ResourceRecord();
		keyrec.id = id;
		return get(keyrec);
	}
	public ArrayList<ResourceRecord> getAll() throws SQLException
	{
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord>();
		for(RecordBase it : getCache()) {
			list.add((ResourceRecord)it);
		}
		return list;
	}
	
	public void insertDetail(ResourceRecord rec, 
			ArrayList<String> resource_aliases,
			ArrayList<ResourceContactRecord> contacts,
			ResourceWLCGRecord wrec,
			ArrayList<ResourceServiceRecord> resource_services/*,
			ArrayList<ResourceDowntimeEditorDE.DowntimeEditor> downtimes,
			HashMap<DowntimeEditor, ArrayList<ResourceDowntimeServiceRecord>> downtime_services*/) throws Exception
	{
		try {
			
			//process detail information
			getConnection().setAutoCommit(false);
			
			//insert resource itself and insert() will set rec.id with newly created id
			insert(rec);
			
			//process contact information
			ResourceContactModel cmodel = new ResourceContactModel(context);
			//reset vo_id on all contact records
			for(ResourceContactRecord vcrec : contacts) {
				vcrec.resource_id = rec.id;
			}
			cmodel.insert(contacts);
		
			//process resource alias
			ResourceAliasModel ramodel = new ResourceAliasModel(context);
			ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
			for(String alias : resource_aliases) {
				ResourceAliasRecord rarec = new ResourceAliasRecord();
				rarec.resource_id = rec.id;
				rarec.resource_alias = alias;
				list.add(rarec);
			}
			ramodel.insert(list);		
			
			//process resource services
			for(ResourceServiceRecord rsrec : resource_services) {
				rsrec.resource_id = rec.id;
			}
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			rsmodel.insert(resource_services);
			
			//process WLCG Resource record
			if(wrec != null) {
				wrec.resource_id = rec.id;
				ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
				wmodel.insert(wrec);
			}
			/*
			//process downtime schedule
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(auth);
			for(ResourceDowntimeEditorDE.DowntimeEditor downtime_editor : downtimes) {
				ResourceDowntimeRecord downtime = downtime_editor.getDowntimeRecord();
				
				downtime.resource_id = rec.id;
				
				dmodel.insert(downtime); //key is now set
			
				//process downtime service
				ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(auth);
				ArrayList<ResourceDowntimeServiceRecord> services = downtime_services.get(downtime_editor);
				for(ResourceDowntimeServiceRecord service : services) {
					service.resource_downtime_id = downtime.id;
					rdsmodel.insert(service);
				}
			}	
			*/
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(ResourceRecord rec,
			ArrayList<String> resource_aliases,
			ArrayList<ResourceContactRecord> contacts,
			ResourceWLCGRecord wrec,
			ArrayList<ResourceServiceRecord> resource_services/*,
			ArrayList<ResourceDowntimeEditorDE.DowntimeEditor> downtimes,
			HashMap<DowntimeEditor, ArrayList<ResourceDowntimeServiceRecord>> downtime_services*/) throws Exception
	{
		//Do insert / update to our DB
		try {		
			//process detail information
			getConnection().setAutoCommit(false);
			
			update(get(rec), rec);
			
			//process contact information
			ResourceContactModel cmodel = new ResourceContactModel(context);
			//reset vo_id on all contact records
			for(ResourceContactRecord vcrec : contacts) {
				vcrec.resource_id = rec.id;
			}
			cmodel.update(cmodel.getByResourceID(rec.id), contacts);
			
			//process resource alias
			ResourceAliasModel ramodel = new ResourceAliasModel(context);
			ArrayList<ResourceAliasRecord> list = new ArrayList<ResourceAliasRecord>();
			for(String alias : resource_aliases) {
				ResourceAliasRecord rarec = new ResourceAliasRecord();
				rarec.resource_id = rec.id;
				rarec.resource_alias = alias;
				list.add(rarec);
			}
			ramodel.update(ramodel.getAllByResourceID(rec.id), list);	
		
			//process resource services
			for(ResourceServiceRecord rsrec : resource_services) {
				rsrec.resource_id = rec.id;
			}
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			rsmodel.update(rsmodel.getAllByResourceID(rec.id), resource_services);
			
			//process WLCG Record
			ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
			ResourceWLCGRecord oldrec = wmodel.get(rec.id);
			if(oldrec == null) {
				//we don't have the record yet.. just do insert
				if(wrec != null) {
					wrec.resource_id = rec.id;
					wmodel.insert(wrec);
				}
			} else {
				//we have old record
				if(wrec != null) {
					//update the record
					wrec.resource_id = rec.id;
					wmodel.update(oldrec, wrec);
				} else {
					//new one is null, so let's remove it
					wmodel.remove(oldrec);
				}
			}
			
			/*
			//process downtime schedule
			ResourceDowntimeModel dmodel = new ResourceDowntimeModel(auth);
			for(ResourceDowntimeEditorDE.DowntimeEditor downtime_editor : downtimes) {
				ResourceDowntimeRecord downtime = downtime_editor.getDowntimeRecord();
				ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(auth);
				ArrayList<ResourceDowntimeServiceRecord> services = downtime_services.get(downtime_editor);
				
				downtime.resource_id = rec.id;
				
				//process the downtime record itself
				if(downtime.id != null) {
					dmodel.update(dmodel.get(downtime.id), downtime);
				} else {
					dmodel.insert(downtime);
					//update the id
					for(ResourceDowntimeServiceRecord service : services) {
						service.resource_downtime_id = downtime.id;

					}
				}
				rdsmodel.update(rdsmodel.getByDowntimeID(downtime.id), services);			
			
			}
			*/
			
			getConnection().commit();
			getConnection().setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back VO insert transaction.");
			getConnection().rollback();
			getConnection().setAutoCommit(true);
			
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}

