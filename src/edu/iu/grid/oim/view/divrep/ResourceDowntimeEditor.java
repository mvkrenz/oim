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
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.ResourceDowntime;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeModel;
import edu.iu.grid.oim.model.db.ResourceDowntimeServiceModel;
import edu.iu.grid.oim.model.db.ResourceServiceModel;
import edu.iu.grid.oim.model.db.ServiceModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.DowntimeClassRecord;
import edu.iu.grid.oim.model.db.record.DowntimeSeverityRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;

public class ResourceDowntimeEditor extends DivRepFormElement {
    static Logger log = Logger.getLogger(ResourceDowntimeEditor.class); 

    private Context context;
    
	private DivRepButton add_button;
	private ArrayList<ResourceDowntimeRecord> downtime_recs;
	private Authorization auth;
	private int resource_id;
	
	public class DowntimeEditor extends DivRepFormElement
	{
		//service details
		private DivRepTextArea summary;
		
		private Integer downtime_id;
		
		private DateDE start_date;
		private TimeDE start_time;
		
		private DateDE end_date;
		private TimeDE end_time;
		
		private DivRepSelectBox class_id;
		private DivRepSelectBox severity_id;
			
		private HashMap<Integer/*service_id*/, DivRepCheckBox> affected_services = new HashMap<Integer, DivRepCheckBox>();
		
		private DivRepButton remove_button;

		class DateDE extends DivRepFormElement<Date>
		{
			private static final String default_format = "M/d/yyyy";

			//http://docs.jquery.com/UI/Datepicker/formatDate
			private static final String default_jquery_format = "m/d/yy";
			
			String minDate = null;
			protected DateDE(DivRep parent) {
				super(parent);
				value = new Date();//today
			}

			public void setMinDate(Date d)
			{
				minDate = "new Date("+d.getTime()+")";
			}
			protected void onEvent(DivRepEvent e) {
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
						"dateFormat: '"+default_jquery_format+"',"+
						"beforeShow: function() {$(this).attr('disabled', 'disabled');},"+
						"onClose: function() {$(this).attr('disabled', '');},"+
						"changeYear: true,"+
						"changeMonth: true"
						);
				out.write("});});");
				out.write("</script>");
				
				error.render(out);
				out.write("</div>");
			}
		}
		class TimeDE extends DivRepFormElement<Integer>
		{

			DivRepSelectBox hour;
			DivRepSelectBox min;
			
