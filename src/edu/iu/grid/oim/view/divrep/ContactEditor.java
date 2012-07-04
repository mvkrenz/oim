package edu.iu.grid.oim.view.divrep;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;

//this requires modified version of jquery autocomplete plugin, and client side code to make the input area to be autocomplete
public class ContactEditor extends DivRepFormElement<HashMap<ContactEditor.Rank, ArrayList<ContactEditor.ContactDE>>> {
	static Logger log = Logger.getLogger(ContactEditor.class);
	
	public enum Rank {Primary, Secondary, Tertiary };
	private HashMap<Rank/*rank_id*/, ArrayList<ContactDE>> selected;
	
	private String primary_label = Rank.Primary.toString();
	private String secondary_label = Rank.Secondary.toString();
	private String tertiary_label = Rank.Tertiary.toString();
	public void setLabel(Rank rank, String label) {
		switch(rank) {
		case Primary: primary_label = label; break;
		case Secondary: secondary_label = label; break;
		case Tertiary: tertiary_label = label; break;
		}
	}
	
	// Default max contact limits - can be overridden 
	private int max_primary = 1;
	private int max_secondary = 1;
	private int max_tertiary = 16;

	private NewContactDE primary_newcontact;
	private NewContactDE secondary_newcontact;
	private NewContactDE tertiary_newcontact;
	
	private Boolean has_secondary = false;
	private Boolean has_tertiary = false;
	private Boolean show_rank = true;
	public void setShowRank(Boolean b) { show_rank = b; }
	
	public void setMinContacts(Rank rank, int min) {
		addValidator(new MinValidator(rank, min));
	}
	public void setMaxContacts(Rank rank, int max) {
		if (rank == Rank.Primary) { 
			max_primary = max;
		}
		else if (rank == Rank.Secondary) { 
			max_secondary= max;
		}
		else { 
			max_tertiary= max;
		}
	}	
	public void setDisabled(Boolean b) { 
		super.setDisabled(b);
		primary_newcontact.setDisabled(b);
		if(secondary_newcontact != null) {
			secondary_newcontact.setDisabled(b);
		}
		if(tertiary_newcontact != null) {
			tertiary_newcontact.setDisabled(b);
		}
	}
	
	public ContactEditor(DivRep parent, ContactModel pmodel, Boolean _has_secondary, Boolean _has_tertiary) {
		super(parent);
		
		selected = new HashMap<Rank/*rank_id*/, ArrayList<ContactDE>>();
		super.setValue(selected);//I need to do this so that DivRepFormElement correctly fire MinValidator
		
		has_secondary = _has_secondary;
		has_tertiary = _has_tertiary;
		
		primary_newcontact = new NewContactDE(this, pmodel, Rank.Primary);
		selected.put(Rank.Primary, new ArrayList());
		
		if(has_secondary) {
			secondary_newcontact = new NewContactDE(this, pmodel, Rank.Secondary);
			selected.put(Rank.Secondary, new ArrayList());
		}
		if(has_tertiary) {
			tertiary_newcontact = new NewContactDE(this, pmodel, Rank.Tertiary);
			selected.put(Rank.Tertiary, new ArrayList());
		}
	}
	
	
	@Deprecated
	//use addSelected() instead
	public void setValue(HashMap<ContactEditor.Rank, ArrayList<ContactEditor.ContactDE>> value)
	{
		//depricated
	}
	
	@Deprecated
	public HashMap<ContactEditor.Rank, ArrayList<ContactEditor.ContactDE>> getValue()
	{
		//depricated
		return null;
	}
	
	class MinValidator implements DivRepIValidator<HashMap<ContactEditor.Rank, ArrayList<ContactDE>>>
	{
		private int min;
		private Rank rank;
		
		public MinValidator(Rank _rank, int _min) {
			min = _min;
			rank = _rank;
		}
		
		public String getErrorMessage() {
			return "Please specify at least " + min + " contact(s) for " + rank.toString();
		}

		public Boolean isValid(HashMap<ContactEditor.Rank, ArrayList<ContactDE>> recs_hash) {
			ArrayList<ContactDE> recs = recs_hash.get(rank);
			return (recs.size() >= min);
		}
	}
	
	//autocomplete area to add new contact
	class NewContactDE extends DivRepFormElement
	{
		private ContactModel pmodel;
		private Rank rank;
		
		public NewContactDE(DivRep parent, ContactModel _pmodel, Rank _rank) {
			super(parent);
			pmodel = _pmodel;
			rank = _rank;
		}
		
