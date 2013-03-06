package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.i18n.Labels;

import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divrep.form.GridAdminFormDE;

public class GridAdmin extends DivRepFormElement {
    static Logger log = Logger.getLogger(GridAdmin.class); 
	Labels lab = Labels.getInstance();
	
	ArrayList<GridAdminEditor> gridadmins = new ArrayList<GridAdminEditor>();
	private DivRepButton add_button;
	private UserContext context;
	//private ArrayList<GridAdminRecord> gridadmin_recs;
	
	LinkedHashMap<Integer, String> vo_keyvalues = new LinkedHashMap();
	
	class GridAdminEditor extends DivRepFormElement
	{
		//service details
    	private DivRepSelectBox vo;
    	private ContactEditor contact;
		
		private DivRepButton remove_button;
		private GridAdmin parent;
		
		protected GridAdminEditor(GridAdmin parent, ArrayList<GridAdminRecord> recs) {
			super(parent);
			
			vo = new DivRepSelectBox(this, vo_keyvalues);
			//vo.setNullLabel("(No VO association)");
			vo.setRequired(true);
			vo.setValue(30);//select OSG by default
			//vo.setLabel("VO");
			
			contact = new ContactEditor(this, new ContactModel(context), false, false);
			//contact.setLabel("GridAdmin Contacts");		
			contact.setMinContacts(ContactRank.Primary, 1);
			contact.setMaxContacts(ContactRank.Primary, 30); //just a random limit - updated per Alain's request
			contact.setShowRank(false);
		
			if(recs != null && recs.size() > 0) {
				//use vo specified in the first item - should be all the same
				GridAdminRecord first = recs.get(0);
				vo.setValue(first.vo_id);
				
				//populate contacts
				for(GridAdminRecord rec : recs) {
					try {
						ContactModel cmodel = new ContactModel(context);
						ContactRecord crec = cmodel.get(rec.contact_id);
						contact.addSelected(crec, 1);
					} catch (SQLException e) {
						log.error("Failed to lookup contact while populating ga", e);
					}
				}
			}
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addClass("pull-right");
			//remove_button.setConfirm(true, "Do you really want to remove this service?");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeGridAdmin(GridAdminEditor.this);	
					modified(true);
				}
			});
		}
		
		public void addGridAdminEventListener(DivRepEventListener listener) {
			GridAdmin.this.addEventListener(listener);
		}
		
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"well well-small\">");
			
			out.write("<div class=\"row-fluid\">");
			out.write("<div class=\"span2\">");
			out.write("VO");
			out.write("</div>");
			out.write("<div class=\"span9\">");
			vo.render(out);
			out.write("</div>");
			out.write("<div class=\"span1\">");
			remove_button.render(out);
			out.write("</div>");
			out.write("</div>"); //row
			
			out.write("<div class=\"row-fluid\">");
			
			out.write("<div class=\"span2\">");
			out.write("GridAdmins");
			out.write("</div>");
			
			out.write("<div class=\"span10\">");
			contact.render(out);
			out.write("</div>");
			
			out.write("</div>"); //row
			
			out.write("</div>"); 
		}

		public ArrayList<GridAdminRecord> getGridAdminRecords(String domain) {
			ArrayList<GridAdminRecord> garecs = new ArrayList<GridAdminRecord>();
			for(ContactRecord rec : contact.getContactRecordsByRank(1)) {
				GridAdminRecord garec = new GridAdminRecord();
				garec.contact_id = rec.id;
				garec.vo_id = vo.getValue();	
				garec.domain = domain;
				garecs.add(garec);
			}
			return garecs;
		}
	}
	
	public void removeGridAdmin(GridAdminEditor ga)
	{
		gridadmins.remove(ga);
		redraw();
	}
	
	public void addGridAdmin(ArrayList<GridAdminRecord> recs) { 
		GridAdminEditor gaeditor = new GridAdminEditor(this, recs);
		gridadmins.add(gaeditor);
		redraw();
	}
	
	public GridAdmin(GridAdminFormDE _parent, UserContext _context) {
		super(_parent);
		context = _context;

		//load vo names
		VOModel vo_model = new VOModel(context);
		try {
			ArrayList<VORecord> recs = vo_model.getAll();
			Collections.sort(recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(VORecord vo_rec : recs) {
				/*
				//check if the VO has at least 1 ra(primary or secondary) specified
				ArrayList<VOContactRecord> crecs = model.getByVOID(vo_rec.id);
				boolean hasra = false;
				for(VOContactRecord crec : crecs) {
					if(crec.contact_type_id.equals(11) && //RA
						(crec.contact_rank_id.equals(1) || crec.contact_rank_id.equals(2))) { //primary or secondary
						//ContactRecord contactrec = cmodel.get(crec.contact_id);
						hasra = true;
						break;
					}
				}
				if(hasra) {
					keyvalues.put(vo_rec.id, vo_rec.name);
				}
				*/
				vo_keyvalues.put(vo_rec.id, vo_rec.name);
			}
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}
		
		add_button = new DivRepButton(this, "Add New VO Group");
		add_button.addClass("btn");
		//add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addGridAdmin(null);
				modified(true);
			}
		});
	}
	
	public ArrayList<GridAdminRecord> getGridAdminRecords(String domain)
	{
		ArrayList<GridAdminRecord> gridadmin_recs  = new ArrayList<GridAdminRecord>();
		for(GridAdminEditor gaeditor : gridadmins) {
			ArrayList<GridAdminRecord> recs = gaeditor.getGridAdminRecords(domain);
			gridadmin_recs.addAll(recs);
		}
		return gridadmin_recs;
	}


	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub

	}
	
	public boolean validate()
	{
		error.redraw();
		boolean valid = true;
		
		//validate each services
		for(GridAdminEditor gaeditor : gridadmins) {
			if(!gaeditor.validate()) {
				error.set(null);//child element should show error message
				valid = false;
			}
		}
		
		/* -- no gridadmin is ok
		if(valid) {
			if(isRequired() && gridadmins.size() == 0) {
				error.set("Please specify at least one gridadmin.");
				valid = false;
			}		
		}
		*/
		
		setValid(valid);
		return valid;
	}

	@Override
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		
		for(GridAdminEditor gaeditor : gridadmins) {
			gaeditor.render(out);
		}
		add_button.render(out);
		error.render(out);
		
		out.print("</div>");
	}

}
