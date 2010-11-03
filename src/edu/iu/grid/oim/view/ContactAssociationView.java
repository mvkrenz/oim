package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRankRecord;
import edu.iu.grid.oim.model.db.record.ContactTypeRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.servlet.HomeServlet;

public class ContactAssociationView extends GenericView {
    static Logger log = Logger.getLogger(ContactAssociationView.class);  
    
	private GenericView view;
	private Context context;
	private boolean show_new_buttons;
	private int contactid;
    
	public ContactAssociationView(Context context, int contactid) throws SQLException
	{
		this.context = context;
		this.contactid = contactid;
		show_new_buttons = false;
	}
	
	public void showNewButtons(boolean b) {
		show_new_buttons = b;
	}
	
	private GenericView createView(int contactid) 
	{
		GenericView view = new GenericView();
		
		try {
			ResourceModel rmodel = new ResourceModel(context);
			ResourceContactModel rcontactmodel = new ResourceContactModel(context);
			
			VOModel vomodel = new VOModel(context);
			VOContactModel vocontactmodel = new VOContactModel(context);
			
			SCModel scmodel = new SCModel(context);
			SCContactModel sccontactmodel = new SCContactModel(context);
			
			//aux..
			ContactTypeModel ctmodel = new ContactTypeModel(context);
			HashMap<Integer, ContactTypeRecord> ctlist = ctmodel.getAll();
			ContactRankModel crmodel = new ContactRankModel(context);
			HashMap<Integer, ContactRankRecord> crlist = crmodel.getAll();
						
			view.add(new HtmlView("<table width=\"100%\"><tr><td width=\"33%\">"));
			
			view.add(new HtmlView("<h3>Resource</h3>"));
			ArrayList<ResourceContactRecord> rcrecs = rcontactmodel.getByContactID(contactid);
			HashMap<Integer, String> resourceassoc = new HashMap<Integer, String>();
			for(ResourceContactRecord rcrec : rcrecs) {
				ResourceRecord rrec = rmodel.get(rcrec.resource_id);
				//if(rrec.active && !rrec.disable) {
				if(rmodel.canEdit(rrec.id)) {
					resourceassoc.put(rrec.id, rrec.name);
				}
			}
			if(show_new_buttons) {
				class NewResourceButtonDE extends DivRepButton
				{
					String url;
					public NewResourceButtonDE(DivRep parent, String _url)
					{
						super(parent, "Add New Resource");
						url = _url;
					}
					protected void onEvent(DivRepEvent e) {
						redirect(url);
					}
				};
				view.add(new NewResourceButtonDE(context.getPageRoot(), "resourceedit"));
			} else {
				if(resourceassoc.size() == 0) {
					view.add(new HtmlView("<p>None</p>"));
				}
			}
			for(Integer rid : resourceassoc.keySet()) {
				String name = resourceassoc.get(rid);
				view.add(new HtmlView("<div><a href=\""+StaticConfig.getApplicationBase()+"/resourceedit?id="+rid+"\">"+name+"</a></div>"));
				
				//show which contact types the user is associated with
				ArrayList<ResourceContactRecord> recs = rcontactmodel.getByResourceID(rid);
				for(ResourceContactRecord rec : recs) {
					if(rec.contact_id.equals(contactid)) {
						ContactTypeRecord ctrec = ctlist.get(rec.contact_type_id);
						String rank = crlist.get(rec.contact_rank_id).name;
						view.add(new HtmlView("<div class=\"small_indent contact_rank contact_"+rank+"\">"+ctrec.name+"</div>"));
					}
				}
				
			}
			
			view.add(new HtmlView("</td><td width=\"33%\">"));
			
			view.add(new HtmlView("<h3>Virtual Organization</h3>"));
			ArrayList<VOContactRecord> vocrecs = vocontactmodel.getByContactID(contactid);
			HashMap<Integer, String> voassoc = new HashMap<Integer, String>();
			for(VOContactRecord vocrec : vocrecs) {
				VORecord vorec = vomodel.get(vocrec.vo_id);
				//if(vorec.active && !vorec.disable) {
				if(vomodel.canEdit(vorec.id)) {
					voassoc.put(vorec.id, vorec.name);
				}	
			}
			if(show_new_buttons) {
				class NewVOButtonDE extends DivRepButton
				{
					String url;
					public NewVOButtonDE(DivRep parent, String _url)
					{
						super(parent, "Add New Virtual Organization");
						url = _url;
					}
					protected void onEvent(DivRepEvent e) {
						redirect(url);
					}
				};
				view.add(new NewVOButtonDE(context.getPageRoot(), "voedit"));
			} else {
				if(voassoc.size() == 0) {
					view.add(new HtmlView("<p>None</p>"));
				}
			}	
			for(Integer vo_id : voassoc.keySet()) {
				String name = voassoc.get(vo_id);
				view.add(new HtmlView("<div><a href=\""+StaticConfig.getApplicationBase()+"/voedit?id="+vo_id+"\">"+name+"</a></div>"));
				
				//show which contact types the user is associated with
				ArrayList<VOContactRecord> recs = vocontactmodel.getByVOID(vo_id);
				for(VOContactRecord rec : recs) {
					if(rec.contact_id.equals(contactid)) {
						ContactTypeRecord ctrec = ctlist.get(rec.contact_type_id);
						String rank = crlist.get(rec.contact_rank_id).name;
						view.add(new HtmlView("<div class=\"small_indent contact_rank contact_"+rank+"\">"+ctrec.name+"</div>"));
					}
				}
			}
			
			view.add(new HtmlView("</td><td width=\"33%\">"));
			
			view.add(new HtmlView("<h3>Support Center</h3>"));
			ArrayList<SCContactRecord> sccrecs = sccontactmodel.getByContactID(contactid);
			HashMap<Integer, String> scassoc = new HashMap<Integer, String>();
			for(SCContactRecord sccrec : sccrecs) {
				SCRecord screc = scmodel.get(sccrec.sc_id);
				//if(screc.active && !screc.disable) {
				if(scmodel.canEdit(screc.id)) {
					scassoc.put(screc.id, screc.name);
				}
			}
			if(show_new_buttons) {
				class NewSCButtonDE extends DivRepButton
				{
					String url;
					public NewSCButtonDE(DivRep parent, String _url)
					{
						super(parent, "Add New Support Center");
						url = _url;
					}
					protected void onEvent(DivRepEvent e) {
						redirect(url);
					}
				};
				view.add(new NewSCButtonDE(context.getPageRoot(), "scedit"));
			} else {
				if(scassoc.size() == 0) {
					view.add(new HtmlView("<p>None</p>"));
				}
			}
			for(Integer scid : scassoc.keySet()) {
				String name = scassoc.get(scid);
				view.add(new HtmlView("<div><a href=\""+StaticConfig.getApplicationBase()+"/scedit?id="+scid+"\">"+name+"</a></div>"));
				
				//show which contact types the user is associated with
				ArrayList<SCContactRecord> recs = sccontactmodel.getBySCID(scid);
				for(SCContactRecord rec : recs) {
					if(rec.contact_id.equals(contactid)) {
						ContactTypeRecord ctrec = ctlist.get(rec.contact_type_id);
						String rank = crlist.get(rec.contact_rank_id).name;
						view.add(new HtmlView("<div class=\"small_indent contact_rank contact_"+rank+"\">"+ctrec.name+"</div>"));
					}
				}
			}

			
			view.add(new HtmlView("</td></tr></table>"));
		
		} catch (SQLException e) {
			view.add(new HtmlView("<p>Error while constructing conact association view</p>"));
			log.error("Failed to construct contact association view", e);
		}
		
		return view;
	}
	
	public void render(PrintWriter out)
	{
		view = createView(contactid);
		view.render(out);
	}
}
