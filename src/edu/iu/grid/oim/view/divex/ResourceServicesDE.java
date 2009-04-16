package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ResourceServicesDE extends FormElementDEBase {

	ArrayList<ServiceEditor> services = new ArrayList<ServiceEditor>();
	ArrayList<Integer> service_id_taken = new ArrayList<Integer>();
	private ButtonDE add_button;
	private ArrayList<ServiceRecord> service_recs;

	class ServiceEditor extends DivEx
	{
		//service details
		private SelectFormElementDE service;
		private TextFormElementDE endpoint_override;
		private CheckBoxFormElementDE hidden;
		private CheckBoxFormElementDE central;
		private TextFormElementDE server_list_regex;
		
		private ButtonDE remove_button;
		private ServiceEditor myself;
		
		protected ServiceEditor(DivEx parent, ResourceServiceRecord rec, ArrayList<ServiceRecord> service_recs) {
			super(parent);
			myself = this;
			
			HashMap<Integer, String> kv = new HashMap();
			for(ServiceRecord srec : service_recs) {
				kv.put(srec.id, srec.name);
			}
			service = new SelectFormElementDE(this, kv);
			service.setLabel("Service Name");
			service.setRequired(true);
			if(rec != null) {
				service.setValue(rec.service_id);
			}
			
			hidden = new CheckBoxFormElementDE(this);
			hidden.setLabel("Hidden Service");
			if(rec != null) {
				hidden.setValue(rec.hidden);
			}
			
			central = new CheckBoxFormElementDE(this);
			central.setLabel("Central");
			if(rec != null) {
				central.setValue(rec.central);
			}
			
			endpoint_override = new TextFormElementDE(this);
			endpoint_override.setLabel("End Point Override");
			if(rec != null) {
				endpoint_override.setValue(rec.endpoint_override);
			}

			server_list_regex = new TextFormElementDE(this);
			server_list_regex.setLabel("Server List RegEx");
			if(rec != null && rec.server_list_regex != null) {
				server_list_regex.setValue(rec.server_list_regex.toString());
			}
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.setConfirm(true, "Do you really want to remove this service?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeService(myself);	
				}
			});
		}

		public void setService(Integer value) {
			service.setValue(value);
		}
		public Integer getService() {
			return service.getValue();
		}
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		public void render(PrintWriter out) {
			out.write("<div class=\"service_editor\">");
			
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

		//caller should set resource_id
		public ResourceServiceRecord getServiceRecord() {
			ResourceServiceRecord rec = new ResourceServiceRecord();

			rec.service_id = service.getValue();
			rec.endpoint_override = endpoint_override.getValue();
			rec.hidden = hidden.getValue();
			rec.central = central.getValue();
			rec.server_list_regex = server_list_regex.getValue();
					
			return rec;
		}
	}
	
	public void removeService(ServiceEditor service)
	{
		services.remove(service);
		redraw();
	}
	
	public void addService(ResourceServiceRecord rec) { 
		
		ServiceEditor elem = new ServiceEditor(this, rec, service_recs);
		services.add(elem);
		redraw();
	}
	
	public ResourceServicesDE(DivEx parent, ArrayList<ServiceRecord> _service_recs) {
		super(parent);
		service_recs = _service_recs;
		
		add_button = new ButtonDE(this, "Add New Service");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addService(new ResourceServiceRecord());
			}
			
		});
	}

	public ArrayList<ResourceServiceRecord> getResourceServiceRecords()
	{
		ArrayList<ResourceServiceRecord> service_recs = new ArrayList<ResourceServiceRecord>();
		for(ServiceEditor service : services) {
			service_recs.add(service.getServiceRecord());
		}
		return service_recs;
	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		
		for(ServiceEditor service : services) {
			service.render(out);
		}
		add_button.render(out);
		
		out.print("</div>");
	}

}
