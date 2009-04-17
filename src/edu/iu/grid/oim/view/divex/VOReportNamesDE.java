package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.StaticDE;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.UniqueValidator;

import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.VOReportContactModel;
import edu.iu.grid.oim.model.db.VOReportNameFqanModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;

public class VOReportNamesDE extends FormElementDEBase {

	ArrayList<VOReportNameEditor> vo_report_names = new ArrayList<VOReportNameEditor>();
	ArrayList<Integer> vo_report_id_taken = new ArrayList<Integer>();
	private ContactModel cmodel;
	private ArrayList<VOReportNameRecord> vorepname_records;
	private ButtonDE add_button;
	
	class VOReportNameEditor extends DivEx
	{
		//VO Report Name Details
		private Integer id;
		private Integer vo_id;
		private TextFormElementDE vo_report_name;
		private VOReportNameFqanDE vo_report_name_fqan;
		private ContactEditorDE contact_editor; // = new ContactEditorDE;

		private ButtonDE remove_button;
		private VOReportNameEditor myself;

		protected VOReportNameEditor(DivEx parent, 
				VOReportNameRecord vorepname_record,
				ArrayList<VOReportNameFqanRecord> fqan_records,
				ArrayList<VOReportContactRecord> vorc_list) 
		{
			super(parent);
			myself = this;

			id    = vorepname_record.id;
			vo_id = vorepname_record.vo_id;

			vo_report_name = new TextFormElementDE(this);
			vo_report_name.setLabel("Report Name");
			vo_report_name.setRequired(true);
			vo_report_name.setValue(vorepname_record.name);

			vo_report_name_fqan = new VOReportNameFqanDE (this);

			if (fqan_records != null) { 
				for(VOReportNameFqanRecord fqan_record : fqan_records) {
					vo_report_name_fqan.addVOReportNameFqan(fqan_record.fqan);
				}
			}
			new StaticDE(this, "<h3>Subscriber Information</h3>");
			ContactEditorDE vorc_editor = new ContactEditorDE (this, cmodel, false, false);
			vorc_editor.setShowRank(false);
			vorc_editor.setMinContacts(ContactEditorDE.Rank.PRIMARY, 1);
			if(vorc_list != null) {
				for(VOReportContactRecord vorc_record : vorc_list) {
					ContactRecord keyrec = new ContactRecord();
					keyrec.id = vorc_record.contact_id;
					ContactRecord person = null;
					try {
						person = cmodel.get(keyrec);
						vorc_editor.addSelected(person, vorc_record.contact_rank_id);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.setConfirm(true, "Do you really want to remove this VO Report Name?");
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeVOReportName(myself);	
				}
			});
		}	
	
		public void setVOReportName(Integer value) {
			vo_report_name.setValue(value.toString());
		}
		public Integer getVOReportName() {
			return Integer.getInteger(vo_report_name.getValue());
		}
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div class=\"vo_report_name_editor\">");
			
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

//		//caller should set vo_report_name_id
		public VOReportNameRecord getVOReportNameRecord() {
			VOReportNameRecord rec = new VOReportNameRecord();
			rec.id    = id;
			rec.name  = vo_report_name.getValue();
			rec.vo_id = vo_id;
			return rec;
		}
	}
	
	public void removeVOReportName(VOReportNameEditor vo_report_name)
	{
		vo_report_names.remove(vo_report_name);
		redraw();
	}
	
	public void addVOReportName(VOReportNameRecord vorepname_record, 
					ArrayList<VOReportNameFqanRecord> fqan_records,
					ArrayList<VOReportContactRecord> vorc_list) {

		VOReportNameEditor elem = new VOReportNameEditor(this, vorepname_record,
										fqan_records, vorc_list);
		vo_report_names.add(elem);
		redraw();
	}
	
	public VOReportNamesDE(DivEx parent, 
						ArrayList<VOReportNameRecord> _vorepname_records, 
						ContactModel _cmodel) {
		super(parent);
		vorepname_records = _vorepname_records;
		cmodel = _cmodel;
		
		add_button = new ButtonDE(this, "Add New Report Name");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addVOReportName(new VOReportNameRecord(),null,null);
			}
			
		});
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

	public ArrayList<VOReportNameRecord> getVOReportNameRecords()
	{
		ArrayList<VOReportNameRecord> vorepname_records = new ArrayList<VOReportNameRecord>();
		for(VOReportNameEditor vo_report_name : vo_report_names) {
			vorepname_records.add(vo_report_name.getVOReportNameRecord());
		}
		return vorepname_records;
	}

}
