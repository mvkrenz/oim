package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.i18n.Labels;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.ResourceServiceDetailRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divrep.form.ResourceFormDE;
import edu.iu.grid.oim.view.divrep.form.servicedetails.ServiceDetails;

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
		private ServiceDetails details;
		
		private DivRepButton remove_button;
		private ResourceServices parent;
		
		protected ServiceEditor(ResourceServices _parent, ResourceServiceRecord rec, 
				final ArrayList<ResourceServiceDetailRecord> details_recs) {
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
				}
			});
			service.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					details.setService(service.getValue(), details_recs);
				}
			});
			
			details = new ServiceDetails(context, this);
			if(rec != null) {
				service.setValue(rec.service_id);
				details.setService(rec.service_id, details_recs);
			}
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this service?");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeService(ServiceEditor.this);	
					modified(true);
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

			return rec;
		}
		
		public ArrayList<ResourceServiceDetailRecord> getServiceDetailsRecords() {
			return details.getServiceDetailsRecords();
		}
	}
	
	public ArrayList<ServiceEditor> getServiceEditors()
	{
		return services;
	}
	
	public void removeService(ServiceEditor service)
	{
		services.remove(service);
		//modified(true);
		redraw();
	}
	
	public void addService(ResourceServiceRecord rec, ArrayList<ResourceServiceDetailRecord> details_recs) { 
		ServiceEditor service = new ServiceEditor(this, rec, details_recs);
		services.add(service);
		//modified(true);
		redraw();
		
		/*
		//notify any listener of our action
		DivRepEvent e = new DivRepEvent(null, null);
		e.action = "add";
		e.value = service;
		notifyListener(e);
		*/
	}
	
	public ResourceServices(ResourceFormDE _parent, Context _context/*, ArrayList<ServiceRecord> _service_recs*/) {
		super(_parent);
		parent = _parent;
		context = _context;
		//service_recs = _service_recs;
		
		add_button = new DivRepButton(this, "Add New Service");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addService(new ResourceServiceRecord(), null);
				modified(true);
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
	public ArrayList<ResourceServiceDetailRecord> getResourceServiceDetailsRecords()
	{
		ArrayList<ResourceServiceDetailRecord> details_recs = new ArrayList<ResourceServiceDetailRecord>();
		for(ServiceEditor service : services) {
			details_recs.addAll(service.getServiceDetailsRecords());
		}
		return details_recs;
	}


	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub

	}
	
	public boolean validate()
	{
		error.redraw();
		boolean valid = true;
		
		//validate each services
		for(ServiceEditor service : services) {
			if(!service.validate()) {
				error.set(null);//child element should show error message
				valid = false;
			}
		}
		
		if(valid) {
			if(isRequired() && services.size() == 0) {
				error.set("Please specify at least one service.");
				valid = false;
			}		
		}
		
		setValid(valid);
		return valid;
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
