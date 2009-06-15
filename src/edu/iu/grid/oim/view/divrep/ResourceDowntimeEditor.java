package edu.iu.grid.oim.view.divrep;

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
import com.webif.divrep.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.Static;
import com.webif.divrep.form.CheckBoxFormElement;
import com.webif.divrep.form.FormElementBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextAreaFormElement;
import com.webif.divrep.form.validator.IFormElementValidator;

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

public class ResourceDowntimeEditor extends FormElementBase {
    static Logger log = Logger.getLogger(ResourceDowntimeEditor.class); 

    private Context context;
    
	private Button add_button;
	private ArrayList<ResourceDowntimeRecord> downtime_recs;
	private Authorization auth;
	private int resource_id;
	
	public class DowntimeEditor extends FormElementBase
	{
		//service details
		private TextAreaFormElement summary;
		
		private Integer downtime_id;
		
		private DateDE start_date;
		private TimeDE start_time;
		
		private DateDE end_date;
		private TimeDE end_time;
		
		private SelectFormElement class_id;
		private SelectFormElement severity_id;
			
		private HashMap<Integer/*service_id*/, CheckBoxFormElement> affected_services = new HashMap();
		
		private Button remove_button;

		class DateDE extends FormElementBase<Date>
		{
			private static final String default_format = "M/d/yyyy";
			
			String minDate = null;
			protected DateDE(DivRep parent) {
				super(parent);
				value = new Date();//today
			}

			public void setMinDate(Date d)
			{
				minDate = "new Date("+d.getTime()+")";
			}
			protected void onEvent(Event e) {
				SimpleDateFormat format = new SimpleDateFormat(default_format);
				try {
					value = format.parse((String)e.value);
				} catch (ParseException e1) {
					alert(e1.getMessage() + ". Please specify a valid date such as 4/17/2009");
				}
				modified(true);
				redraw();
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				if(label != null) {
					out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
				}
				
				SimpleDateFormat format = new SimpleDateFormat(default_format);
				String str = format.format(value);
				out.write("<input type=\"text\" class=\"datepicker\" value=\""+str+"\"/>");	
				
				//setup the datepicker
				out.write("<script type=\"text/javascript\">");
				out.write("$(document).ready(function() { $(\"#"+getNodeID()+" .datepicker\").datepicker({" +
						"onSelect: function(value) {divrep('"+getNodeID()+"', null, value);},"+
						"altFormat: 'm/d/yyyy'");
				out.write("});});");
				out.write("</script>");

				out.write("</div>");
			}
		}
		
		class TimeDE extends FormElementBase<Date>
		{
			private static final String default_format = "h:mm a";
			protected TimeDE(DivRep parent) {
				super(parent);
				
				SimpleDateFormat format = new SimpleDateFormat(default_format);
				try {
					value = format.parse("0:0 AM");
				} catch (ParseException e) {
					//should not happen
				}
			}

