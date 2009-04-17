package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE.ContactDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class VOReportNameDE_OLD extends FormElementDEBase {

	ArrayList<VOReportNameEditor> vo_report_names = new ArrayList<VOReportNameEditor>();
	private ButtonDE add_button;

	class VOReportNameEditor extends FormElementDEBase
	{
		private TextFormElementDE text;
		private ButtonDE remove_button;
		private VOReportNameEditor myself;
		
		protected VOReportNameEditor(DivEx parent) {
			super(parent);
			myself = this;
			
			text = new TextFormElementDE(this);
			text.addClass("inline");
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeVOReportName(myself);
					// Need to print warning about removing related FQANs and contacts or remove this?
				}
			});
		}

		public void setValue(String value) {
			text.setValue(value);
		}
		public String getValue() {
			return text.getValue();
		}


		@Override
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div class=\"vo_report_name\">");
			text.render(out);
			remove_button.render(out);
			out.write("</div>");
		}
	}
	
	public void removeVOReportName(VOReportNameEditor vo_report_name)
	{
		vo_report_names.remove(vo_report_name);
		redraw();
	}
	
	public void addVOReportName(String vo_report_name) { 
		VOReportNameEditor elem = new VOReportNameEditor(this);
		elem.setValue(vo_report_name);
		vo_report_names.add(elem);
		redraw();
	}
	
	public VOReportNameDE_OLD(DivEx parent) {
		super(parent);
		add_button = new ButtonDE(this, "Add New VO Report Name");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addVOReportName("");
			}
			
		});
	}

	//Note: caller need to set the id for each record ?
	public ArrayList<String> getVOReportNames()
	{
		ArrayList<String> records = new ArrayList<String>();
		for(VOReportNameEditor vo_report_name : vo_report_names) {
			String str = vo_report_name.getValue();
			if(str.length() > 0) {
				records.add(str);
			}
		}
		return records;
	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(VOReportNameEditor vo_report_name : vo_report_names) {
			vo_report_name.render(out);
		}
		add_button.render(out);
		out.print("</div>");
	}
}
