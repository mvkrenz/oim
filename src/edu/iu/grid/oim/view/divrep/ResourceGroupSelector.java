package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.FacilityModel;
import edu.iu.grid.oim.model.db.ResourceGroupModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SiteModel;
import edu.iu.grid.oim.model.db.record.FacilityRecord;
import edu.iu.grid.oim.model.db.record.ResourceGroupRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SiteRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class ResourceGroupSelector extends DivRepFormElement<Integer> {

	static Logger log = Logger.getLogger(ResourceGroupSelector.class);  
    private Context context;
    
    private DivRepSelectBox site;
    private DivRepSelectBox resource_group;

    public ResourceGroupSelector(DivRep parent, Context _context) {
		super(parent);
		context = _context;
		
		SiteModel smodel = new SiteModel(context);
		site = new DivRepSelectBox(this);
		site.setLabel("Site");
		LinkedHashMap<Integer, String> sites = new LinkedHashMap<Integer, String>();
		ArrayList<SiteRecord> srecs;
		try {
			srecs = smodel.getAll();
			Collections.sort(srecs, new Comparator<SiteRecord> (){
				public int compare(SiteRecord a, SiteRecord b) {
					return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
				}
			});
			for(SiteRecord srec : srecs) {
				sites.put(srec.id, srec.name);
			}
			site.setValues(sites);
			site.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					if(site.getValue() == null) {
						resource_group.setNullLabel("(Please Select Site First)");
						resource_group.setValue(null);
						resource_group.setValues(new LinkedHashMap<Integer, String>());
						resource_group.validate();
						resource_group.redraw();
					} else {
						setValue(site.getValue(), null);
					}
				}
			});
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resource_group = new DivRepSelectBox(this);
		resource_group.setLabel("Resource Group");
		resource_group.setNullLabel("(Please Select Site First)");
		
		/*
		ResourceGroupModel rgmodel = new ResourceGroupModel(context);
		try {
			ArrayList<SiteRecord> srecs = smodel.getAll();
			Collections.sort(srecs, new Comparator<SiteRecord> (){
				public int compare(SiteRecord a, SiteRecord b) {
					return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
				}
			});
			for(SiteRecord srec : srecs) {
				ArrayList<ResourceGroupRecord> rgrecs = rgmodel.getBySiteID(srec.id);
				LinkedHashMap<Integer, String> rgs = new LinkedHashMap<Integer, String>();
				for(ResourceGroupRecord rgrec : rgrecs) {
					rgs.put(rgrec.id, rgrec.name);
				}
				addGroup("(Site) " + srec.name, rgs);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
	}
    
    private void setValue(Integer site_id, Integer resource_group_id) {
		try {
			site.setValue(site_id);
			site.redraw();
			
			//load resource group list
			ResourceGroupModel rgmodel = new ResourceGroupModel(context);
			ArrayList<ResourceGroupRecord> rgrecs = rgmodel.getBySiteID(site_id);
			LinkedHashMap<Integer, String> rgs = new LinkedHashMap<Integer, String>();
			for(ResourceGroupRecord rgrec : rgrecs) {
				rgs.put(rgrec.id, rgrec.name);
			}
			
			if(resource_group_id == null) {
				//select first resource group if there is only 1 resource group
				if(rgrecs.size() == 1) {
					resource_group.setValue(rgrecs.get(0).id);
				} else {
					resource_group.setValue(null);
				}
			} else {
				resource_group.setValue(resource_group_id);
			}
			resource_group.setNullLabel("(Please Select)");
			resource_group.setValues(rgs);
			resource_group.validate();
			resource_group.redraw();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    
    public void setValue(Integer resource_group_id) {
		ResourceGroupModel rgmodel = new ResourceGroupModel(context);
		try {
			ResourceGroupRecord rgrec = rgmodel.get(resource_group_id);
			setValue(rgrec.site_id, resource_group_id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    public Integer getValue() { 
    	return resource_group.getValue();
    }
    
	public void setRequired(Boolean b) { 
		//super.setRequired(b);//don't set required on super.. we don't have any associated with it
		site.setRequired(b);
		resource_group.setRequired(b);
	}

	public void render(PrintWriter out) {
		out.write("<div class=\"divrep_form_element\" id=\""+getNodeID()+"\">");
		site.render(out);
		out.write("<div class=\"indent\">");
		resource_group.render(out);
		out.write("</div>");
		out.write("</div>");
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