			protected void onEvent(Event e) {
				SimpleDateFormat format = new SimpleDateFormat(default_format);
				String str = (String)e.value;
				format.setLenient(true);
				try {
					value = format.parse(str);
				} catch (ParseException e1) {
					//try alternative format
					try {
						//try without space between minute and am/pm sign
						format = new SimpleDateFormat("h:mma");
						value = format.parse(str);		
					} catch(ParseException e2) {
						try {
							//try 24 hour format
							format = new SimpleDateFormat("H:mm");
							value = format.parse(str);
						} catch(ParseException e3) {
							alert(e3.getMessage() + ". Please enter a valid time (example: 2:30 PM)");
						}
					}
				}
				redraw();
				modified(true);
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				if(label != null) {
					out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
				}
				
				SimpleDateFormat format = new SimpleDateFormat("h:mm a");
				String str = format.format(value);
				out.write("<input type=\"text\" class=\"timepickr\" onchange=\"divrep('"+getNodeID()+"', event, this.value);\" value=\""+str+"\"/>");	
				/*
				//setup the timepickr
				out.write("<script type=\"text/javascript\">");
				out.write("$(document).ready(function() { $(\"#"+getNodeID()+" .timepickr\").timepickr({" +
						"onSelect: function(value) {divrep('"+getNodeID()+"', null, value);},"+
						"convention: 12,"+
						"updateLive: false");
				out.write("});});");
				out.write("</script>");
				*/
				out.write("</div>");
			}
		}
		/*
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
			}
		}
		*/
		public DowntimeEditor(DivRep parent, ResourceDowntimeRecord rec, Authorization auth) throws SQLException {
			super(parent);
			downtime_id = rec.id;
			
			new Static(this, "<h3>Duration (UTC)</h3>");
			new Static(this, "<table><tr><td>");
			start_date = new DateDE(this);
			start_date.setMinDate(new Date());
			if(rec.start_time != null) {
				start_date.setValue(rec.start_time);
			}
			start_date.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					DowntimeEditor.this.adjustEndTime();
				}});

			new Static(this, "</td><td>");
			
			start_time = new TimeDE(this);
			if(rec.start_time != null) {
				start_time.setValue(rec.start_time);
			}
			start_time.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					DowntimeEditor.this.adjustEndTime();
				}});
			
			new Static(this, "</td><td>&nbsp;to&nbsp;</td><td>");
			
			end_date = new DateDE(this);
			end_date.setMinDate(new Date());
			if(rec.end_time != null) {
				end_date.setValue(rec.end_time);
			}
			end_date.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					DowntimeEditor.this.adjustStartTime();
				}});
			
			new Static(this, "</td><td>");
			
			end_time = new TimeDE(this);
			if(rec.end_time != null) {
				end_time.setValue(rec.end_time);
			}
			end_time.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					DowntimeEditor.this.adjustStartTime();
				}});
			
			new Static(this, "</td></tr></table>");
			
			new Static(this, "<h3>Detail</h3>");
			summary = new TextAreaFormElement(this);
			summary.setLabel("Downtime Summary");
			summary.setRequired(true);
			if(rec.downtime_summary != null) {
				summary.setValue(rec.downtime_summary);
			}
			summary.setWidth(600);
			summary.setHeight(200);
			
			HashMap<Integer, String> class_kv = new HashMap();
			DowntimeClassModel dcmodel = new DowntimeClassModel(context);
			for(DowntimeClassRecord dcrec : dcmodel.getAll()) {
				class_kv.put(dcrec.id, dcrec.name);
			}
			class_id = new SelectFormElement(this, class_kv);
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
			severity_id = new SelectFormElement(this, severity_kv);
			severity_id.setLabel("Severity");
			severity_id.setRequired(true);
			if(rec.downtime_severity_id != null) {
				severity_id.setValue(rec.downtime_severity_id);
			}
			else { // Select first element as default, we could set this to any of the choices
				if (severity_kv != null) severity_id.setValue(1);
			}
			
			new Static(this, "<h3>Affected Services</h3>");
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			Collection<ResourceServiceRecord> rsrecs = rsmodel.getAllByResourceID(resource_id);
			for(ResourceServiceRecord rsrec : rsrecs) {
				addService(rsrec.service_id);
			}
			
			remove_button = new Button(this, "images/delete.png");
			remove_button.setStyle(Button.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this downtime schedule?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeDowntime(DowntimeEditor.this);	
					modified(true);
				}
			});
		}
		
		public void addService(Integer service_id)
		{
			
			final ServiceModel servicemodel = new ServiceModel(context);
			ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(context);

			try {
				final CheckBoxFormElement elem = new CheckBoxFormElement(this);
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
			CheckBoxFormElement check = affected_services.get(service_id);
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
		public ResourceDowntimeRecord getDowntimeRecord() {
			ResourceDowntimeRecord rec = new ResourceDowntimeRecord();

			rec.id = downtime_id;
			rec.resource_id = resource_id;
			rec.downtime_summary = summary.getValue();
			rec.start_time = getStartTime();
			rec.end_time = getEndTime();
			rec.downtime_class_id = class_id.getValue();
			rec.downtime_severity_id = severity_id.getValue();
			rec.dn_id = auth.getDNID();

			return rec;
		}
		public void adjustStartTime()
		{
			Timestamp start = getStartTime();
			Timestamp end = getEndTime();
			if(start.compareTo(end) > 0) {
				//alert("You have selected the start time which is before the current end time. Adjusting start time..");
				start_date.setValue(end_date.getValue());
				start_date.redraw();
				start_time.setValue(end_time.getValue());
				start_time.redraw();
			}
		}
		public void adjustEndTime()
		{
			Timestamp start = getStartTime();
			Timestamp end = getEndTime();
			if(start.compareTo(end) > 0) {
				//alert("You have selected the end time which is after the current start time. Adjusting end time..");
				end_date.setValue(start_date.getValue());
				end_date.redraw();
				end_time.setValue(start_time.getValue());
				end_time.redraw();
			}
		}
		public Timestamp getStartTime() 
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(start_date.getValue());
			cal.set(Calendar.HOUR, start_time.getValue().getHours());
			cal.set(Calendar.MINUTE, start_time.getValue().getMinutes());
			return new Timestamp(cal.getTimeInMillis());		
		}
		
		public Timestamp getEndTime()
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(end_date.getValue());
			cal.set(Calendar.HOUR, end_time.getValue().getHours());
			cal.set(Calendar.MINUTE, end_time.getValue().getMinutes());		
			return new Timestamp(cal.getTimeInMillis());
		}
		
		public ArrayList<ResourceDowntimeServiceRecord> getAffectedServiceRecords()
		{
			ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList();
			for(Integer service_id : affected_services.keySet()) {
				CheckBoxFormElement checkbox = affected_services.get(service_id);
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

	public ResourceDowntimeEditor(DivRep parent, Context _context, final Integer _resource_id) throws SQLException {
		super(parent);
		context = _context;
		auth = context.getAuthorization();
		resource_id = _resource_id;
		
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(context);	
		Collection <ResourceDowntimeRecord> dt_records = dmodel.getFutureDowntimesByResourceID(resource_id);
		for(ResourceDowntimeRecord drec : dt_records) {
			addDowntime(drec);
		}
		
		add_button = new Button(this, "Add New Downtime");
		add_button.setStyle(Button.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				try {
					addDowntime(new ResourceDowntimeRecord());
					modified(true);
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
		});
	}

	public ArrayList<ResourceDowntime> getResourceDowntimes()
	{
		ArrayList<ResourceDowntime> downtimes = new ArrayList<ResourceDowntime>();
		for(DivRep node : childnodes) {
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

		for(DivRep node : childnodes) {
			if(node instanceof DowntimeEditor) {
				count++;
				node.render(out);
			}
		}
		// Adding some clear text to make it look less odd. Is there a cleaner way to do this? -agopu
		if (count == 0) {
			new Static(this, "<p>No existing downtimes for this resource</p>").render(out);
		}
		add_button.render(out);
		out.print("</div>");
		
	}
}
