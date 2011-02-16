package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepDoubleValidator;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.db.record.VOResourceOwnershipRecord;
import edu.iu.grid.oim.view.divrep.ResourceServices.ServiceEditor;

public class VOResourceOwnership extends DivRepFormElement {
	ArrayList<Integer> owner_id_taken = new ArrayList<Integer>();
	private DivRepButton add_button;
	private ArrayList<VORecord> vo_recs;

	class OwnershipEditor extends DivRepFormElement
	{
		private DivRepSelectBox vo;
		private DivRepTextBox percent;
		private DivRepButton remove_button;
		private OwnershipEditor myself;
		
		protected OwnershipEditor(DivRep parent, VOResourceOwnershipRecord rec, ArrayList<VORecord> vo_recs) {
			super(parent);
			myself = this;
		
			Collections.sort(vo_recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			LinkedHashMap<Integer, String> kv = new LinkedHashMap();
			for(VORecord vo_rec : vo_recs) {
				kv.put(vo_rec.id, vo_rec.name);
			}
			
			vo = new DivRepSelectBox(this, kv);
			vo.setLabel("Select a VO Owner");
			vo.setRequired(true);
			if (rec.vo_id != null) {
				vo.setValue(rec.vo_id);
			}
			percent = new DivRepTextBox(this);
			percent.setLabel("Percentage of Ownership");
			percent.setRequired(true);
			percent.addValidator(new DivRepDoubleValidator());
			percent.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					VOResourceOwnership.this.validate();
					
				}});
			percent.setSampleValue("100.0");

			if (rec.percent != null) {
				percent.setValue(rec.percent.toString());
			}
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this owner record?");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeOwner(myself);	
				}
			});

		}

		public void addVOEventListener(DivRepEventListener listener) {
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

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"owner_editor\">");
			
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");

			for(DivRep child : childnodes) {
				if(child == remove_button) continue;
				
				if(child instanceof DivRepFormElement) {
					DivRepFormElement elem = (DivRepFormElement)child;
					if(!elem.isHidden()) {
						out.print("<div class=\"divrep_form_element\">");
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
		modified(true);
		redraw();
	}
	
	public void addOwner(VOResourceOwnershipRecord rec) { 
		OwnershipEditor owner = new OwnershipEditor(this, rec, vo_recs);
		redraw();
		
		//don't set modifed(true) here - addOwner is called by init and doing so will make form to popup confirmation
		//everytime user opens it
	}
	
	public VOResourceOwnership(DivRep parent, ArrayList<VORecord> _vo_recs) {
		super(parent);
		vo_recs = _vo_recs;
		
		add_button = new DivRepButton(this, "Add New Owner");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addOwner(new VOResourceOwnershipRecord());
				modified(true);
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

	protected void onEvent(DivRepEvent e) {
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
