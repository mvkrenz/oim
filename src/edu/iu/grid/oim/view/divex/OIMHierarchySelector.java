package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.FormElementDEBase;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;

public class OIMHierarchySelector extends FormElementDEBase<Integer> {
    static Logger log = Logger.getLogger(OIMHierarchySelector.class);  
	
    private Context context;
    
	//target element to select
	public enum Type {SITE, FACILITY, RESOURCE, RESOURCE_GROUP};
	private Type type;
	private Authorization auth;
	private FacilitySelector facility_selector;
	ArrayList<Selectable> selectables = new ArrayList();
	
	abstract class ExpandableSelector extends DivEx {
		HashMap<Integer/*id*/, FormElementDEBase> items = new HashMap();
		ExpandableSelector mine;	
		
		public ExpandableSelector(DivEx _parent) throws SQLException {
			super(_parent);
			mine = this;
		}

		protected void onEvent(Event e) {
			//no event to handle
		}

		public void render(PrintWriter out) {
			for(FormElementDEBase expandable : items.values()) {
				expandable.render(out);
			}
		}
		public FormElementDEBase getItem(Integer id) 
		{
			return items.get(id);
		}
		abstract protected void loadChild(Expandable<Integer> item);
	}
	
	class FacilitySelector extends ExpandableSelector {
		public FacilitySelector(DivEx _parent) throws SQLException {
			super(_parent);
			FacilityModel model = new FacilityModel(context);
			for(FacilityRecord rec : model.getAll()) {				
				final FormElementDEBase<Integer> item;
				if(type == Type.FACILITY) {
					item = new Selectable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							select((Selectable)item);
						}
					});
					selectables.add((Selectable)item);
				} else {
					item = new Expandable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							loadChild((Expandable)item);
						}
					});
				}
				item.setLabel("(Facility) "+rec.name);
				item.setValue(rec.id);
				items.put(rec.id, item);
			}
		}
				
		public void loadChild(Expandable<Integer> item) 
		{
			if(item.getChild() == null) {
				try {
					item.setChild(new SiteSelector(mine, item.getValue()));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class SiteSelector extends ExpandableSelector {		
		public SiteSelector(DivEx _parent, int facility_id) throws SQLException {
			super(_parent);
			SiteModel model = new SiteModel(context);
			for(SiteRecord rec : model.getByFacilityID(facility_id)) {	
				final FormElementDEBase<Integer> item;
				if(type == Type.SITE) {
					item = new Selectable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							select((Selectable)item);
						}
					});
					selectables.add((Selectable) item);
				} else {
					item = new Expandable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							loadChild((Expandable)item);
						}
					});
				}
				item.setLabel("(Site) "+rec.name);
				item.setValue(rec.id);
				items.put(rec.id, item);
			}
		}
		
		public void loadChild(Expandable<Integer> item) 
		{
			if(item.getChild() == null) {
				try {
					item.setChild(new ResourceGroupSelector(mine, item.getValue()));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class ResourceGroupSelector extends ExpandableSelector {		
		public ResourceGroupSelector(DivEx _parent, int site_id) throws SQLException {
			super(_parent);
			ResourceGroupModel model = new ResourceGroupModel(context);
			for(ResourceGroupRecord rec : model.getBySiteID(site_id)) {			
				final FormElementDEBase<Integer> item;
				if(type == Type.RESOURCE_GROUP) {
					item = new Selectable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							select((Selectable)item);
						}
					});
					selectables.add((Selectable) item);
				} else {
					item = new Expandable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							loadChild((Expandable)item);
						}
					});
				}
				item.setLabel("(Resource Group) "+rec.name);
				item.setValue(rec.id);
				items.put(rec.id, item);
			}
		}
		
		public void loadChild(Expandable<Integer> item) 
		{
			if(item.getChild() == null) {
				try {
					item.setChild(new ResourceSelector(mine, item.getValue()));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			item.setExpand(true);
		}
	}
	
	class ResourceSelector extends ExpandableSelector {
		public ResourceSelector(DivEx _parent, int rg_id) throws SQLException {
			super(_parent);
			ResourceModel model = new ResourceModel(context);
			for(ResourceRecord rec : model.getByGroupID(rg_id)) {
				final FormElementDEBase<Integer> item;
				if(type == Type.RESOURCE) {
					item = new Selectable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							select((Selectable)item);
						}
					});
					selectables.add((Selectable) item);
				} else {
					item = new Expandable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							loadChild((Expandable)item);
						}
					});
				}
				item.setLabel("(Resource) "+rec.name);
				item.setValue(rec.id);
				items.put(rec.id, item);
			}
		}
		
		public void loadChild(Expandable<Integer> item) 
		{
			//nothing to expand;
			//item.setExpand(true);
		}
	}
	
	public OIMHierarchySelector(DivEx parent, Context _context, Type _type) throws SQLException {
		super(parent);
		type = _type;
		context = _context;
		auth = context.getAuthorization();
		
		//let's begin with facility
		facility_selector = new FacilitySelector(this);
	}

	//override this to add auto-expand feature
	public void setValue(Integer id) { 
		super.setValue(id);

		if(id == null) {
			select(null);
		} else {
		
			Integer facility_id = null;
			Integer site_id = null;
			Integer resource_group_id = null;
			Integer resource_id = null;
			
			try {
				//trace IDs in backward to find which item to expand (notice no-break)
				switch(type) {
	
				case RESOURCE:
					resource_id = id;
					ResourceModel rmodel = new ResourceModel(context);
					ResourceRecord rrec = rmodel.get(id);
					resource_group_id = rrec.resource_group_id;
					id = resource_group_id;
				case RESOURCE_GROUP:
					resource_group_id = id;
					ResourceGroupModel rgmodel = new ResourceGroupModel(context);
					ResourceGroupRecord rgrec = rgmodel.get(id);
					site_id = rgrec.site_id;
					id = site_id;		
				case SITE:
					site_id = id;
					SiteModel smodel = new SiteModel(context);
					SiteRecord srec = smodel.get(id);
					facility_id = srec.facility_id;
					id = facility_id;
				case FACILITY:
					facility_id = id;
				}
				
				//expand one by one
				FormElementDEBase elem = null;
				Expandable<Integer> item = null;
				if(facility_id != null) {
					elem = facility_selector.getItem(facility_id);
					if(elem instanceof Expandable) {
						item = (Expandable)elem;
						facility_selector.loadChild(item);
						item.setExpand(true);
						item.redraw();
					}
				}
				if(site_id != null) {
					SiteSelector selector = (SiteSelector)item.getChild();
					elem = selector.getItem(site_id);
					if(elem instanceof Expandable) {
						item = (Expandable)elem;
						selector.loadChild(item);
						item.setExpand(true);
						item.redraw();
					}
				}
				if(resource_group_id != null) {
					ResourceGroupSelector selector = (ResourceGroupSelector)item.getChild();
					elem = selector.getItem(resource_group_id);
					if(elem instanceof Expandable) {
						item = (Expandable)elem;
						selector.loadChild(item);
						item.setExpand(true);
						item.redraw();
					}
				}
				if(resource_id != null) {
					ResourceSelector selector = (ResourceSelector)item.getChild();
					elem = selector.getItem(resource_id);
					if(elem instanceof Expandable) {
						//for now, this should never be called
						item = (Expandable)elem;
						selector.loadChild(item);
						item.setExpand(true);
						item.redraw();			
					}
				}
				
				//finally, the last one that is pulled as elem should be selectable that we must select
				select((Selectable)elem);
				elem.scrollToShow("#" + getNodeID() + " .oim_hierarchy");
				
			} catch (SQLException e) {
				//TODO..
			}
		}
	}
	
	//selectable item
	class Selectable<T> extends FormElementDEBase<T> {
		protected Selectable(DivEx parent) {
			super(parent);
			// TODO Auto-generated constructor stub
		}

		Boolean selected = false;
		public void setSelected(Boolean b) { selected = b; }
		public Boolean isSelected() { return selected; }

		protected void onEvent(Event e) {	
		}

		public void render(PrintWriter out) {
			String selected_class = "";
			if(selected) {
				selected_class="selected";
			}
			out.write("<div class=\""+selected_class+"\" onclick=\"divex(this.id, event)\" id=\""+getNodeID()+"\">");
			out.write(label);
			out.write("</div>");
		}
	}
	
	//expandable item
	class Expandable<T> extends FormElementDEBase<T> {
		private Boolean expanded = false;
		public void setExpand(Boolean b) { expanded = b; }
		public Boolean isExpanded() { return expanded; }
		
		DivEx child = null;
		public void setChild(DivEx _child) {
			child = _child;
		}
		public DivEx getChild() {
			return child;
		}
		
		public Expandable(DivEx _parent) {
			super(_parent);
		}
		public void render(PrintWriter out) {
			out.write("<div onclick=\"divex(this.id, event)\" id=\""+getNodeID()+"\">");
	
			if(expanded) {
				out.write("<img align=\"top\" src=\""+Config.getStaticBase()+"/images/minusbox.gif\"/>" + label);
				out.write("<div class=\"indent\">");
				if(child != null) {
					child.render(out);
				} else {
					out.write("(no children set)");
				}
				out.write("</div>");
			} else {
				out.write("<img align=\"top\" src=\""+Config.getStaticBase()+"/images/plusbox.gif\"/>" + label);				
			}
			out.write("</div>");
		}
		protected void onEvent(Event e) {
			expanded = !expanded;
			redraw();			
		}
	}

	protected void onEvent(Event e) {		
	}

	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		if(label != null) {
			out.write("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
		}
		out.write("<table><tr><td>");
		out.write("<div class=\"oim_hierarchy\">");
		facility_selector.render(out);
		out.write("</div>");
		out.write("</td><td>");
		if(isRequired()) {
			out.print(" * Required");
		}
		out.write("</td></tr></table>");
		if(error != null) {
			out.print("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
		}
		out.write("</div>");
	}
	
	public void select(Selectable<Integer> sel) {
		
		//de-select current selection
		for(Selectable s : selectables) {
			if(s.isSelected()) {
				s.setSelected(false);
				s.redraw();
			}
		}
		
		if(sel != null) {
			//select new selection
			sel.setSelected(true);
			sel.redraw();
			value = sel.getValue();
		} else {
			value = null;
		}
	}

}
