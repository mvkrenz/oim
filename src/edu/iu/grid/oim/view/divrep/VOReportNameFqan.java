package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.common.DivRepStaticContent;
import com.webif.divrep.common.DivRepCheckBox;
import com.webif.divrep.common.DivRepFormElement;
import com.webif.divrep.common.DivRepSelectBox;
import com.webif.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.view.divrep.VOReportNames.VOReportNameEditor;

public class VOReportNameFqan extends DivRepFormElement {

	//ArrayList<VOReportNameFqanEditor> vo_report_name_fqans = new ArrayList<VOReportNameFqanEditor>();
	private DivRepButton add_button;

	class VOReportNameFqanEditor extends DivRepFormElement
	{
		private Integer vo_report_name_id;
		private DivRepTextBox group_name;
		private DivRepTextBox role;
		private DivRepButton remove_button;
		private VOReportNameFqanEditor myself;
		
		protected VOReportNameFqanEditor(DivRep parent, VOReportNameFqanRecord vorepnamefqan_record) {
			super(parent);
			myself = this;

			vo_report_name_id  = vorepnamefqan_record.vo_report_name_id;

			new DivRepStaticContent(this, "<h3>FQAN</h3>");
			group_name = new DivRepTextBox(this);
			group_name.setLabel("Group Name");
			group_name.setRequired(true);
			group_name.setValue("FOO");
			
			role = new DivRepTextBox(this);
			role.setLabel("Role");
			role.setValue("BAR");
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
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
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"vo_report_name_fqan divrep_round\">");
			
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
		add_button = new DivRepButton(this, "Add New FQAN");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
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
	
	protected void onEvent(DivRepEvent e) {
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
