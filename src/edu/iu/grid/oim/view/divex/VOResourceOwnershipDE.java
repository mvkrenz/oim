package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
import edu.iu.grid.oim.view.divex.ResourceServicesDE.ServiceEditor;

public class VOResourceOwnershipDE extends FormElementDEBase {

	ArrayList<OwnershipEditor> owners = new ArrayList<OwnershipEditor>();
	ArrayList<Integer> owner_id_taken = new ArrayList<Integer>();
	private ButtonDE add_button;
	private ArrayList<VORecord> vo_recs;

	class OwnershipEditor extends FormElementDEBase
	{
		private SelectFormElementDE vo;
		private TextFormElementDE percent;
		private ButtonDE remove_button;
		private OwnershipEditor myself;
		
		protected OwnershipEditor(DivEx parent, VOResourceOwnershipRecord rec, ArrayList<VORecord> vo_recs) {
			super(parent);
			myself = this;
		
			HashMap<Integer, String> kv = new HashMap();
			for(VORecord vo_rec : vo_recs) {
				kv.put(vo_rec.id, vo_rec.name);
			}
			vo = new SelectFormElementDE(this, kv);
			vo.setLabel("Select a VO Owner");
			vo.setRequired(true);
			if (rec.vo_id != null) {
				vo.setValue(rec.vo_id);
			}
			percent = new TextFormElementDE(this);
			//text.addClass("inline");
			percent.setLabel("Percentage of Ownership");
			percent.setRequired(true);

			if (rec.percent != null) {
				percent.setValue(rec.percent.toString());
			}
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.setConfirm(true, "Do you really want to remove this owner record?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeOwner(myself);	
				}
			});

		}

		public void addVOEventListener(EventListener listener) {
			vo.addEventListener(listener);
		}

		public void setVO(Integer value) {
			vo.setValue(value);
		}
		public Integer getVO() {
			return vo.getValue();
		}

		public void setPercent(String value) {
			percent.setValue(value);
		}
		public String getPercent() {
			return percent.getValue();
		}

		public VOResourceOwnershipRecord getVOResourceOwnershipRecord () {
			VOResourceOwnershipRecord rec = new VOResourceOwnershipRecord();
			rec.vo_id = vo.getValue();
			rec.percent     = percent.getValueAsDouble();
			return rec;
		}
		
		@Override
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		public ArrayList<OwnershipEditor> getOwnershipEditors()
		{
			return owners;
		}
		
//		public void removeOwner(OwnershipEditor owner)
//		{
//			owners.remove(owner);
//			redraw();
//			
//			//notify any listener of our action
//			Event e = new Event(null, null);
//			e.action = "remove";
//			e.value = owner;
//			notifyListener(e);
//		}
//		
//		public void addOwner(VOResourceOwnershipRecord rec) { 
//			OwnershipEditor owner = new OwnershipEditor(this, rec, vo_recs);
//			owners.add(owner);
//			redraw();
//			
//			//notify any listener of our action
//			Event e = new Event(null, null);
//			e.action = "add";
//			e.value = owner;
//			notifyListener(e);
//		}
//		
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"owner_editor\">");
			
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");

			for(DivEx child : childnodes) {
				if(child == remove_button) continue;
				
				if(child instanceof FormElementDEBase) {
					FormElementDEBase elem = (FormElementDEBase)child;
					if(!elem.isHidden()) {
						out.print("<div class=\"form_element\">");
						child.render(out);
						out.print("</div>");
					}
				} else {
					//non form element..
					child.render(out);
				}
			}
			
			out.write("</div>");
		}
	}
	
	public void removeOwner(OwnershipEditor owner)
	{
		owners.remove(owner);
		redraw();
		
		//notify any listener of our action
		Event e = new Event(null, null);
		e.action = "remove";
		e.value = owner;
		notifyListener(e);
	}
	
	public void addOwner(VOResourceOwnershipRecord rec) { 
		OwnershipEditor owner = new OwnershipEditor(this, rec, vo_recs);
		owners.add(owner);
		redraw();
		
		//notify any listener of our action
		Event e = new Event(null, null);
		e.action = "add";
		e.value = owner;
		notifyListener(e);
	}
	
	public VOResourceOwnershipDE(DivEx parent, ArrayList<VORecord> _vo_recs) {
		super(parent);
		vo_recs = _vo_recs;

		add_button = new ButtonDE(this, "Add New Owner");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addOwner(new VOResourceOwnershipRecord());
			}
			
		});
	}
	
	//Note: caller need to set the resource_id for each records
	public ArrayList<VOResourceOwnershipRecord> getOwners()
	{
		ArrayList<VOResourceOwnershipRecord> records = new ArrayList<VOResourceOwnershipRecord>();
		for(DivEx node : childnodes) {
			if(node instanceof OwnershipEditor) {
				OwnershipEditor owner = (OwnershipEditor)node;
				VOResourceOwnershipRecord rec = owner.getVOResourceOwnershipRecord();
				if(rec != null)  {
					records.add(rec);
				}
			}
		}
		return records;
	}

	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}
	
	public void validate()
	{
		//validate all downtimes
		redraw();
		valid = true;
		for(OwnershipEditor owner : owners) {
			if(!owner.isValid()) {
				valid = false;
			}
		}
	}	
	
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(DivEx node : childnodes) {
			if(node instanceof OwnershipEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
