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
	private ArrayList<ContactDE> selected = new ArrayList();
	private NewContactDE newcontact;
	
	class NewContactDE extends DivEx
	{
		private ContactModel pmodel;
		
		public NewContactDE(DivEx parent, ContactModel _pmodel) {
			super(parent);
			pmodel = _pmodel;
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
				addSelected(person);
			} catch (SQLException e1) {
				alert("Unknown contact_id");
			}
		}
		
		protected void onRequest(HttpServletRequest request, HttpServletResponse response)
		{
			try {
				String query = request.getParameter("q").toLowerCase();
				int limit = Integer.parseInt(request.getParameter("limit"));
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
				for(ContactDE rec : selected) {
					persons.remove(rec.person.id);
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
		ContactDE(DivEx parent, ContactRecord _person) {
			super(parent);
			person = _person;
			myself = this;
			removebutton = new ButtonDE(this, "Remove");
			removebutton.setStyle(ButtonDE.Style.IMAGE);
			removebutton.setImageUrl(ServletBase.BaseURL() + "/images/delete.png");
			removebutton.addEventListener(new EventListener() {
				public void handleEvent(Event e) { removeContact(myself); }
			});
			
			setAttr("class", "inline");
		}
		public String renderInside()
		{
			String out = "<span class=\"contact round\">"; 
			out += person.name;
			out += removebutton.render();
			out += "</span>&nbsp;";
			return out;
		}
	}
	
	public void removeContact(ContactDE contact)
	{
		selected.remove(contact);
		redraw();
	}
	
	public ContactEditorDE(DivEx parent, ContactModel pmodel) {
		super(parent);
		newcontact = new NewContactDE(this, pmodel);
	}
	
	public void addSelected(ContactRecord rec)
	{
		selected.add(new ContactDE(this, rec));
		redraw();
	}
	
	public String renderInside() 
	{
		String out = "<div class=\"contact_editor\" onclick=\"$(this).find('.ac_input').focus(); return false;\">";
		
		for(ContactDE contact : selected) {
			out += contact.render();
		}
		out += newcontact.render();
		out += "</div>";
	
		return out;
	}

}