		public void render(PrintWriter out) {
			out.print("<div class=\"divrep_inline\" id=\""+getNodeID()+"\">");
			out.print("<input type='text' class='autocomplete'/>");
			out.print("<script type='text/javascript'>$(document).ready(function() {setAutocomplete($('#"+getNodeID()+" input.autocomplete'));});</script>");
			out.print("</div>");
		}
		
		protected void onEvent(DivRepEvent e) {

			int contact_id = Integer.parseInt((String)e.value);
			try {
				ContactRecord person = pmodel.get(contact_id);
				addSelected(person, Enum2DBRank(rank));
				setFormModified();
			} catch (SQLException e1) {
				alert("Unknown contact_id");
			}
		}
		
	    /* replace multiple whitespaces between words with single blank */
	    private String itrim(String source) {
	    	if(source == null) return null;
	        return source.replaceAll("\\b\\s{2,}\\b", " ");
	    }

		
		//this handles the list request from the autocomplete box.
		protected void onRequest(HttpServletRequest request, HttpServletResponse response)
		{
			try {
				
				//support both new & old version of autocomplete
				String query = itrim(request.getParameter("q"));				
				int limit = Integer.parseInt(request.getParameter("limit")); //only returns records upto requested limit
				Collection<ContactRecord> all = pmodel.getAllNonDisabled();
				HashMap<Integer, ContactRecord> persons = new HashMap();
				ContactRecord best_guess = null;
				int best_guess_distance = 10000;
				//filter records that matches the query upto limit
				for(ContactRecord rec : all) {
					if(persons.size() > limit) break;
					
					if(rec.name != null) {
						String name = itrim(rec.name.toLowerCase());
						if(name.contains(query)) {
							persons.put(rec.id, rec);
							continue;
						}
						
						//calculate levenshtein distance per token
						for(String token : rec.name.split(" ")) {
							int distance = StringUtils.getLevenshteinDistance(token, query);
							if(best_guess_distance > distance) {
								best_guess = rec;
								best_guess_distance = distance;
							}
						}
					}
					if(rec.primary_email != null) {
						String name = rec.primary_email.toLowerCase();
						if(name.contains(query)) {
							persons.put(rec.id, rec);
							continue;
						}
					}
				}
				
				//if no match was found, pick the closest match
				if(persons.size() == 0) {
					persons.put(best_guess.id, best_guess);	
				}
		
				//remove people that are already selected 
				for(ContactDE rec : selected.get(Rank.Primary)) {
					persons.remove(rec.person.id);
				}
				if(has_secondary) {
					for(ContactDE rec : selected.get(Rank.Secondary)) {
						persons.remove(rec.person.id);
					}					
				}
				if(has_tertiary) {
					for(ContactDE rec : selected.get(Rank.Tertiary)) {
						persons.remove(rec.person.id);
					}					
				}
	
				String out = "[";
				boolean first = true;
				for(ContactRecord rec : persons.values()) {
					if(first) {
						first = false;
					} else {
						out += ",";
					}
					out += "{\"id\":"+rec.id+", \"name\":\""+itrim(rec.name)+"\", \"email\":\""+rec.primary_email+"\"}\n";
				}
				out += "]";
				response.setContentType("text/javascript");
				response.getOutputStream().print(out);
			
			} catch (SQLException e) {
				log.error(e);
			} catch (IOException e) {
				log.error(e);
			}		
		}
	}
	
	class ContactDE extends DivRepFormElement
	{
		public ContactRecord person;
		private DivRepButton removebutton;
		private ContactDE myself;
		private Rank rank;
		
