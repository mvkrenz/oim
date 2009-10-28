package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ContactAssociationView extends GenericView {
	
	private GenericView view;
	private Context context;
    
	public ContactAssociationView(Context _context, int contactid) throws SQLException
	{
		context = _context;
		view = createView(contactid);
	}
	
	private GenericView createView(int contactid) throws SQLException
	{
		GenericView view = new GenericView();
		
		ResourceModel rmodel = new ResourceModel(context);
		ResourceContactModel rcontactmodel = new ResourceContactModel(context);
		
		VOModel vomodel = new VOModel(context);
		VOContactModel vocontactmodel = new VOContactModel(context);
		
		SCModel scmodel = new SCModel(context);
		SCContactModel sccontactmodel = new SCContactModel(context);
		
		ArrayList<ResourceContactRecord> rcrecs = rcontactmodel.getByContactID(contactid);
		HashMap<Integer, String> resourceassoc = new HashMap<Integer, String>();
		for(ResourceContactRecord rcrec : rcrecs) {
			ResourceRecord rrec = rmodel.get(rcrec.resource_id);
			if(rrec.active && !rrec.disable) {
				resourceassoc.put(rrec.id, rrec.name);
			}
		}
		if(resourceassoc.size() > 0) {
			view.add(new HtmlView("<h3>Resource</h3>"));
			for(Integer rid : resourceassoc.keySet()) {
				String name = resourceassoc.get(rid);
				view.add(new HtmlView("<p><a href=\""+StaticConfig.getApplicationBase()+"/resourceedit?id="+rid+"\">"+name+"</a></p>"));
			}
		}
		
		ArrayList<VOContactRecord> vocrecs = vocontactmodel.getByContactID(contactid);
		HashMap<Integer, String> voassoc = new HashMap<Integer, String>();
		for(VOContactRecord vocrec : vocrecs) {
			VORecord vorec = vomodel.get(vocrec.vo_id);
			if(vorec.active && !vorec.disable) {
				voassoc.put(vorec.id, vorec.name);
			}	
		}	
		if(voassoc.size() > 0) {
			view.add(new HtmlView("<h3>Virtual Organization</h3>"));
			for(Integer vo_id : voassoc.keySet()) {
				String name = voassoc.get(vo_id);
				view.add(new HtmlView("<p><a href=\""+StaticConfig.getApplicationBase()+"/voedit?id="+vo_id+"\">"+name+"</a></p>"));
			}
		}
		
		ArrayList<SCContactRecord> sccrecs = sccontactmodel.getByContactID(contactid);
		HashMap<Integer, String> scassoc = new HashMap<Integer, String>();
		for(SCContactRecord sccrec : sccrecs) {
			SCRecord screc = scmodel.get(sccrec.sc_id);
			if(screc.active && !screc.disable) {
				scassoc.put(screc.id, screc.name);
			}
		}	
		if(scassoc.size() > 0) {
			view.add(new HtmlView("<h3>Support Center</h3>"));
			for(Integer scid : scassoc.keySet()) {
				String name = scassoc.get(scid);
				view.add(new HtmlView("<p><a href=\""+StaticConfig.getApplicationBase()+"/scedit?id="+scid+"\">"+name+"</a></p>"));
			}
		}
		
		return view;
	}
	
	public void render(PrintWriter out)
	{
		view.render(out);
	}
}
