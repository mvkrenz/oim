package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divrep.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.form.FormElementBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.DoubleValidator;
import com.webif.divrep.form.validator.IFormElementValidator;

import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
import edu.iu.grid.oim.view.divrep.ResourceServices.ServiceEditor;

public class VOResourceOwnership extends FormElementBase {
	ArrayList<Integer> owner_id_taken = new ArrayList<Integer>();
	private Button add_button;
	private ArrayList<VORecord> vo_recs;

	class OwnershipEditor extends FormElementBase
	{
		private SelectFormElement vo;
		private TextFormElement percent;
		private Button remove_button;
		private OwnershipEditor myself;
		
		protected OwnershipEditor(DivRep parent, VOResourceOwnershipRecord rec, ArrayList<VORecord> vo_recs) {
			super(parent);
			myself = this;
		
			HashMap<Integer, String> kv = new HashMap();
			for(VORecord vo_rec : vo_recs) {
				kv.put(vo_rec.id, vo_rec.name);
			}
			vo = new SelectFormElement(this, kv);
			vo.setLabel("Select a VO Owner");
			vo.setRequired(true);
			if (rec.vo_id != null) {
				vo.setValue(rec.vo_id);
			}
			percent = new TextFormElement(this);
			percent.setLabel("Percentage of Ownership");
			percent.setRequired(true);
			percent.addValidator(new DoubleValidator());
			percent.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					VOResourceOwnership.this.validate();
					
				}});
			percent.setSampleValue("100.0");

			if (rec.percent != null) {
				percent.setValue(rec.percent.toString());
			}
			
			remove_button = new Button(this, "images/delete.png");
			remove_button.setStyle(Button.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this owner record?");
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

		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"owner_editor\">");
			
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");

			for(DivRep child : childnodes) {
				if(child == remove_button) continue;
				
				if(child instanceof FormElementBase) {
					FormElementBase elem = (FormElementBase)child;
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
		remove(owner);
		redraw();
	}
	
	public void addOwner(VOResourceOwnershipRecord rec) { 
		OwnershipEditor owner = new OwnershipEditor(this, rec, vo_recs);
		redraw();
		//validate();
	}
	
	public VOResourceOwnership(DivRep parent, ArrayList<VORecord> _vo_recs) {
		super(parent);
		vo_recs = _vo_recs;
		
		add_button = new Button(this, "Add New Owner");
		add_button.setStyle(Button.Style.ALINK);
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
		for(DivRep node : childnodes) {
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
		error.redraw();
		
		//make sure percentage sums upto 100%
		Double total = 0D;
		for(DivRep node : childnodes) {					
			if(node instanceof OwnershipEditor) {
				OwnershipEditor owner = (OwnershipEditor)node;
				if(!owner.isValid()) {
					valid = false;
					return;
				}
				total += Double.parseDouble(owner.percent.getValue());
			}
		}
		if(total <= 100D) {
			error.clear();
			valid = true;
		} else {
			error.set("Total percentage of ownership must be less than or equal to 100% -- Current total is " + total + "%");
			valid = false;
		}
	}
	
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		error.render(out);
		for(DivRep node : childnodes) {
			if(node instanceof OwnershipEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
