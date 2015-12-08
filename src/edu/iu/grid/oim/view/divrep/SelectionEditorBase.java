package edu.iu.grid.oim.view.divrep;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.validator.DivRepIValidator;

//Used by MeshConfigServlet
//this requires modified version of jquery autocomplete plugin, and client side code to make the input area to be autocomplete
abstract public class SelectionEditorBase extends DivRepFormElement<ArrayList<SelectionEditorBase.ItemDE>> {
	static Logger log = Logger.getLogger(SelectionEditorBase.class);
	
	protected ArrayList<ItemDE> selected;
	private NewItemDE newitem;
	
	public class ItemInfo {
		public Integer id;
		public String name;
		public String detail = null;
		public Boolean disabled = false;
	}
	
    /* replace multiple whitespaces between words with single blank */
    protected String itrim(String source) {
    	if(source == null) return null;
        return source.replaceAll("\\b\\s{2,}\\b", " ");
    }
	
	//you need to override this
	abstract protected ItemInfo getDetailByID(Integer id) throws SQLException;
	abstract protected Collection<ItemInfo> searchByQuery(String query) throws SQLException;
	
	// Default max contact limits - can be overridden 
	private int max = 32;
	public void setMax(int max) {
		this.max = max;
	}	
	
	public void setDisabled(Boolean b) { 
		super.setDisabled(b);
		newitem.setDisabled(b);
	}
	
	public SelectionEditorBase(DivRep parent) {
		super(parent);
		selected = new ArrayList<ItemDE>();
		super.setValue(selected);//I need to do this so that DivRepFormElement correctly fire MinValidator
		newitem = new NewItemDE(this);
	}
	
	class MinValidator implements DivRepIValidator<ArrayList<ItemDE>>
	{
		private int min;
		
		public MinValidator(int _min) {
			min = _min;
		}
		
		public String getErrorMessage() {
			return "Please specify at least " + min + " item(s)";
		}

		public Boolean isValid(ArrayList<ItemDE> recs) {
			return (recs.size() >= min);
		}
	}
	
	//autocomplete area to add new contact
	class NewItemDE extends DivRepFormElement
	{
		public NewItemDE(DivRep parent) {
			super(parent);
		}
		
		public void render(PrintWriter out) {
			out.print("<div class=\"divrep_inline\" id=\""+getNodeID()+"\">");
			out.print("<input type='text' class='autocomplete'/>");
			out.print("<script type='text/javascript'>$(document).ready(function() {setAutocomplete($('#"+getNodeID()+" input.autocomplete'), 1);});</script>");
			out.print("</div>");
		}
		
		protected void onEvent(DivRepEvent e) {

			int id = Integer.parseInt((String)e.value);
			try {
				ItemInfo info = getDetailByID(id);
				addSelected(info);
				setFormModified();
				
				js("$('#"+getNodeID()+" input').focus();");
			} catch (SQLException e1) {
				alert("Unknown contact_id");
			}
		}
	    
		//this handles the list request from the autocomplete box.
		protected void onRequest(HttpServletRequest request, HttpServletResponse response)
		{
			try {	
				//support both new & old version of autocomplete
				String query = itrim(request.getParameter("q"));		
				int limit = Integer.parseInt(request.getParameter("limit")); //only returns records upto requested limit
				Collection<ItemInfo> recs = searchByQuery(query);	
				String out = "[";
				boolean first = true;
				for(ItemInfo rec: recs) {
					if(first) {
						first = false;
					} else {
						out += ",";
					}
					if(rec.detail != null) {
						out += "{\"id\":"+rec.id+", \"name\":\""+itrim(rec.name)+"\", \"email\":\""+rec.detail+"\"}\n";
					} else {
						out += "{\"id\":"+rec.id+", \"name\":\""+itrim(rec.name)+"\"}\n";
					}
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
	
	class ItemDE extends DivRepFormElement
	{
		public ItemInfo info;
		
		private DivRepButton removebutton;
		private ItemDE myself;
		
		ItemDE(DivRep parent, ItemInfo info) {
			super(parent);
			this.info = info;
			
			myself = this;
			removebutton = new DivRepButton(this, "images/delete.png") {
				@Override
				public void onClick(DivRepEvent e) {
					removeItem(myself);
					setFormModified();
				}
			};
			removebutton.setStyle(DivRepButton.Style.IMAGE);
		}
		
		public void render(PrintWriter out)
		{
			out.print("<div class=\"divrep_inline contact divrep_round\" id=\""+getNodeID()+"\">");
			if(info.name == null) {
				out.print("(No Name)");
			} else {
				out.print(StringEscapeUtils.escapeHtml(info.name.trim()));
				if(info.detail != null) {
					out.print(" <code>"+StringEscapeUtils.escapeHtml("<"+info.detail+">")+"</code>");
				}
				if(info.disabled) {
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
	
	public void removeItem(ItemDE rec)
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
	
	public void addSelected(ItemInfo info)
	{
		ItemDE newde = new ItemDE(this, info);
		selected.add(newde);
		validate();
		redraw();	
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
			renderList(out, newitem, selected, max);
			out.print("</table>");
			error.render(out);
		}
		out.print("</div>");
	}
	
	public void renderList(PrintWriter out, NewItemDE newitem, ArrayList<ItemDE> selected, int max)
	{
		out.print("<tr>");
		if(selected.size() >= max || isDisabled()) {
			//list is full or disabled
			out.print("<td><div class=\"contact_editor\">");
			for(ItemDE item : selected) {
				item.setDisabled(isDisabled());
				item.render(out);
			}
			out.print("</div></td>");
		} else {
			//user can add more contact
			out.print("<td style=\"border: 1px solid #ccc; background-color: white;\"><div class=\"contact_editor\" onclick=\"$(this).find('.autocomplete').focus(); return false;\">");
			for(ItemDE item: selected) {
				item.render(out);
			}
			newitem.render(out);
			out.write("</div>");
			
			out.write("</td>");
		}
		out.print("</tr>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub	
	}
	/*
	public ArrayList<MeshConfigOIMMemberRecord> getRecords(Integer group_id, Integer service_id) {
		ArrayList<MeshConfigOIMMemberRecord> recs = new ArrayList<MeshConfigOIMMemberRecord>();
		for(ItemDE de : selected) {
			MeshConfigOIMMemberRecord rec = new MeshConfigOIMMemberRecord();
			rec.group_id = group_id;
			rec.item_id = de.info.rec.id;
			rec.service_id = service_id;
			recs.add(rec);
		}
		return recs;
	}
	*/
	public ArrayList<ItemDE> getSelected() {
		return selected;
	}
}
