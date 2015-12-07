package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;

public class ResourceModel extends SmallTableModelBase<ResourceRecord> {
    static Logger log = Logger.getLogger(ResourceModel.class);  
    
    public ResourceModel(UserContext context) 
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
	
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("resource_group_id")) {
			ResourceGroupModel model = new ResourceGroupModel(context);
			ResourceGroupRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
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
	public ArrayList<ResourceRecord> getAllEditable() throws SQLException
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

	public ArrayList<ResourceRecord> getAllActiveNotDisabedEditable() throws SQLException
	{	   
		ArrayList<ResourceRecord> list = new ArrayList<ResourceRecord> ();
    	//only select record that is active, not disabled, editable
	    for(ResourceRecord rec : getAllEditable()) {
	    	if ((rec.active)  && (!rec.disable)){
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
		// First, add all resources someone is contact for
		HashSet<Integer> list = new HashSet<Integer>();
		if(auth.getContact() != null) {	
			ResourceContactModel model = new ResourceContactModel(context);
			Collection<ResourceContactRecord> rrecs = model.getByContactID(auth.getContact().id);
			for(ResourceContactRecord rec : rrecs) {
				if(rec.contact_type_id == 1) continue; //submitter contact can't edit.
				list.add(rec.resource_id);
			}
			
			// Second, find VOs that this person is a VO Manager of..
			VOContactModel voc_model = new VOContactModel(context);
			Collection<VOContactRecord> voc_recs = voc_model.getByContactID(auth.getContact().id);
			HashSet<Integer> voids = new HashSet<Integer>();
			for (VOContactRecord voc_rec: voc_recs) {
				//Is contact_type_id 6 (VO Manager)?
				if(voc_rec.contact_type_id == 6) {
					voids.add(voc_rec.vo_id);
				}
			}
			// Then add all resources owned by that VO
			VOResourceOwnershipModel voresowner_model = new VOResourceOwnershipModel (context);  
			for(Integer vo_id : voids) {
				Collection<VOResourceOwnershipRecord> voresowners = voresowner_model.getAllByVOID(vo_id);
				for (VOResourceOwnershipRecord voresowner : voresowners) {
					list.add(voresowner.resource_id);
				}
			}
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
			ArrayList<ResourceServiceRecord> resource_services,
			ArrayList<ResourceServiceDetailRecord> resource_service_details,
			ArrayList<VOResourceOwnershipRecord> owners) throws Exception
	{
		Connection conn = null;
		try {
			conn = connectOIM();		
			conn.setAutoCommit(false);
			
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
			
			//process resource service details
			for(ResourceServiceDetailRecord rsdrec : resource_service_details) {
				rsdrec.resource_id = rec.id;
			}
			ResourceServiceDetailModel rsdmodel = new ResourceServiceDetailModel(context);
			rsdmodel.insert(resource_service_details);		
			
			//process resource services
			for(ResourceServiceRecord rsrec : resource_services) {
				rsrec.resource_id = rec.id;
			}
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			rsmodel.insert(resource_services);
			
			//process resource owners
			for(VOResourceOwnershipRecord owner_rec : owners) {
				owner_rec.resource_id = rec.id;
			}
			VOResourceOwnershipModel voresowner_model = new VOResourceOwnershipModel(context);
			voresowner_model.insert(owners);

			
			//process WLCG Resource record
			if(wrec != null) {
				wrec.resource_id = rec.id;
				ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
				wmodel.insert(wrec);
			}

			conn.commit();
			conn.setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back resource insert transaction.");
			if(conn != null) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			
			//re-throw original exception
			throw new Exception(e);
		}	
	}
	
	public void updateDetail(ResourceRecord rec,
			ArrayList<String> resource_aliases,
			ArrayList<ResourceContactRecord> contacts,
			ResourceWLCGRecord wrec,
			ArrayList<ResourceServiceRecord> resource_services,
			ArrayList<ResourceServiceDetailRecord> resource_service_details,
			ArrayList<VOResourceOwnershipRecord> owners) throws Exception
	{
		Connection conn = null;
		try {		
			conn = connectOIM();
			conn.setAutoCommit(false);
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

			//process resource service details
			for(ResourceServiceDetailRecord rsdrec : resource_service_details) {
				rsdrec.resource_id = rec.id;
			}
			ResourceServiceDetailModel rsdmodel = new ResourceServiceDetailModel(context);
			rsdmodel.update(rsdmodel.getAllByResourceID(rec.id), resource_service_details);
			
			//process resource services
			for(ResourceServiceRecord rsrec : resource_services) {
				rsrec.resource_id = rec.id;
			}
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			rsmodel.update(rsmodel.getByResourceID(rec.id), resource_services);
			
			//process resource owners
			for(VOResourceOwnershipRecord owner_rec : owners) {
				owner_rec.resource_id = rec.id;
			}
			VOResourceOwnershipModel voresowner_model = new VOResourceOwnershipModel(context);
			voresowner_model.update(voresowner_model.getAllByResourceID(rec.id), owners);
			
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
			
			conn.commit();
			conn.setAutoCommit(true);
		} catch (Exception e) {
			log.error(e);
			log.info("Rolling back resource insert transaction.");
			if(conn != null) {
				conn.rollback();
				conn.setAutoCommit(true);
			}
			//re-throw original exception
			throw new Exception(e);
		}			
	}
}

