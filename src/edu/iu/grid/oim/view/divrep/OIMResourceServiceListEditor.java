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
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.model.db.record.MeshConfigOIMMemberRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;

//Used by MeshConfigServlet
//this requires modified version of jquery autocomplete plugin, and client side code to make the input area to be autocomplete
abstract public class OIMResourceServiceListEditor extends DivRepFormElement<ArrayList<OIMResourceServiceListEditor.ResourceDE>> {
	static Logger log = Logger.getLogger(OIMResourceServiceListEditor.class);
	
	private ArrayList<ResourceDE> selected;
	private NewResourceDE newresource;
	
	public class ResourceInfo {
		public ResourceRecord rec;
		public String detail;
	}
	
	//you need to override this
	abstract protected ResourceInfo getDetailByResourceID(Integer id) throws SQLException;
	abstract protected Collection<ResourceInfo> getAvailableResourceRecords() throws SQLException;
	
	// Default max contact limits - can be overridden 
	private int max = 32;
	public void setMax(int max) {
		this.max = max;
	}	
	
	public void setDisabled(Boolean b) { 
		super.setDisabled(b);
		newresource.setDisabled(b);
	}
	
	public OIMResourceServiceListEditor(DivRep parent) {
		super(parent);
		
		selected = new ArrayList<ResourceDE>();
		super.setValue(selected);//I need to do this so that DivRepFormElement correctly fire MinValidator
		
		newresource = new NewResourceDE(this);
	}
	
	class MinValidator implements DivRepIValidator<ArrayList<ResourceDE>>
	{
		private int min;
		
		public MinValidator(int _min) {
			min = _min;
		}
		
		public String getErrorMessage() {
			return "Please specify at least " + min + " resource(s)";
		}

		public Boolean isValid(ArrayList<ResourceDE> recs) {
			return (recs.size() >= min);
		}
	}
	
	//autocomplete area to add new contact
	class NewResourceDE extends DivRepFormElement
	{
		public NewResourceDE(DivRep parent) {
			super(parent);
		}
		
		public void render(PrintWriter out) {
			out.print("<div class=\"divrep_inline\" id=\""+getNodeID()+"\">");
			out.print("<input type='text' class='autocomplete'/>");
			out.print("<script type='text/javascript'>$(document).ready(function() {setAutocomplete($('#"+getNodeID()+" input.autocomplete'), 1);});</script>");
			out.print("</div>");
		}
		
