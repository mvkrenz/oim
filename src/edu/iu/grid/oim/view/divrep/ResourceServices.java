package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;
import com.divrep.i18n.Labels;
import com.divrep.validator.DivRepIValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;

public class ResourceServices extends DivRepFormElement {
	Labels lab = Labels.getInstance();
	
	ArrayList<ServiceEditor> services = new ArrayList<ServiceEditor>();
	ArrayList<Integer> service_id_taken = new ArrayList<Integer>();
	private DivRepButton add_button;
	private Context context;
	private ArrayList<ServiceRecord> service_recs;
	
	private ResourceFormDE parent;

	private boolean hasService(Integer service_id, ServiceEditor ignore) {		
		for(ServiceEditor editor : services) {
			if(editor == ignore) continue;
			if(service_id.equals(editor.getService())) return true;
		}
		return false;
	}
	
	class ServiceEditor extends DivRepFormElement
	{
		//service details
		private ServiceSelector service;
		private DivRepTextBox endpoint_override;
		private DivRepCheckBox hidden;
		private DivRepCheckBox central;
		private DivRepTextBox server_list_regex;
		
		private DivRepButton remove_button;
		private ResourceServices parent;
		
		protected ServiceEditor(ResourceServices _parent, ResourceServiceRecord rec, ArrayList<ServiceRecord> service_recs) {
			super(_parent);
			parent = _parent;
			
			service = new ServiceSelector(this, context);
			service.setLabel("Service");
			service.setRequired(true);
			service.addValidator(new DivRepIValidator<Integer>() {
				public String getErrorMessage() {
					return "This service already exists for this resource.";
				}
				public Boolean isValid(Integer id) {
					if(hasService(id, ServiceEditor.this)) {
						return false;
					}
					return true;
				}});
			if(rec != null) {
				service.setValue(rec.service_id);
			}

			// TODO These lines look a bit ugly -- needs clean up -agopu
			hidden = new DivRepCheckBox(this);
			hidden.setLabel("Is this a Hidden Service? (for eg., an internal gatekeeper inaccessible to the outside world; If you are not sure, leave it unchecked)");
			if(rec != null) {
				hidden.setValue(rec.hidden);
			}
			
			central = new DivRepCheckBox(this);
			central.setLabel("Is this a Centralized Service? (for eg., an infrastructure service like a BDII or an accounting server? If you are not sure, leave it unchecked)");
			if(rec != null) {
				central.setValue(rec.central);
			}
			
			endpoint_override = new DivRepTextBox(this);
			endpoint_override.setLabel("Service URI Override (FQDN[:port])");
			endpoint_override.setSampleValue("research.iu.edu:2812");
			if(rec != null) {
				endpoint_override.setValue(rec.endpoint_override);
			}
			endpoint_override.addValidator(new DivRepIValidator<String>() {
				String message;
				public String getErrorMessage() {
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
			server_list_regex = new DivRepTextBox(this);
			server_list_regex.setLabel("Server List RegEx");
			if(rec != null && rec.server_list_regex != null) {
				server_list_regex.setValue(rec.server_list_regex.toString());
			}
			server_list_regex.setHidden(true);
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this service?");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeService(ServiceEditor.this);	
				}
			});
		}

		public void addServiceEventListener(DivRepEventListener listener) {
			service.addEventListener(listener);
		}
		
		public void setService(Integer value) {
			service.setValue(value);
		}
		public Integer getService() {
			return service.getValue();
		}
		
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"service_editor\">");
			
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
		modified(true);
		redraw();
	}
	
	public void addService(ResourceServiceRecord rec) { 
		ServiceEditor service = new ServiceEditor(this, rec, service_recs);
		services.add(service);
		modified(true);
		redraw();
		
		/*
		//notify any listener of our action
		DivRepEvent e = new DivRepEvent(null, null);
		e.action = "add";
		e.value = service;
		notifyListener(e);
		*/
	}
	
	public ResourceServices(ResourceFormDE _parent, Context _context, ArrayList<ServiceRecord> _service_recs) {
		super(_parent);
		parent = _parent;
		context = _context;
		service_recs = _service_recs;
		
		add_button = new DivRepButton(this, "Add New Service");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
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
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub

	}
	
	public void validate()
	{
		boolean original = valid;
		
		//validate all services
		redraw();
		valid = true;
		for(ServiceEditor service : services) {
			if(!service.isValid()) {
				error.set(null);//child element should show error message
				valid = false;
			}
		}
		
		if(required && services.size() == 0) {
			error.set("Please specify at least one service.");
			valid = false;
		}
		
		//why valid == false? because sometime error message can change to something else while it's set to true
		if(original != valid || valid == false) {
			error.redraw();
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
		
		/*
		if(isRequired()) {
			//out.print(" * Required");
			out.print(lab.RequiredFieldNote());
		}
		*/
		error.render(out);
		
		out.print("</div>");
	}

}
