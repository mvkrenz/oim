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

import edu.iu.grid.oim.model.FOSRank;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;

//this requires modified version of jquery autocomplete plugin, and client side code to make the input area to be autocomplete
public class FOSEditor extends DivRepFormElement<HashMap<FOSRank, ArrayList<FOSEditor.FOSDE>>> {
	static Logger log = Logger.getLogger(FOSEditor.class);
	
	//public enum Rank {Primary, Secondary, Tertiary };
	private HashMap<FOSRank/*rank_id*/, ArrayList<FOSDE>> selected;
	/*
	//allow user to override label
	private String primary_label = FOSRank.Primary.toString();
	private String secondary_label = FOSRank.Secondary.toString();
	//private String tertiary_label = FOSRank.Tertiary.toString();
	public void setLabel(FOSRank rank, String label) {
		switch(rank) {
		case Primary: primary_label = label; break;
		case Secondary: secondary_label = label; break;
		//case Tertiary: tertiary_label = label; break;
		}
	}
	*/
	
	// Default max contact limits - can be overridden 
	private int max_primary = 1;
	private int max_secondary = 32; //just some arbitrary limit for usability
	//private int max_tertiary = 16;

	private NewFOSDE primary_newfos;
	private NewFOSDE secondary_newfos;
	//private NewContactDE tertiary_newcontact;
	
	private Boolean has_secondary = false;
	//private Boolean has_tertiary = false;
	private Boolean show_rank = true;
	public void setShowRank(Boolean b) { show_rank = b; }
	
	public void setMin(FOSRank rank, int min) {
		addValidator(new MinValidator(rank, min));
	}
	public void setMax(FOSRank rank, int max) {
		if (rank == FOSRank.Primary) { 
			max_primary = max;
		}
		else if (rank == FOSRank.Secondary) { 
			max_secondary= max;
		}
		/*
		else { 
			max_tertiary= max;
		}
		*/
	}	
	public void setDisabled(Boolean b) { 
		super.setDisabled(b);
		primary_newfos.setDisabled(b);
		if(secondary_newfos != null) {
			secondary_newfos.setDisabled(b);
		}
	}
	
	public FOSEditor(DivRep parent, FieldOfScienceModel pmodel, Boolean _has_secondary) {
		super(parent);
		
		selected = new HashMap<FOSRank/*rank_id*/, ArrayList<FOSDE>>();
		super.setValue(selected);//I need to do this so that DivRepFormElement correctly fire MinValidator
		
		has_secondary = _has_secondary;
		//has_tertiary = _has_tertiary;
		
		primary_newfos = new NewFOSDE(this, pmodel, FOSRank.Primary);
		selected.put(FOSRank.Primary, new ArrayList());
		
		if(has_secondary) {
			secondary_newfos = new NewFOSDE(this, pmodel, FOSRank.Secondary);
			selected.put(FOSRank.Secondary, new ArrayList());
		}
		/*
		if(has_tertiary) {
			tertiary_newcontact = new NewContactDE(this, pmodel, ContactRank.Tertiary);
			selected.put(ContactRank.Tertiary, new ArrayList());
		}
		*/
	}
	
	
	@Deprecated
	//use addSelected() instead
	public void setValue(HashMap<FOSRank, ArrayList<FOSEditor.FOSDE>> value)
	{
		//depricated
	}
	
	@Deprecated
	public HashMap<FOSRank, ArrayList<FOSEditor.FOSDE>> getValue()
	{
		//depricated
		return null;
	}
	
	class MinValidator implements DivRepIValidator<HashMap<FOSRank, ArrayList<FOSDE>>>
	{
		private int min;
		private FOSRank rank;
		
		public MinValidator(FOSRank _rank, int _min) {
			min = _min;
			rank = _rank;
		}
		
		public String getErrorMessage() {
			return "Please specify at least " + min + " field of science for " + rank.toString();
		}

		public Boolean isValid(HashMap<FOSRank, ArrayList<FOSDE>> recs_hash) {
			ArrayList<FOSDE> recs = recs_hash.get(rank);
			return (recs.size() >= min);
		}
	}
	
	//autocomplete area to add new fos
	class NewFOSDE extends DivRepFormElement
	{
		private FieldOfScienceModel pmodel;
		private FOSRank rank;
		
		public NewFOSDE(DivRep parent, FieldOfScienceModel pmodel, FOSRank rank) {
			super(parent);
			this.pmodel = pmodel;
			this.rank = rank;
		}
		
		public void render(PrintWriter out) {
			out.print("<div class=\"divrep_inline\" id=\""+getNodeID()+"\">");
			out.print("<input type='text' class='autocomplete'/>");
			out.print("<script type='text/javascript'>$(document).ready(function() {setAutocomplete($('#"+getNodeID()+" input.autocomplete'), 0);});</script>");
			out.print("</div>");
		}
		
