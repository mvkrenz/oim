package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.model.db.record.MetricServiceRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.view.divex.MetricServiceDE.MetricEditor;
import edu.iu.grid.oim.view.divex.ResourceServicesDE.ServiceEditor;
import edu.iu.grid.oim.view.divex.VOReportNamesDE.VOReportNameEditor;

public class VOReportNameFqanDE extends FormElementDEBase {

	//ArrayList<VOReportNameFqanEditor> vo_report_name_fqans = new ArrayList<VOReportNameFqanEditor>();
	private ButtonDE add_button;

	class VOReportNameFqanEditor extends FormElementDEBase
	{
		private Integer vo_report_name_id;
		private TextFormElementDE group_name;
		private TextFormElementDE role;
		private ButtonDE remove_button;
		private VOReportNameFqanEditor myself;
		
		protected VOReportNameFqanEditor(DivEx parent, VOReportNameFqanRecord vorepnamefqan_record) {
			super(parent);
			myself = this;

			vo_report_name_id  = vorepnamefqan_record.vo_report_name_id;

			new StaticDE(this, "<h3>FQAN</h3>");
			group_name = new TextFormElementDE(this);
			group_name.setLabel("Group Name");
			group_name.setRequired(true);
			group_name.setValue("FOO");
			//group_name.addClass("inline");
			
			role = new TextFormElementDE(this);
			role.setLabel("Role");
			role.setValue("BAR");
			//role.addClass("inline");
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					// Need to print warning about removing related FQANs and contacts or remove this?
					removeVOReportNameFqan(myself);
				}
			});
		}

		public void setGroupName(String _group_name) {
			group_name.setValue(_group_name);
		}
		public String getGroupName() {
			return group_name.getValue();
		}

		public void setRole(String _role) {
			role.setValue(_role);
		}
		public String getRole() {
			return role.getValue();
		}
		public VOReportNameFqanRecord getVOReportNameFqanRecord() {
			VOReportNameFqanRecord record = new VOReportNameFqanRecord();
			record.vo_report_name_id = vo_report_name_id;
			record.group_name        = group_name.getValue();
			record.role              = role.getValue();
			return record;
		}

		@Override
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"vo_report_name_fqan round\">");
			
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");
			
			group_name.render(out);
			role.render(out);
			out.write("</div>");
		}
	}
	
	public void removeVOReportNameFqan(VOReportNameFqanEditor vo_report_name_fqan)
	{
		remove(vo_report_name_fqan);
		redraw();
	}
	
	public void addVOReportNameFqan(VOReportNameFqanRecord record) { 
		VOReportNameFqanEditor elem = new VOReportNameFqanEditor(this, record);
		elem.setGroupName(record.group_name);
		elem.setRole(record.role);
		redraw();
	}
	
	public VOReportNameFqanDE(DivEx parent) {
		super(parent);
		add_button = new ButtonDE(this, "Add New FQAN for this VO Report Name");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addVOReportNameFqan(new VOReportNameFqanRecord());
			}
		});
		//add_button.addClass("indent");
	}

	public ArrayList<VOReportNameFqanRecord> getVOReportNameFqanRecords()
	{
		ArrayList<VOReportNameFqanRecord> records = new ArrayList<VOReportNameFqanRecord>();
		for(DivEx node : childnodes) {
			if(node instanceof VOReportNameFqanEditor) {
				VOReportNameFqanEditor editor = (VOReportNameFqanEditor)node;
				records.add(editor.getVOReportNameFqanRecord());
			}
		}
		return records;
	}
	
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(DivEx node : childnodes) {
			if(node instanceof VOReportNameFqanEditor) {
				VOReportNameFqanEditor vo_report_name_fqan = (VOReportNameFqanEditor)node;
				vo_report_name_fqan.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}
}
