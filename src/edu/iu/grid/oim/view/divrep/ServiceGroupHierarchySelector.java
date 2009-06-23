package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.common.FormElement;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ServiceGroupModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.ServiceGroupRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ServiceGroupHierarchySelector extends FormElement<Integer> {
    static Logger log = Logger.getLogger(ServiceGroupHierarchySelector.class);  
	
    private Context context;
    
	//target element to select
	public enum Type {SERVICE, SERVICE_GROUP};
	private Type type;
	private Authorization auth;
	private ServiceGroupSelector service_group_selector;
	ArrayList<Selectable> selectables = new ArrayList();
	
	abstract class ExpandableSelector extends DivRep {
		HashMap<Integer/*id*/, FormElement> items = new HashMap();
		ExpandableSelector mine;	
		
		public ExpandableSelector(DivRep _parent) throws SQLException {
			super(_parent);
			mine = this;
		}

		protected void onEvent(Event e) {
			//no event to handle
		}

		public void render(PrintWriter out) {
			for(FormElement expandable : items.values()) {
				expandable.render(out);
			}
		}
		public FormElement getItem(Integer id) 
		{
			return items.get(id);
		}
		abstract protected void loadChild(Expandable<Integer> item);
	}
	
	class ServiceGroupSelector extends ExpandableSelector {
		public ServiceGroupSelector(DivRep _parent) throws SQLException {
			super(_parent);
			ServiceGroupModel model = new ServiceGroupModel(context);
			for(ServiceGroupRecord rec : model.getAll()) {				
				final FormElement<Integer> item;
				if(type == Type.SERVICE_GROUP) {
					item = new Selectable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							select((Selectable)item);
							modified(true);
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
				item.setLabel("(Service Group) "+rec.name);
				item.setValue(rec.id);
				items.put(rec.id, item);
			}
		}
				
		public void loadChild(Expandable<Integer> item) 
		{
			if(item.getChild() == null) {
				try {
					item.setChild(new ServiceSelector(mine, item.getValue()));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	class ServiceSelector extends ExpandableSelector {		
		public ServiceSelector(DivRep _parent, int service_group_id) throws SQLException {
			super(_parent);
			ServiceModel model = new ServiceModel(context);
			for(ServiceRecord rec : model.getByServiceGroupID(service_group_id)) {	
				final FormElement<Integer> item;
				if(type == Type.SERVICE) {
					item = new Selectable<Integer>(this);
					item.addEventListener(new EventListener() {
						public void handleEvent(Event e) {
							select((Selectable)item);
							modified(true);
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
				item.setLabel("(Service) "+rec.name);
				item.setValue(rec.id);
				items.put(rec.id, item);
			}
		}

		@Override
		protected void loadChild(Expandable<Integer> item) {
			//nothing to expand;
		}
	}
	
	public ServiceGroupHierarchySelector(DivRep parent, Context _context, Type _type) throws SQLException {
		super(parent);
		type = _type;
		context = _context;
		auth = context.getAuthorization();
		
		//let's begin with facility
		service_group_selector = new ServiceGroupSelector(this);
	}

	//override this to add auto-expand feature
	public void setValue(Integer id) { 
		super.setValue(id);

		if(id == null) {
			select(null);
		} else {
		
			Integer service_group_id = null;
			Integer service_id = null;
			
			try {
				//trace IDs in backward to find which item to expand (notice no-break)
				switch(type) {
	
				case SERVICE:
					service_id = id;
					ServiceModel smodel = new ServiceModel(context);
					ServiceRecord srec = smodel.get(id);
					service_group_id = srec.service_group_id;
					id = service_group_id;
				case SERVICE_GROUP:
					service_group_id = id;
				}
				
				//expand one by one
				FormElement elem = null;
				Expandable<Integer> item = null;
				if(service_group_id != null) {
					elem = service_group_selector.getItem(service_group_id);
					if(elem instanceof Expandable) {
						item = (Expandable)elem;
						service_group_selector.loadChild(item);
						item.setExpand(true);
						item.redraw();
					}
				}
				if(service_id != null) {
					ServiceSelector selector = (ServiceSelector)item.getChild();
					elem = selector.getItem(service_id);
					if(elem instanceof Expandable) {
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
	class Selectable<T> extends FormElement<T> {
		protected Selectable(DivRep parent) {
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
			out.write("<div class=\""+selected_class+"\" onclick=\"divrep(this.id, event)\" id=\""+getNodeID()+"\">");
			out.write(label);
			out.write("</div>");
		}
	}
	
	//expandable item
	class Expandable<T> extends FormElement<T> {
		private Boolean expanded = false;
		public void setExpand(Boolean b) { expanded = b; }
		public Boolean isExpanded() { return expanded; }
		
		DivRep child = null;
		public void setChild(DivRep _child) {
			child = _child;
		}
		public DivRep getChild() {
			return child;
		}
		
		public Expandable(DivRep _parent) {
			super(_parent);
		}
		public void render(PrintWriter out) {
			out.write("<div onclick=\"divrep(this.id, event)\" id=\""+getNodeID()+"\">");
	
			if(expanded) {
				out.write("<img align=\"top\" src=\""+StaticConfig.getStaticBase()+"/images/minusbox.gif\"/>" + label);
				out.write("<div class=\"indent\">");
				if(child != null) {
					child.render(out);
				} else {
					out.write("(no children set)");
				}
				out.write("</div>");
			} else {
				out.write("<img align=\"top\" src=\""+StaticConfig.getStaticBase()+"/images/plusbox.gif\"/>" + label);				
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
		service_group_selector.render(out);
		out.write("</div>");
		out.write("</td><td>");
		if(isRequired()) {
			out.print(" * Required");
		}
		out.write("</td></tr></table>");
		error.render(out);
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
		
		validate();
	}
}
