package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divrep.common.Button;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.common.Static;
import com.webif.divrep.common.CheckBoxFormElement;
import com.webif.divrep.common.FormElement;
import com.webif.divrep.common.Select;
import com.webif.divrep.common.Text;

import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.view.divrep.VOReportNames.VOReportNameEditor;

public class VOReportNameFqan extends FormElement {

	//ArrayList<VOReportNameFqanEditor> vo_report_name_fqans = new ArrayList<VOReportNameFqanEditor>();
	private Button add_button;

	class VOReportNameFqanEditor extends FormElement
	{
		private Integer vo_report_name_id;
		private Text group_name;
		private Text role;
		private Button remove_button;
		private VOReportNameFqanEditor myself;
		
		protected VOReportNameFqanEditor(DivRep parent, VOReportNameFqanRecord vorepnamefqan_record) {
			super(parent);
			myself = this;

			vo_report_name_id  = vorepnamefqan_record.vo_report_name_id;

			new Static(this, "<h3>FQAN</h3>");
			group_name = new Text(this);
			group_name.setLabel("Group Name");
			group_name.setRequired(true);
			group_name.setValue("FOO");
			//group_name.addClass("inline");
			
			role = new Text(this);
			role.setLabel("Role");
			role.setValue("BAR");
			//role.addClass("inline");
			
			remove_button = new Button(this, "images/delete.png");
			remove_button.setStyle(Button.Style.IMAGE);
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
	
	public VOReportNameFqan(DivRep parent) {
		super(parent);
		add_button = new Button(this, "Add New FQAN");
		add_button.setStyle(Button.Style.ALINK);
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
		for(DivRep node : childnodes) {
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
		for(DivRep node : childnodes) {
			if(node instanceof VOReportNameFqanEditor) {
				VOReportNameFqanEditor vo_report_name_fqan = (VOReportNameFqanEditor)node;
				vo_report_name_fqan.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}
}
