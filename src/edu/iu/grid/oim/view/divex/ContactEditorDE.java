package edu.iu.grid.oim.view.divex;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;

import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.servlet.ServletBase;

//this requires modified version of jquery autocomplete plugin, and client side code to make the input area to be autocomplete
public class ContactEditorDE extends DivEx {
	static Logger log = Logger.getLogger(ContactEditorDE.class);  

	public enum Rank {PRIMARY, SECONDARY, TERTIARY };
	private HashMap<Rank/*rank_id*/, ArrayList<ContactDE>> selected = new HashMap();
	private int max_primary = 1;
	private int max_secondary = 1;
	private int max_tertiary = 16;

	private NewContactDE primary_newcontact;
	private NewContactDE secondary_newcontact;
	private NewContactDE tertiary_newcontact;
	
	private Boolean has_secondary = false;
	private Boolean has_tertiary = false;
	
	public ContactEditorDE(DivEx parent, ContactModel pmodel, Boolean _has_secondary, Boolean _has_tertiary) {
		super(parent);
		
		has_secondary = _has_secondary;
		has_tertiary = _has_tertiary;
		
		primary_newcontact = new NewContactDE(this, pmodel, Rank.PRIMARY);
		selected.put(Rank.PRIMARY, new ArrayList());
		
		if(has_secondary) {
			secondary_newcontact = new NewContactDE(this, pmodel, Rank.SECONDARY);
			selected.put(Rank.SECONDARY, new ArrayList());
		}
		if(has_tertiary) {
			tertiary_newcontact = new NewContactDE(this, pmodel, Rank.TERTIARY);
			selected.put(Rank.TERTIARY, new ArrayList());
		}
	}
	
	class NewContactDE extends DivEx
	{
		private ContactModel pmodel;
		private Rank rank;
		
		public NewContactDE(DivEx parent, ContactModel _pmodel, Rank _rank) {
			super(parent);
			pmodel = _pmodel;
			rank = _rank;
			setAttr("class", "inline");
		}
		
		public String renderInside() {
			String html = "";
			html += "<input type='text' class='autocomplete'/>";
			html += "<script type='text/javascript'>$(document).ready(function() {setAutocomplete($('#"+getNodeID()+" input.autocomplete'));});</script>";
			return html;
		}
		
		protected void onEvent(Event e) {

			int contact_id = Integer.parseInt(e.getValue());
			try {
				ContactRecord person = pmodel.get(contact_id);
				addSelected(person, rank);
			} catch (SQLException e1) {
				alert("Unknown contact_id");
			}
		}
		
		//this handles the list request from the autocomplete box.
		protected void onRequest(HttpServletRequest request, HttpServletResponse response)
		{
			try {
				String query = request.getParameter("q").toLowerCase();
				int limit = Integer.parseInt(request.getParameter("limit")); //only returns records upto requested limit
				HashMap<Integer, ContactRecord> all = pmodel.getAll();
				HashMap<Integer, ContactRecord> persons = new HashMap();		
				//filter records that matches the query upto limit
				for(ContactRecord rec : all.values()) {
					if(persons.size() > limit) break;
					
					if(rec.name != null) {
						String name = rec.name.toLowerCase();
						if(name.contains(query)) {
							persons.put(rec.id, rec);
							continue;
						}
					}
					if(rec.primary_email != null) {
						String email = rec.primary_email.toLowerCase();
						if(email.contains(query)) {
							persons.put(rec.id, rec);
							continue;
						}
					}
				}
		
				//remove people that are already selected 
				for(ContactDE rec : selected.get(Rank.PRIMARY)) {
					persons.remove(rec.person.id);
				}
				if(has_secondary) {
					for(ContactDE rec : selected.get(Rank.SECONDARY)) {
						persons.remove(rec.person.id);
					}					
				}
				if(has_tertiary) {
					for(ContactDE rec : selected.get(Rank.TERTIARY)) {
						persons.remove(rec.person.id);
					}					
				}
	
				String out = "";
				for(ContactRecord rec : persons.values()) {
					out += rec.id+"|"+rec.name+"|"+rec.primary_email+"\n";
				}
				response.setContentType("text/javascript");
				response.getOutputStream().print(out);
			
			} catch (SQLException e) {
				log.error(e);
			} catch (IOException e) {
				log.error(e);
			}		
		}
	}
	
	class ContactDE extends DivEx
	{
		public ContactRecord person;
		private ButtonDE removebutton;
		private ContactDE myself;
		private Rank rank;
		
		ContactDE(DivEx parent, ContactRecord _person, Rank _rank) {
			super(parent);
			person = _person;
			rank = _rank;
			myself = this;
			removebutton = new ButtonDE(this, "Remove");
			removebutton.setStyle(ButtonDE.Style.IMAGE);
			removebutton.setImageUrl(ServletBase.BaseURL() + "/images/delete.png");
			removebutton.addEventListener(new EventListener() {
				public void handleEvent(Event e) { removeContact(myself, rank); }
			});
			setAttr("class", "inline contact round");
		}
		public String renderInside()
		{
			String out = "";
			//String out = "<span class=\"contact round\">"; 
			if(person.name == null) {
				out += "Null";
			} else {
				out += person.name.replace(" ", "&nbsp;");	
			}
			out += removebutton.render();
			//out += "</span>&nbsp;";
			return out;
		}
	}
	
	public void removeContact(ContactDE contact, Rank rank)
	{
		ArrayList<ContactDE> list = selected.get(rank);		
		list.remove(contact);
		redraw();
	}
	
	public void addSelected(ContactRecord rec, Rank rank)
	{
		ArrayList<ContactDE> list = selected.get(rank);
		list.add(new ContactDE(this, rec, rank));
		redraw();
	}
	
	public String renderInside() 
	{
		String out = "";
		
		out += "<table class='contact_table'>";
		out += renderContactList(primary_newcontact, selected.get(Rank.PRIMARY), "Primary", max_primary);
		if(has_secondary) {
			out += renderContactList(secondary_newcontact, selected.get(Rank.SECONDARY), "Secondary", max_secondary);
		}
		if(has_tertiary) {
			out += renderContactList(tertiary_newcontact, selected.get(Rank.TERTIARY), "Tertiary", max_tertiary);
		}
		out += "</table>";
	
		return out;
	}
	
	public String renderContactList(NewContactDE newcontact, ArrayList<ContactDE> selected, String rank, int max)
	{
		String out = "";
		out += "<tr><th><div style='margin-top: 5px;' class='contact_rank contact_"+rank+"'>"+rank+"</div></th>";
		if(selected.size() == max) {
			out += "<td><div class=\"contact_editor_full\">";
			for(ContactDE contact : selected) {
				out += contact.render();
			}
			out += "</div></td>";
		} else {
			out += "<td><div class=\"contact_editor\" onclick=\"$(this).find('.ac_input').focus(); return false;\">";
			for(ContactDE contact : selected) {
				out += contact.render();
			}
			out += newcontact.render();
			out += "</div></td>";
		}
		out += "</tr>";
		return out;
	}

}
