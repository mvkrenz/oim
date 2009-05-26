package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.validator.IFormElementValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.ResourceDowntime;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.DowntimeClassRecord;
import edu.iu.grid.oim.model.db.record.DowntimeSeverityRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ResourceDowntimeEditorDE extends FormElementDEBase {
    static Logger log = Logger.getLogger(ResourceDowntimeEditorDE.class); 

    private Context context;
    
	private ButtonDE add_button;
	private ArrayList<ResourceDowntimeRecord> downtime_recs;
	private Authorization auth;
	private int resource_id;
	
	public class DowntimeEditor extends FormElementDEBase
	{
		//service details
		private TextAreaFormElementDE summary;
		
		private Integer downtime_id;
		
		private DateDE start_date;
		private TimeDE start_time;
		
		private DateDE end_date;
		private TimeDE end_time;
		
		private SelectFormElementDE class_id;
		private SelectFormElementDE severity_id;
			
		private HashMap<Integer/*service_id*/, CheckBoxFormElementDE> affected_services = new HashMap();
		
		private ButtonDE remove_button;

		class DateDE extends FormElementDEBase<Date>
		{
			String minDate = null;
			protected DateDE(DivEx parent) {
				super(parent);
			}

			public void setMinDate(Date d)
			{
				minDate = "new Date("+d.getTime()+")";
			}
			protected void onEvent(Event e) {
				SimpleDateFormat format = new SimpleDateFormat("M/d/y");
				try {
					value = format.parse((String)e.value);
					validate();
				} catch (ParseException e1) {
					redraw();
					error.set("Please specify a valid date such as 4/17/2009");
					valid = false;
				}
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				if(label != null) {
					out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
				}
				
				String str = "";
				if(value != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(value);
					str = cal.get(Calendar.MONTH)+1 + "/" + 
						cal.get(Calendar.DAY_OF_MONTH) + "/" + 
						cal.get(Calendar.YEAR);
				}
				out.write("<input type=\"text\" class=\"datepicker\" value=\""+str+"\"/>");	
				if(isRequired()) {
					out.print(" * Required");
				}
				error.render(out);
				
				//setup the datepicker
				out.write("<script type=\"text/javascript\">");
				out.write("$(document).ready(function() { $(\"#"+getNodeID()+" .datepicker\").datepicker({" +
						"onSelect: function(value) {divex('"+getNodeID()+"', null, value);},"+
						"altFormat: 'm/d/yy'");
				if(minDate != null) {
					out.write(", minDate : " + minDate);
				}
				out.write("});});");
				out.write("</script>");

				out.write("</div>");
			}
		}
		
		class TimeDE extends FormElementDEBase<Date>
		{
			protected TimeDE(DivEx parent) {
				super(parent);
				
				SimpleDateFormat format = new SimpleDateFormat("KK:mm aa");
				try {
					value = format.parse("12:00 PM");
				} catch (ParseException e) {
					//should not happen
				}
			}

			protected void onEvent(Event e) {
				SimpleDateFormat format = new SimpleDateFormat("KK:mm aa");
				try {
					String str = (String)e.value;
					value = format.parse(str);
					validate();
				} catch (ParseException e1) {
					redraw();
					error.set("Please specify a valid time (example: 2:30 PM)");
					valid = false;
				}
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				if(label != null) {
					out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
				}
				
				String str;
				if(value != null) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(value);
					str = cal.get(Calendar.HOUR) + ":" + cal.get(Calendar.MINUTE) + " ";
					if(cal.get(Calendar.AM_PM) == 0) {
						str += "AM";
					} else {
						str += "PM";
					}
				} else {
					str = "";
				}
				out.write("<input type=\"text\" onchange=\"divex('"+getNodeID()+"', event, this.value);\" value=\""+str+"\"/>");	
				if(isRequired()) {
					out.print(" * Required");
				}
				error.render(out);
				out.write("</div>");
			}
		}
		
		public void validate()
		{
			//first, validate individual elements
			super.validate();
			
			//check the date range
			if(start_date.getValue() != null && end_date.getValue() != null) {
				//check the data range
				GregorianCalendar start = new GregorianCalendar();
				start.set(Calendar.MILLISECOND, (int) start_date.getValue().getTime());
				start.set(Calendar.HOUR, start_time.getValue().getHours());
				start.set(Calendar.MINUTE, start_time.getValue().getMinutes());
				
				GregorianCalendar end = new GregorianCalendar();
				end.set(Calendar.MILLISECOND, (int) end_date.getValue().getTime());
				end.set(Calendar.HOUR, end_time.getValue().getHours());
				end.set(Calendar.MINUTE, end_time.getValue().getMinutes());
				/*
				if(start.getTimeInMillis() >= end.getTimeInMillis()) {
					error.set("End date/time value needs to be later than the start date/time. Also, earliest start date/time allowed is today 0:00 AM.");
					valid = false;
				}
				*/				
			}
		}
		
		protected DowntimeEditor(DivEx parent, ResourceDowntimeRecord rec, Authorization auth) throws SQLException {
			super(parent);
			downtime_id = rec.id;
			
			new StaticDE(this, "<h3>Start Time</h3>");
			start_date = new DateDE(this);
			start_date.setLabel("Date");
			start_date.setRequired(true);
			start_date.setMinDate(new Date());
			if(rec.start_time != null) {
				start_date.setValue(rec.start_time);
			}
			start_date.addEventListener(new EventListener() {
				public void handleEvent(Event e) {validate();}});
			
			start_time = new TimeDE(this);
			start_time.setLabel("Time (UTC)");
			start_time.setRequired(true);
			if(rec.start_time != null) {
				start_time.setValue(rec.start_time);
			}
			start_time.addEventListener(new EventListener() {
				public void handleEvent(Event e) {validate();}});
			
			new StaticDE(this, "<h3>End Time</h3>");
			end_date = new DateDE(this);
			end_date.setLabel("Date");
			end_date.setRequired(true);
			end_date.setMinDate(new Date());
			if(rec.end_time != null) {
				end_date.setValue(rec.end_time);
			}
			end_date.addEventListener(new EventListener() {
				public void handleEvent(Event e) {validate();}});
			
			end_time = new TimeDE(this);
			end_time.setLabel("Time (UTC)");
			end_time.setRequired(true);
			if(rec.end_time != null) {
				end_time.setValue(rec.end_time);
			}
			end_time.addEventListener(new EventListener() {
				public void handleEvent(Event e) {validate();}});
			
			new StaticDE(this, "<h3>Detail</h3>");
			summary = new TextAreaFormElementDE(this);
			summary.setLabel("Downtime Summary");
			summary.setRequired(true);
			if(rec.downtime_summary != null) {
				summary.setValue(rec.downtime_summary);
			}
			
			HashMap<Integer, String> class_kv = new HashMap();
			DowntimeClassModel dcmodel = new DowntimeClassModel(context);
			for(DowntimeClassRecord dcrec : dcmodel.getAll()) {
				class_kv.put(dcrec.id, dcrec.name);
			}
			class_id = new SelectFormElementDE(this, class_kv);
			class_id.setLabel("Class");
			class_id.setRequired(true);
			if(rec.downtime_class_id != null) {
				class_id.setValue(rec.downtime_class_id);
			}
			else {// Select first element as default, we could set this to any of the choices
				if (class_kv != null) class_id.setValue(1);
			}
			
			
			HashMap<Integer, String> severity_kv = new HashMap();
			DowntimeSeverityModel smodel = new DowntimeSeverityModel(context);
			for(DowntimeSeverityRecord dcrec : smodel.getAll()) {
				severity_kv.put(dcrec.id, dcrec.name);
			}
			severity_id = new SelectFormElementDE(this, severity_kv);
			severity_id.setLabel("Severity");
			severity_id.setRequired(true);
			if(rec.downtime_severity_id != null) {
				severity_id.setValue(rec.downtime_severity_id);
			}
			else { // Select first element as default, we could set this to any of the choices
				if (severity_kv != null) severity_id.setValue(1);
			}
			
			new StaticDE(this, "<h3>Affected Services</h3>");
			//affected_services = new HashMap<ServiceEditor, CheckBoxFormElementDE>();
			//ArrayList<ServiceEditor> ses = resource_services_de.getServiceEditors();
			//for(ServiceEditor se : ses) {
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			Collection<ResourceServiceRecord> rsrecs = rsmodel.getAllByResourceID(resource_id);
			for(ResourceServiceRecord rsrec : rsrecs) {
				addService(rsrec.service_id);
			}
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.setConfirm(true, "Do you really want to remove this downtime schedule?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeDowntime(DowntimeEditor.this);	
				}
			});
		}
		
		public void addService(Integer service_id)
		{
			
			final ServiceModel servicemodel = new ServiceModel(context);
			ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(context);

			try {
				final CheckBoxFormElementDE elem = new CheckBoxFormElementDE(this);
				if(service_id != null) {
					ServiceRecord srec = servicemodel.get(service_id);
					elem.setLabel(srec.name);
				} else {
					elem.setLabel("(Service Name Not Yet Selected)");
				}
				affected_services.put(service_id, elem);
				ResourceDowntimeServiceRecord keyrec = new ResourceDowntimeServiceRecord();
				keyrec.resource_downtime_id = downtime_id;
				keyrec.service_id = service_id;
				if(rdsmodel.get(keyrec) != null) {
					elem.setValue(true);
				}
				// If this is a new add, then by default have all services selected. Proves to be less error prone! -agopu
				if (downtime_id == null) {
					elem.setValue(true);
				}
				redraw();
			} catch(SQLException e) {
				log.error(e);
			}
		}
		public void removeService(Integer service_id)
		{
			CheckBoxFormElementDE check = affected_services.get(service_id);
			affected_services.remove(service_id);
			remove(check);
			redraw();
		}
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div class=\"downtime_editor\" id=\""+getNodeID()+"\">");	
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
		public ResourceDowntimeRecord getDowntimeRecord() {
			ResourceDowntimeRecord rec = new ResourceDowntimeRecord();

			rec.id = downtime_id;
			rec.resource_id = resource_id;
			rec.downtime_summary = summary.getValue();
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(start_date.getValue());
			cal.set(Calendar.HOUR, start_time.getValue().getHours());
			cal.set(Calendar.MINUTE, start_time.getValue().getMinutes());
			rec.start_time = new Timestamp(cal.getTimeInMillis());
			
			cal.setTime(end_date.getValue());
			cal.set(Calendar.HOUR, end_time.getValue().getHours());
			cal.set(Calendar.MINUTE, end_time.getValue().getMinutes());		
			rec.end_time = new Timestamp(cal.getTimeInMillis());
			
			rec.downtime_class_id = class_id.getValue();
			rec.downtime_severity_id = severity_id.getValue();
			rec.dn_id = auth.getDNID();

			return rec;
		}
		
		public ArrayList<ResourceDowntimeServiceRecord> getAffectedServiceRecords()
		{
			ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList();
			for(Integer service_id : affected_services.keySet()) {
				CheckBoxFormElementDE checkbox = affected_services.get(service_id);
				if(checkbox.getValue()) {
					ResourceDowntimeServiceRecord rec = new ResourceDowntimeServiceRecord();
					rec.resource_downtime_id = downtime_id;
					rec.service_id = service_id;
					list.add(rec);
				}	
			}
			
			return list;
		}
		
		public ResourceDowntime getResourceDowntime()
		{
			ResourceDowntime downtime = new ResourceDowntime();
			downtime.downtime = getDowntimeRecord();
			downtime.services = getAffectedServiceRecords();
			return downtime;
		}
	}

	public void removeDowntime(DowntimeEditor downtime)
	{
		remove(downtime);
		redraw();
	}
	
	public DowntimeEditor addDowntime(ResourceDowntimeRecord rec) throws SQLException { 
		DowntimeEditor elem = new DowntimeEditor(this, rec, auth);
		redraw();
		return elem;
	}

	public ResourceDowntimeEditorDE(DivEx parent, Context _context, final Integer _resource_id) throws SQLException {
		super(parent);
		context = _context;
		auth = context.getAuthorization();
		resource_id = _resource_id;
		
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);	
		Collection <ResourceDowntimeRecord> dt_records = dmodel.getFutureDowntimesByResourceID(resource_id);
		for(ResourceDowntimeRecord drec : dt_records) {
			addDowntime(drec);
		}
		
		add_button = new ButtonDE(this, "Add New Downtime");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				try {
					addDowntime(new ResourceDowntimeRecord());
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
		});
	}

	public ArrayList<ResourceDowntime> getResourceDowntimes()
	{
		ArrayList<ResourceDowntime> downtimes = new ArrayList<ResourceDowntime>();
		for(DivEx node : childnodes) {
			if(node instanceof DowntimeEditor) {
				DowntimeEditor downtime = (DowntimeEditor)node;
				downtimes.add(downtime.getResourceDowntime());
			}
		}
		return downtimes;
	}
	
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		int count = 0;
		
		out.print("<div id=\""+getNodeID()+"\">");

		for(DivEx node : childnodes) {
			if(node instanceof DowntimeEditor) {
				count++;
				node.render(out);
			}
		}
		// Adding some clear text to make it look less odd. Is there a cleaner way to do this? -agopu
		if (count == 0) {
			new StaticDE(this, "<p>No existing downtimes for this resource - click the \"Add\" link below to schedule a maintenance schedule. </p> <p>Once you have entered downtimes you would like to schedule do not forget to click on the <strong>Submit</strong> button!</p>").render(out);
		}
		add_button.render(out);
		out.print("</div>");
		
	}
}