		protected void onEvent(DivRepEvent e) {

			int fos_id = Integer.parseInt((String)e.value);
			try {
				FieldOfScienceRecord person = pmodel.get(fos_id);
				addSelected(person, rank);
				setFormModified();
				js("$('#"+getNodeID()+" input').focus();");
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
				Collection<FieldOfScienceRecord> all = pmodel.getAll();
				HashMap<Integer, FieldOfScienceRecord> foss = new HashMap();
				FieldOfScienceRecord best_guess = null;
				int best_guess_distance = 10000;
				//filter records that matches the query upto limit
				for(FieldOfScienceRecord rec : all) {
					if(foss.size() > limit) break;
					
					if(rec.name != null) {
						String name = itrim(rec.name.toLowerCase());
						if(name.contains(query.toLowerCase())) {
							foss.put(rec.id, rec);
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
				}
				
				//if no match was found, pick the closest match
				if(foss.size() == 0 && best_guess != null) {
					foss.put(best_guess.id, best_guess);	
				}
		
				//remove people that are already selected 
				for(FOSDE rec : selected.get(FOSRank.Primary)) {
					foss.remove(rec.fos.id);
				}
				if(has_secondary) {
					for(FOSDE rec : selected.get(FOSRank.Secondary)) {
						foss.remove(rec.fos.id);
					}					
				}
				/*
				if(has_tertiary) {
					for(ContactDE rec : selected.get(ContactRank.Tertiary)) {
						persons.remove(rec.person.id);
					}					
				}
				*/
	
				String out = "[";
				boolean first = true;
				for(FieldOfScienceRecord rec : foss.values()) {
					if(first) {
						first = false;
					} else {
						out += ",";
					}
					out += "{\"id\":"+rec.id+", \"name\":\""+itrim(rec.name)+"\"}\n";
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
	
	class FOSDE extends DivRepFormElement
	{
		public FieldOfScienceRecord fos;
		private DivRepButton removebutton;
		private FOSDE myself;
		private FOSRank rank;
		
		FOSDE(DivRep parent, FieldOfScienceRecord _fos, FOSRank _rank) {
			super(parent);
			fos = _fos;
			rank = _rank;
			myself = this;
			removebutton = new DivRepButton(this, "images/delete.png");
			removebutton.setStyle(DivRepButton.Style.IMAGE);
			removebutton.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) { 
					removeFOS(myself, rank);
					setFormModified();
				}
			});
		}
		public void render(PrintWriter out)
		{
			out.print("<div class=\"divrep_inline contact divrep_round\" id=\""+getNodeID()+"\">");
			if(fos.name == null) {
				out.print("(No Name)");
			} else {
				out.print(StringEscapeUtils.escapeHtml(fos.name.trim()));
			}
			if(!isDisabled()) {
				out.write(" ");
				removebutton.render(out);
			}
			out.print("</div>");
		}
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	public void removeFOS(FOSDE fd, FOSRank rank)
	{
		ArrayList<FOSDE> list = selected.get(rank);		
		list.remove(fd);
		validate();
		redraw();
	}
	public void addSelected(FieldOfScienceRecord rec, FOSRank rank)
	{
		ArrayList<FOSDE> list = selected.get(rank);
		if(list != null) {
			list.add(new FOSDE(this, rec, rank));
			validate();
			redraw();
		}
	}
	public void addSelected(FieldOfScienceRecord rec, int fos_rank_id)
	{
		FOSRank rank = FOSRank.get(fos_rank_id);
		addSelected(rec, rank);
	}
	
	public HashMap<FieldOfScienceRecord, FOSRank/*rank*/> getFOSRecords()
	{
		HashMap<FieldOfScienceRecord, FOSRank> records = new HashMap();
		for(FOSRank rank : selected.keySet()) {
			for(FOSDE fd : selected.get(rank)) {
				records.put(fd.fos, rank);
			}
		}
		return records;
	}

	public ArrayList<FieldOfScienceRecord> getFOSRecordsByRank(FOSRank rank)
	{
		return getFOSRecordsByRank(rank.id);
	}
	
	public ArrayList<FieldOfScienceRecord> getFOSRecordsByRank(Integer _rank_id)
	{
		FOSRank rank = FOSRank.get(_rank_id);
		ArrayList<FieldOfScienceRecord> records = new ArrayList<FieldOfScienceRecord>();
		for(FOSDE div : selected.get(rank)) {
			records.add(div.fos);
		}
		return records;
	}
	
	
	public void render(PrintWriter out) 
	{
		out.write("<div ");
		renderClass(out);
		out.write(" id=\""+getNodeID()+"\">");
		if(!isHidden()) {
			if(getLabel() != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(getLabel())+"</label><br/>");
			}
			if(isDisabled()) {
				out.print("<table class='contact_table gray'>");		
			} else {
				out.print("<table class='contact_table'>");
			}
			renderFOSList(out, primary_newfos, selected.get(FOSRank.Primary), FOSRank.Primary, max_primary);
			if(has_secondary) {
				renderFOSList(out, secondary_newfos, selected.get(FOSRank.Secondary), FOSRank.Secondary, max_secondary);
			}
			/*
			if(has_tertiary) {
				renderFOSList(out, tertiary_newcontact, selected.get(ContactRank.Tertiary), ContactRank.Tertiary, max_tertiary);
			}
			*/
			out.print("</table>");
			error.render(out);
		}
		out.print("</div>");
	}
	
	public void renderFOSList(PrintWriter out, NewFOSDE newfos, ArrayList<FOSDE> selected, FOSRank rank, int max)
	{
		out.print("<tr>");
		if(show_rank) {
			out.print("<th><div class='contact_rank contact_"+rank+"'>"+rank+"</div></th>");
		}
		if(selected.size() >= max || isDisabled()) {
			//list is full or disabled
			out.print("<td><div class=\"contact_editor\">");
			for(FOSDE fd : selected) {
				fd.setDisabled(isDisabled());
				fd.render(out);
			}
			out.print("</div></td>");
		} else {
			//user can add more contact
			out.print("<td style=\"border: 1px solid #ccc; background-color: white;\"><div class=\"contact_editor\" onclick=\"$(this).find('.autocomplete').focus(); return false;\">");
			for(FOSDE fd : selected) {
				fd.render(out);
			}
			newfos.render(out);
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
