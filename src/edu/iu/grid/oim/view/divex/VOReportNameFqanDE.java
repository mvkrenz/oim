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

import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;

public class VOReportNameFqanDE extends FormElementDEBase {

	ArrayList<VOReportNameFqanEditor> vo_report_name_fqans = new ArrayList<VOReportNameFqanEditor>();
	private ButtonDE add_button;

	class VOReportNameFqanEditor extends FormElementDEBase
	{
		private TextFormElementDE text;
		private ButtonDE remove_button;
		private VOReportNameFqanEditor myself;
		
		protected VOReportNameFqanEditor(DivEx parent) {
			super(parent);
			myself = this;
			
			text = new TextFormElementDE(this);
			text.addClass("inline");
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeVOReportNameFqan(myself);
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
			out.write("<div class=\"vo_report_name_fqan\">");
			text.render(out);
			remove_button.render(out);
			out.write("</div>");
		}
	}
	
	public void removeVOReportNameFqan(VOReportNameFqanEditor vo_report_name_fqan)
	{
		vo_report_name_fqans.remove(vo_report_name_fqan);
		redraw();
	}
	
	public void addVOReportNameFqan(String vo_report_name_fqan) { 
		VOReportNameFqanEditor elem = new VOReportNameFqanEditor(this);
		elem.setValue(vo_report_name_fqan);
		vo_report_name_fqans.add(elem);
		redraw();
	}
	
	public VOReportNameFqanDE(DivEx parent) {
		super(parent);
		add_button = new ButtonDE(this, "Add New FQAN for this VO Report Name");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addVOReportNameFqan("");
			}
			
		});
	}

	//Note: caller need to set the id for each record ?
	public ArrayList<String> getVOReportNameFqans()
	{
		ArrayList<String> records = new ArrayList<String>();
		for(VOReportNameFqanEditor vo_report_name_fqan : vo_report_name_fqans) {
			String str = vo_report_name_fqan.getValue();
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
		for(VOReportNameFqanEditor vo_report_name_fqan : vo_report_name_fqans) {
			vo_report_name_fqan.render(out);
		}
		add_button.render(out);
		out.print("</div>");
	}
}