		ContactDE(DivRep parent, ContactRecord _person, Rank _rank) {
			super(parent);
			person = _person;
			rank = _rank;
			myself = this;
			removebutton = new DivRepButton(this, "images/delete.png");
			removebutton.setStyle(DivRepButton.Style.IMAGE);
			removebutton.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) { 
					removeContact(myself, rank);
					setFormModified();
				}
			});
		}
		public void render(PrintWriter out)
		{
			out.print("<div class=\"divrep_inline contact divrep_round\" id=\""+getNodeID()+"\">");
			if(person.name == null) {
				out.print("(No Name)");
			} else {
				out.print(StringEscapeUtils.escapeHtml(person.name.trim()));
				if(person.primary_email != null) {
					out.print(" <code>"+StringEscapeUtils.escapeHtml("<"+person.primary_email+">")+"</code>");
				} else {
					out.print(" <code>(no email address specified)</code>");
				}
			}
			if(!isDisabled()) {
				out.write("&nbsp;");
				removebutton.render(out);
			}
			out.print("</div>");
		}
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public void removeContact(ContactDE contact, Rank rank)
	{
		ArrayList<ContactDE> list = selected.get(rank);		
		list.remove(contact);
		validate();
		redraw();
	}
	
	private Rank DBRank2Enum(int contact_rank_id)
	{
		switch(contact_rank_id) {
		case 1:
			return ContactEditor.Rank.Primary;
		case 2:
			return ContactEditor.Rank.Secondary;
		case 3:
			return ContactEditor.Rank.Tertiary;
		}	
		throw new IllegalArgumentException("Uknown contact_rank_id: " + contact_rank_id);
	}
	
	private int Enum2DBRank(Rank rank)
	{
		switch(rank) {
		case Primary:
			return 1;
		case Secondary:
			return 2;
		case Tertiary:
			return 3;
		}
		throw new IllegalArgumentException("Uknown rank: " + rank);
	}
	
	public void addSelected(ContactRecord rec, int contact_rank_id)
	{
		Rank rank = DBRank2Enum(contact_rank_id);
		
		ArrayList<ContactDE> list = selected.get(rank);
		if(list != null) {
			list.add(new ContactDE(this, rec, rank));
			validate();
			redraw();
		}
	}
	
	public HashMap<ContactRecord, Integer/*rank*/> getContactRecords()
	{
		HashMap<ContactRecord, Integer> records = new HashMap();
		for(Rank rank : selected.keySet()) {
			ArrayList<ContactDE> cons = selected.get(rank);
			for(ContactDE con : cons) {
				records.put(con.person, Enum2DBRank(rank));
			}
		}
		return records;
	}

	public ArrayList<ContactRecord> getContactRecordsByRank(Integer _rank_id)
	{
		Rank rank = DBRank2Enum(_rank_id);

		ArrayList<ContactRecord> records = new ArrayList<ContactRecord>();
		ArrayList<ContactDE> contact_divs = new ArrayList<ContactDE>();
		contact_divs = selected.get(rank);
		for(ContactDE contact_div : contact_divs) {
			records.add(contact_div.person);
		}
		return records;
	}
	
	
	public void render(PrintWriter out) 
	{
		out.print("<div id=\""+getNodeID()+"\">");
		if(getLabel() != null) {
			out.print("<label>"+StringEscapeUtils.escapeHtml(getLabel())+"</label><br/>");
		}
		if(isDisabled()) {
			out.print("<table class='contact_table gray'>");		
		} else {
			out.print("<table class='contact_table'>");
		}
		renderContactList(out, primary_newcontact, selected.get(Rank.Primary), Rank.Primary, primary_label, max_primary);
		if(has_secondary) {
			renderContactList(out, secondary_newcontact, selected.get(Rank.Secondary), Rank.Secondary, secondary_label, max_secondary);
		}
		if(has_tertiary) {
			renderContactList(out, tertiary_newcontact, selected.get(Rank.Tertiary), Rank.Tertiary, tertiary_label, max_tertiary);
		}
		out.print("</table>");
		error.render(out);
		out.print("</div>");
	}
	
	public void renderContactList(PrintWriter out, NewContactDE newcontact, ArrayList<ContactDE> selected, Rank rank, String label, int max)
	{
		out.print("<tr>");
		if(show_rank) {
			out.print("<th><div class='contact_rank contact_"+rank+"'>"+label+"</div></th>");
		}
		if(selected.size() == max || isDisabled()) {
			//list is full or disabled
			out.print("<td><div class=\"contact_editor\">");
			for(ContactDE contact : selected) {
				contact.setDisabled(isDisabled());
				contact.render(out);
			}
			out.print("</div></td>");
		} else {
			//user can add more contact
			out.print("<td style=\"border: 1px solid #ccc; background-color: white;\"><div class=\"contact_editor\" onclick=\"$(this).find('.autocomplete').focus(); return false;\">");
			for(ContactDE contact : selected) {
				contact.render(out);
			}
			newcontact.render(out);
			out.write("</div>");
			
			out.write("</td>");
		}
		out.print("</tr>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub	
	}
	
	@Deprecated
	public Boolean isRequired() { return false; }

	@Deprecated
	//use setMinContact instead
	public void setRequired(Boolean b) { }
}
