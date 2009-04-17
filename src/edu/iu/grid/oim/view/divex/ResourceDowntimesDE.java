package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.DowntimeClassModel;
import edu.iu.grid.oim.model.db.DowntimeSeverityModel;
import edu.iu.grid.oim.model.db.record.DowntimeClassRecord;
import edu.iu.grid.oim.model.db.record.DowntimeSeverityRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.view.divex.form.ResourceFormDE;

public class ResourceDowntimesDE extends FormElementDEBase {
    static Logger log = Logger.getLogger(ResourceDowntimesDE.class); 
    
	ArrayList<DowntimeEditor> downtimes = new ArrayList<DowntimeEditor>();
	private ButtonDE add_button;
	private ArrayList<ResourceDowntimeRecord> downtime_recs;
	private Authorization auth;

	class DowntimeEditor extends FormElementDEBase
	{
		//service details
		private TextAreaFormElementDE summary;
		
		private DateDE start_date;
		private TimeDE start_time;
		
		private DateDE end_date;
		private TimeDE end_time;
		
		private SelectFormElementDE class_id;
		private SelectFormElementDE severity_id;
		private CheckBoxFormElementDE disable;
		
		private ButtonDE remove_button;
		private DowntimeEditor myself;
		
		class DateDE extends FormElementDEBase<Date>
		{
			protected DateDE(DivEx parent) {
				super(parent);
			}

			protected void onEvent(Event e) {
				SimpleDateFormat format = new SimpleDateFormat("M/d/y");
				try {
					value = format.parse(e.getValue());
					myself.validate();
				} catch (ParseException e1) {
					redraw();
					error = "Please specify a valid date such as 4/17/2009";
					setValid(false);
				}
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				
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
						"altFormat: 'm/d/yy'"+
						"});});");
				out.write("</script>");

				out.write("</div>");
			}
		}
		
		class TimeDE extends FormElementDEBase<Date>
		{
			protected TimeDE(DivEx parent) {
				super(parent);
			}

			protected void onEvent(Event e) {
				SimpleDateFormat format = new SimpleDateFormat("K:m a");
				try {
					value = format.parse(e.getValue());
					myself.validate();
				} catch (ParseException e1) {
					redraw();
					error = "Please specify a valid time such as 0:00 AM";
					setValid(false);
				}
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
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
			
			summary = new TextAreaFormElementDE(this);
			summary.setLabel("Downtime Summary");
			summary.setRequired(true);
			if(rec != null) {
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
			if(rec != null) {
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
			if(rec != null) {
				severity_id.setValue(rec.downtime_severity_id);
			}
			
			new StaticDE(this, "<h2>Details</h2>");
			start_date = new DateDE(this);
			start_date.setLabel("Date");
			start_date.setRequired(true);
			if(rec != null) {
				start_date.setValue(rec.start_time);
			}
			start_time = new TimeDE(this);
			start_time.setLabel("Time");
			start_time.setRequired(true);
			if(rec != null) {
				start_time.setValue(rec.start_time);
			}
			
			end_date = new DateDE(this);
			end_date.setLabel("Date");
			end_date.setRequired(true);
			if(rec != null) {
				end_date.setValue(rec.end_time);
			}
			end_time = new TimeDE(this);
			end_time.setLabel("Time");
			end_time.setRequired(true);
			if(rec != null) {
				end_time.setValue(rec.end_time);
			}
			
			disable = new CheckBoxFormElementDE(this);
			disable.setLabel("Disable");
			if(rec != null) {
				disable.setValue(rec.disable);
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
		
		public void validate()
		{
			redraw();
			valid = true;
			
			//validate *all* elements
			for(DivEx child : childnodes) {
				if(child instanceof FormElementDEBase) { 
					FormElementDEBase element = (FormElementDEBase)child;
					if(element != null && !element.isHidden()) {
						if(!element.isValid()) {
							valid = false;
						}
					}
				}
			}
		}
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div class=\"downtime_editor\">");
			
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

			rec.downtime_summary = summary.getValue();
			rec.start_time.setTime(start_time.getValue().getTime());
			rec.end_time.setTime(end_time.getValue().getTime());
			rec.downtime_class_id = class_id.getValue();
			rec.downtime_severity_id = severity_id.getValue();
			rec.disable = disable.getValue();

			return rec;
		}
	}
	
	public void removeDowntime(DowntimeEditor downtime)
	{
		downtimes.remove(downtime);
		redraw();
	}
	
	public void addDowntime(ResourceDowntimeRecord rec) throws SQLException { 
		
		DowntimeEditor elem = new DowntimeEditor(this, rec, auth);
		downtimes.add(elem);
		redraw();
	}
	
	public ResourceDowntimesDE(DivEx parent, Authorization _auth) {
		super(parent);
		auth = _auth;
		add_button = new ButtonDE(this, "Add New Downtime");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				try {
					addDowntime(null);
				} catch (SQLException e1) {
					log.error(e1);
				}
			}
		});
	}

	public ArrayList<ResourceDowntimeRecord> getResourceDowntimeRecords()
	{
		ArrayList<ResourceDowntimeRecord> downtime_recs = new ArrayList<ResourceDowntimeRecord>();
		for(DowntimeEditor downtime : downtimes) {
			downtime_recs.add(downtime.getDowntimeRecord());
		}
		return downtime_recs;
	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}
	
	public void validate()
	{
		redraw();
		valid = true;
		
		for(DowntimeEditor downtime : downtimes) {
			if(!downtime.isValid()) {
				valid = false;
			}
		}
	}
	
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		
		for(DowntimeEditor downtime : downtimes) {
			downtime.render(out);
		}
		add_button.render(out);
		
		out.print("</div>");
	}

}
