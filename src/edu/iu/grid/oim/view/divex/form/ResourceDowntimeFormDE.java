package edu.iu.grid.oim.view.divex.form;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletException;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.ResourceDowntime;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.record.DowntimeClassRecord;
import edu.iu.grid.oim.model.db.record.DowntimeSeverityRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divex.ResourceServicesDE;

public class ResourceDowntimeFormDE extends FormDE {
    static Logger log = Logger.getLogger(ResourceDowntimeFormDE.class); 
    
	//ArrayList<DowntimeEditor> downtimes = new ArrayList<DowntimeEditor>();
	private ButtonDE add_button;
	private ArrayList<ResourceDowntimeRecord> downtime_recs;
	private Authorization auth;
	private int resource_id;
	
	private ResourceServicesDE resource_services_de;
	
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
		private CheckBoxFormElementDE disable;
			
		private HashMap<Integer/*service_id*/, CheckBoxFormElementDE> affected_services = new HashMap();
		
		private ButtonDE remove_button;
		private DowntimeEditor myself;

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
					error = "Please specify a valid date such as 4/17/2009";
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
				if(error != null) {
					out.print("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
				}
				
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
					value = format.parse("0:00 AM");
				} catch (ParseException e) {
					//shoud not happen
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
					error = "Please specify a valid time such as 0:00 AM";
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
				if(error != null) {
					out.print("<p class='elementerror round'>"+StringEscapeUtils.escapeHtml(error)+"</p>");
				}
				out.write("</div>");
			}
		}
		
		protected DowntimeEditor(DivEx parent, ResourceDowntimeRecord rec, Authorization auth) throws SQLException {
			super(parent);
			myself = this;
			downtime_id = rec.id;
			
			new StaticDE(this, "<h3>Start Time (UTC)</h3>");
			start_date = new DateDE(this);
			start_date.setLabel("Date");
			start_date.setRequired(true);
			start_date.setMinDate(new Date());
			if(rec.start_time != null) {
				start_date.setValue(rec.start_time);
			}
			start_time = new TimeDE(this);
			start_time.setLabel("Time");
			start_time.setRequired(true);
			if(rec.start_time != null) {
				start_time.setValue(rec.start_time);
			}
			
			new StaticDE(this, "<h3>End Time (UTC)</h3>");
			end_date = new DateDE(this);
			end_date.setLabel("Date");
			end_date.setRequired(true);
			end_date.setMinDate(new Date());
			if(rec.end_time != null) {
				end_date.setValue(rec.end_time);
			}
			end_time = new TimeDE(this);
			end_time.setLabel("Time");
			end_time.setRequired(true);
			if(rec.end_time != null) {
				end_time.setValue(rec.end_time);
			}
			
			new StaticDE(this, "<h3>Detail</h3>");
			summary = new TextAreaFormElementDE(this);
			summary.setLabel("Downtime Summary");
			summary.setRequired(true);
			if(rec.downtime_summary != null) {
				summary.setValue(rec.downtime_summary);
			}
			
			HashMap<Integer, String> class_kv = new HashMap();
			DowntimeClassModel dcmodel = new DowntimeClassModel(auth);
			for(DowntimeClassRecord dcrec : dcmodel.getAll()) {
				class_kv.put(dcrec.id, dcrec.name);
			}
			class_id = new SelectFormElementDE(this, class_kv);
			class_id.setLabel("Class");
			class_id.setRequired(true);
			if(rec.downtime_class_id != null) {
				class_id.setValue(rec.downtime_class_id);
			}
			
			HashMap<Integer, String> severity_kv = new HashMap();
			DowntimeSeverityModel smodel = new DowntimeSeverityModel(auth);
			for(DowntimeSeverityRecord dcrec : smodel.getAll()) {
				severity_kv.put(dcrec.id, dcrec.name);
			}
			severity_id = new SelectFormElementDE(this, severity_kv);
			severity_id.setLabel("Severity");
			severity_id.setRequired(true);
			if(rec.downtime_severity_id != null) {
				severity_id.setValue(rec.downtime_severity_id);
			}
			
			disable = new CheckBoxFormElementDE(this);
			disable.setLabel("Disable");
			if(rec.disable != null) {
				disable.setValue(rec.disable);
			}
			
			new StaticDE(this, "<h3>Affected Services</h3>");
			//affected_services = new HashMap<ServiceEditor, CheckBoxFormElementDE>();
			//ArrayList<ServiceEditor> ses = resource_services_de.getServiceEditors();
			//for(ServiceEditor se : ses) {
			ResourceServiceModel rsmodel = new ResourceServiceModel(auth);
			Collection<ResourceServiceRecord> rsrecs = rsmodel.getAllByResourceID(rec.resource_id);
			for(ResourceServiceRecord rsrec : rsrecs) {
				addService(rsrec.service_id);
			}
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.setConfirm(true, "Do you really want to remove this downtime schedule?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeDowntime(myself);	
				}
			});
			
		}
		
		public void addService(Integer service_id)
		{
			
			final ServiceModel servicemodel = new ServiceModel(auth);
			ResourceDowntimeServiceModel rdsmodel = new ResourceDowntimeServiceModel(auth);

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
			rec.disable = disable.getValue();
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

	public ResourceDowntimeFormDE(DivEx parent, Authorization _auth, String _origin_url, final Integer _resource_id) throws SQLException {
		super(parent, _origin_url);
		auth = _auth;
		resource_id = _resource_id;
		
		ResourceDowntimeModel dmodel = new ResourceDowntimeModel(auth);	
		for(ResourceDowntimeRecord drec : dmodel.getFutureDowntimesByResourceID(resource_id)) {
			addDowntime(drec);
		}
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
	
	/*
	//if new downtime is created, we don't have a way to match it back to the specific downtime (since downtime_id isn't set yet)
	//so this returns a hashmap with downtime editor associated with it as a key
	public HashMap<Integer, ArrayList<ResourceDowntimeServiceRecord>> getAffectedServiceRecords()
	{
		HashMap<Integer, ArrayList<ResourceDowntimeServiceRecord>> downtime_recs = 
			new HashMap<Integer, ArrayList<ResourceDowntimeServiceRecord>>();
		for(DivEx node : childnodes) {
			if(node instanceof DowntimeEditor) {
				DowntimeEditor downtime = (DowntimeEditor)node;
				ArrayList<ResourceDowntimeServiceRecord> a_downtime_recs = downtime.getAffectedServiceRecords();
				downtime_recs.put(downtime.downtime_id, downtime.getAffectedServiceRecords());
			}
		}
		return downtime_recs;		
	}
	*/
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	protected Boolean doSubmit() {		
		ResourceDowntimeModel model = new ResourceDowntimeModel(auth);
		try {
			model.updateDetail(resource_id, getResourceDowntimes());
		} catch (Exception e) {
			alert(e.getMessage());
			return false;
		}
		return true;
	}

}
