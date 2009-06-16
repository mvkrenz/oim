package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divrep.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.form.CheckBoxFormElement;
import com.webif.divrep.form.FormElementBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.IFormElementValidator;
import com.webif.divrep.form.validator.UrlValidator;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;

public class ResourceServices extends FormElementBase {

	ArrayList<ServiceEditor> services = new ArrayList<ServiceEditor>();
	ArrayList<Integer> service_id_taken = new ArrayList<Integer>();
	private Button add_button;
	private Context context;
	private ArrayList<ServiceRecord> service_recs;
	
	private ResourceFormDE parent;

	class ServiceEditor extends FormElementBase
	{
		//service details
		private ServiceGroupHierarchySelector service;
		private TextFormElement endpoint_override;
		private CheckBoxFormElement hidden;
		private CheckBoxFormElement central;
		private TextFormElement server_list_regex;
		
		private Button remove_button;
		private ResourceServices parent;
		
		protected ServiceEditor(ResourceServices _parent, ResourceServiceRecord rec, ArrayList<ServiceRecord> service_recs) {
			super(_parent);
			parent = _parent;
			
			try {
				service = new ServiceGroupHierarchySelector(this, context, ServiceGroupHierarchySelector.Type.SERVICE);
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			service.setLabel("Select a service group, then a service");
			service.setRequired(true);
			if(rec != null) {
				service.setValue(rec.service_id);
			}

			// TODO These lines look a bit ugly -- needs clean up -agopu
			hidden = new CheckBoxFormElement(this);
			hidden.setLabel("Is this a Hidden Service? (for eg., an internal gatekeeper inaccessible to the outside world; If you are not sure, leave it unchecked)");
			if(rec != null) {
				hidden.setValue(rec.hidden);
			}
			
			central = new CheckBoxFormElement(this);
			central.setLabel("Is this a Centralized Service? (for eg., an infrastructure service like a BDII or an accounting server? If you are not sure, leave it unchecked)");
			if(rec != null) {
				central.setValue(rec.central);
			}
			
			endpoint_override = new TextFormElement(this);
			endpoint_override.setLabel("Service URI Override (FQDN[:port])");
			endpoint_override.setSampleValue("research.iu.edu:2812");
			if(rec != null) {
				endpoint_override.setValue(rec.endpoint_override);
			}
			endpoint_override.addValidator(new IFormElementValidator<String>() {
				String message;
				public String getMessage() {
					return message;
				}
				public Boolean isValid(String value) {
					//split the value into 2 segments
					String[]parts = value.split(":");
					if(parts.length != 1 && parts.length != 2) {
						message = "Please enter override in the form such as \"resource123.iu.edu:2119\"";
						return false;
					}
					
					//validate the url
					String url = parts[0];
					if(!parent.isValidResourceFQDN(url)) {
						message = "Please use FQDN of this resource, or one of FQDN aliases.";
						return false;
					}
					
					//validate port
					if(parts.length == 2) {
						try {
							Integer port = Integer.parseInt(parts[1]);
						} catch (NumberFormatException e) {
							message = "The port number is invalid.";
							return false;
						}
					}
					
					return true;
				}});

			// Hiding this for now. Only Brian B knows how to use it.
			server_list_regex = new TextFormElement(this);
			server_list_regex.setLabel("Server List RegEx");
			if(rec != null && rec.server_list_regex != null) {
				server_list_regex.setValue(rec.server_list_regex.toString());
			}
			server_list_regex.setHidden(true);
			
			remove_button = new Button(this, "images/delete.png");
			remove_button.setStyle(Button.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this service?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeService(ServiceEditor.this);	
				}
			});
		}

		public void addServiceEventListener(EventListener listener) {
			service.addEventListener(listener);
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
			out.write("<div id=\""+getNodeID()+"\" class=\"service_editor\">");
			
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
	
	public ArrayList<ServiceEditor> getServiceEditors()
	{
		return services;
	}
	
	public void removeService(ServiceEditor service)
	{
		services.remove(service);
		redraw();
		
		//notify any listener of our action
		Event e = new Event(null, null);
		e.action = "remove";
		e.value = service;
		notifyListener(e);
	}
	
	public void addService(ResourceServiceRecord rec) { 
		ServiceEditor service = new ServiceEditor(this, rec, service_recs);
		services.add(service);
		redraw();
		
		//notify any listener of our action
		Event e = new Event(null, null);
		e.action = "add";
		e.value = service;
		notifyListener(e);
	}
	
	public ResourceServices(ResourceFormDE _parent, Context _context, ArrayList<ServiceRecord> _service_recs) {
		super(_parent);
		parent = _parent;
		context = _context;
		service_recs = _service_recs;
		
		add_button = new Button(this, "Add New Service");
		add_button.setStyle(Button.Style.ALINK);
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
	
	public void validate()
	{
		//validate all services
		redraw();
		valid = true;
		for(ServiceEditor service : services) {
			if(!service.isValid()) {
				valid = false;
			}
		}
	}
	
	public Boolean isValidResourceFQDN(String url)
	{
		return parent.isValidResourceFQDN(url);
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