			protected TimeDE(DivRep parent) {
				super(parent);
				
				TreeMap<Integer, String> hours = new TreeMap<Integer, String>();
				hours.put(0, "0 AM");
				hours.put(1, "1 AM");
				hours.put(2, "2 AM");
				hours.put(3, "3 AM");
				hours.put(4, "4 AM");
				hours.put(5, "5 AM");
				hours.put(6, "6 AM");
				hours.put(7, "7 AM");
				hours.put(8, "8 AM");
				hours.put(9, "9 AM");
				hours.put(10, "10 AM");
				hours.put(11, "11 AM");
				hours.put(12, "12 (Noon)");
				hours.put(13, "1 PM (13)");
				hours.put(14, "2 PM (14)");
				hours.put(15, "3 PM (15)");
				hours.put(16, "4 PM (16)");
				hours.put(17, "5 PM (17)");
				hours.put(18, "6 PM (18)");
				hours.put(19, "7 PM (19)");
				hours.put(20, "8 PM (20)");
				hours.put(21, "9 PM (21)");
				hours.put(22, "10 PM (22)");
				hours.put(23, "11 PM (23)");
				hour = new DivRepSelectBox(this, hours);
				hour.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						Integer h = Integer.valueOf((String)e.value);
						int current_min = value%60;
						value = h*60 + current_min;
					}});
				hour.setHasNull(false);
				
				TreeMap<Integer, String> mins = new TreeMap<Integer, String>();	
				for(int m = 0; m < 60; m+=5) {
					mins.put(m, ":" + m);
				}
				min = new DivRepSelectBox(this, mins);
				min.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						Integer m = Integer.valueOf((String)e.value);
						int current_hour = value/60;
						value = current_hour*60 + m;
					}});
				min.setHasNull(false);

				Date current = new Date();
				setValue(new Timestamp(current.getTime()));
			}

			public void render(PrintWriter out) {
				int value_hour = (int)value/60;
				int value_min = (int)value%60;
				
				hour.setValue(value_hour);
				min.setValue(value_min);
				
				out.write("<table id=\""+getNodeID()+"\"><tr><td>");
				hour.render(out);
				out.write("</td><td>");
				min.render(out);
				
				error.render(out);
				out.write("</td></tr></table>");
			}

			public void setValue(Timestamp time) {
				long sec = time.getTime()/1000;
				int sec_inday = (int)(sec % (3600*24));
				int mins = sec_inday / 60;
				int hours = mins / 60;
				
				//adjust it to 5 minutes increment (since we don't allow selecting sub 5 minutes)
				mins = (mins / 5) * 5;

				value = (mins%60) + hours * 60;
			}
			public Integer getHour()
			{
				return value/60;
			}
			public Integer getMin()
			{
				return value%60;
			}
		
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}
		}
		
		public DowntimeEditor(DivRep parent, ResourceDowntimeRecord rec, Authorization auth) throws SQLException {
			super(parent);
			downtime_id = rec.id;
			
			new DivRepStaticContent(this, "<h3>Duration (UTC)</h3>");
			new DivRepStaticContent(this, "<table><tr><td>");
			start_date = new DateDE(this);
			start_date.setMinDate(new Date());
			if(rec.start_time != null) {
				start_date.setValue(rec.start_time);
			}
			
			start_date.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					//DowntimeEditor.this.adjustEndTime();
					DowntimeEditor.this.validate();
				}});
			
			new DivRepStaticContent(this, "</td><td>");
			
			start_time = new TimeDE(this);
			if(rec.start_time != null) {
				start_time.setValue(rec.start_time);
			}
			start_time.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					//DowntimeEditor.this.adjustEndTime();
					DowntimeEditor.this.validate();
				}});
			new DivRepStaticContent(this, "</td><td>&nbsp;to&nbsp;</td><td>");
			
			end_date = new DateDE(this);
			end_date.setMinDate(new Date());
			if(rec.end_time != null) {
				end_date.setValue(rec.end_time);
			}
			end_date.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					//DowntimeEditor.this.adjustStartTime();
					DowntimeEditor.this.validate();
				}});
			new DivRepStaticContent(this, "</td><td>");
			
			end_time = new TimeDE(this);
			if(rec.end_time != null) {
				end_time.setValue(rec.end_time);
			}
			end_time.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					//DowntimeEditor.this.adjustStartTime();
					DowntimeEditor.this.validate();
				}});
			new DivRepStaticContent(this, "</td></tr></table>");
			
			new DivRepStaticContent(this, "<h3>Detail</h3>");
			summary = new DivRepTextArea(this);
			summary.setLabel("Downtime Summary");
			summary.setRequired(true);
			if(rec.downtime_summary != null) {
				summary.setValue(rec.downtime_summary);
			}
			summary.setWidth(600);
			summary.setHeight(200);
			
			TreeMap<Integer, String> class_kv = new TreeMap<Integer, String>();
			DowntimeClassModel dcmodel = new DowntimeClassModel(context);
			for(DowntimeClassRecord dcrec : dcmodel.getAll()) {
				class_kv.put(dcrec.id, dcrec.name);
			}
			class_id = new DivRepSelectBox(this, class_kv);
			class_id.setLabel("Class");
			class_id.setRequired(true);
			if(rec.downtime_class_id != null) {
				class_id.setValue(rec.downtime_class_id);
			}
			else {// DivRepSelectBox first element as default, we could set this to any of the choices
				if (class_kv != null) class_id.setValue(1);
			}
			
			TreeMap<Integer, String> severity_kv = new TreeMap<Integer, String>();
			DowntimeSeverityModel smodel = new DowntimeSeverityModel(context);
			for(DowntimeSeverityRecord dcrec : smodel.getAll()) {
				severity_kv.put(dcrec.id, dcrec.name);
			}
			severity_id = new DivRepSelectBox(this, severity_kv);
			severity_id.setLabel("Severity");
			severity_id.setRequired(true);
			if(rec.downtime_severity_id != null) {
				severity_id.setValue(rec.downtime_severity_id);
			}
			else { // DivRepSelectBox first element as default, we could set this to any of the choices
				if (severity_kv != null) severity_id.setValue(1);
			}
			
			new DivRepStaticContent(this, "<h3>Affected Services</h3>");
			ResourceServiceModel rsmodel = new ResourceServiceModel(context);
			Collection<ResourceServiceRecord> rsrecs = rsmodel.getAllByResourceID(resource_id);
			for(ResourceServiceRecord rsrec : rsrecs) {
				addService(rsrec.service_id);
			}
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
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
				final DivRepCheckBox elem = new DivRepCheckBox(this);
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
			DivRepCheckBox check = affected_services.get(service_id);
			affected_services.remove(service_id);
			remove(check);
			redraw();
		}
		
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div class=\"downtime_editor\" id=\""+getNodeID()+"\">");	
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");

			for(DivRep child : childnodes) {
				if(child == remove_button) continue;
				if(child == error) continue;
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
			error.render(out);
			
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

		public Timestamp getStartTime() 
		{
			return convertToTimestamp(start_date.getValue(), start_time.getHour(), start_time.getMin());
		}		
		
		public Timestamp getEndTime()
		{
			return convertToTimestamp(end_date.getValue(), end_time.getHour(), end_time.getMin());
		}
		
		private Timestamp convertToTimestamp(Date date, int hour, int min)
		{
			//Calendar cal = (Calendar)Calendar.getInstance().clone();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.set(Calendar.HOUR_OF_DAY, hour);
			cal.set(Calendar.MINUTE, min);
			cal.set(Calendar.SECOND, 0);
			return new Timestamp(cal.getTimeInMillis());				
		}
		
		public ArrayList<ResourceDowntimeServiceRecord> getAffectedServiceRecords()
		{
			ArrayList<ResourceDowntimeServiceRecord> list = new ArrayList<ResourceDowntimeServiceRecord>();
			for(Integer service_id : affected_services.keySet()) {
				DivRepCheckBox checkbox = affected_services.get(service_id);
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

		public void validate()
		{
			super.validate();

			Timestamp start = getStartTime();
			Timestamp end = getEndTime();
			if(start.compareTo(end) > 0) {
				valid = false;
				error.set("Start Time is after the end time. Please correct.");
				return;
			}
			
			int service_count = 0;
			for(Integer service_id : affected_services.keySet()) {
				DivRepCheckBox checkbox = affected_services.get(service_id);
				if(checkbox.getValue()) {
					++service_count;
				}
			}
			if(service_count == 0) {
				valid = false;
				error.set("Please select at least one affected service.");
				return;
			}
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
		
		add_button = new DivRepButton(this, "Add New Downtime");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
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
	
	protected void onEvent(DivRepEvent e) {
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
			new DivRepStaticContent(this, "<p>No existing downtimes for this resource</p>").render(out);
		}
		add_button.render(out);
		out.print("</div>");
		
	}
}