		protected void onEvent(DivRepEvent e) {

			int resource_id = Integer.parseInt((String)e.value);
			try {
				ResourceInfo info = getDetailByResourceID(resource_id);
				addSelected(info);
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
				Collection<ResourceInfo> all = getAvailableResourceRecords();
				HashMap<Integer, ResourceInfo> recs = new HashMap();
				ResourceInfo best_guess = null;
				int best_guess_distance = 10000;
				//filter records that matches the query upto limit
				for(ResourceInfo info : all) {
					if(recs.size() > limit) break;
					
					if(info.rec.name != null) {
						String name = itrim(info.rec.name.toLowerCase());
						if(name.contains(query.toLowerCase())) {
							recs.put(info.rec.id, info);
							continue;
						}
						
						//calculate levenshtein distance per token
						for(String token : info.rec.name.split(" ")) {
							int distance = StringUtils.getLevenshteinDistance(token, query);
							if(best_guess_distance > distance) {
								best_guess = info;
								best_guess_distance = distance;
							}
						}
					}
					if(info.detail != null) {
						String name = info.detail.toLowerCase();
						if(name.contains(query.toLowerCase())) {
							recs.put(info.rec.id, info);
							continue;
						}
					}
					if(info.rec.fqdn != null) {
						String name = info.rec.fqdn.toLowerCase();
						if(name.contains(query.toLowerCase())) {
							recs.put(info.rec.id, info);
							continue;
						}
					}
				}
				
				//if no match was found, pick the closest match
				if(recs.size() == 0 && best_guess != null) {
					recs.put(best_guess.rec.id, best_guess);	
				}
		
				//remove resources that are already selected 
				for(ResourceDE r : selected) {
					recs.remove(r.info.rec.id);
				}
	
	
				String out = "[";
				boolean first = true;
				for(ResourceInfo info: recs.values()) {
					if(first) {
						first = false;
					} else {
						out += ",";
					}
					String detail = info.detail;
					if(detail == null) {
						detail = info.rec.fqdn;
					}
					out += "{\"id\":"+info.rec.id+", \"name\":\""+itrim(info.rec.name)+"\", \"email\":\""+detail+"\"}\n";
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
	
	class ResourceDE extends DivRepFormElement
	{
		public ResourceInfo info;
		
		private DivRepButton removebutton;
		private ResourceDE myself;
		
		ResourceDE(DivRep parent, ResourceInfo info) {
			super(parent);
			this.info = info;
			
			myself = this;
			removebutton = new DivRepButton(this, "images/delete.png") {
				@Override
				public void onClick(DivRepEvent e) {
					removeResource(myself);
					setFormModified();
				}
			};
			removebutton.setStyle(DivRepButton.Style.IMAGE);
		}
		
		public void render(PrintWriter out)
		{
			out.print("<div class=\"divrep_inline contact divrep_round\" id=\""+getNodeID()+"\">");
			if(info.rec.name == null) {
				out.print("(No Name)");
			} else {
				out.print(StringEscapeUtils.escapeHtml(info.rec.name.trim()));
				if(info.detail != null) {
					out.print(" <code>"+StringEscapeUtils.escapeHtml("<"+info.detail+">")+"</code>");
				} else if(info.rec.fqdn != null) {
					//use fqdn if no detail is given
					out.print(" <code>"+StringEscapeUtils.escapeHtml("<"+info.rec.fqdn+">")+"</code>");
				}
				if(info.rec.disable) {
					out.print(" <span class=\"label label-important\">Disabled</span>");
				}
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
	
	public void removeResource(ResourceDE rec)
	{	
		selected.remove(rec);
		validate();
		redraw();
	}
	
	//remove all selected items
	public void clear() {
		selected.clear();
		redraw();
	}
	
	public void addSelected(ResourceInfo info)
	{
		ResourceDE newde = new ResourceDE(this, info);
		selected.add(newde);
		validate();
		redraw();	
	}
	
	/*
	public HashMap<ContactRecord> getResources()
	{
		HashMap<ContactRecord, ContactRank> records = new HashMap();
		for(ContactRank rank : selected.keySet()) {
			ArrayList<ContactDE> cons = selected.get(rank);
			for(ContactDE con : cons) {
				records.put(con.person, rank);
			}
		}
		return records;
	}
	*/
	
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
			renderList(out, newresource, selected, max);
			out.print("</table>");
			error.render(out);
		}
		out.print("</div>");
	}
	
	public void renderList(PrintWriter out, NewResourceDE newresource, ArrayList<ResourceDE> selected, int max)
	{
		out.print("<tr>");
		if(selected.size() >= max || isDisabled()) {
			//list is full or disabled
			out.print("<td><div class=\"contact_editor\">");
			for(ResourceDE resource : selected) {
				resource.setDisabled(isDisabled());
				resource.render(out);
			}
			out.print("</div></td>");
		} else {
			//user can add more contact
			out.print("<td style=\"border: 1px solid #ccc; background-color: white;\"><div class=\"contact_editor\" onclick=\"$(this).find('.autocomplete').focus(); return false;\">");
			for(ResourceDE resource: selected) {
				resource.render(out);
			}
			newresource.render(out);
			out.write("</div>");
			
			out.write("</td>");
		}
		out.print("</tr>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub	
	}
	public ArrayList<MeshConfigOIMMemberRecord> getRecords(Integer group_id, Integer service_id) {
		ArrayList<MeshConfigOIMMemberRecord> recs = new ArrayList<MeshConfigOIMMemberRecord>();
		for(ResourceDE de : selected) {
			MeshConfigOIMMemberRecord rec = new MeshConfigOIMMemberRecord();
			rec.group_id = group_id;
			rec.resource_id = de.info.rec.id;
			rec.service_id = service_id;
			recs.add(rec);
		}
		return recs;
	}
}
