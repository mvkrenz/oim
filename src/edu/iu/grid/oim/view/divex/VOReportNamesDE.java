package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
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

import edu.iu.grid.oim.view.divex.VOReportNamesDE.VOReportNameEditor;

import edu.iu.grid.oim.model.VOReport;

import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.VOReportContactModel;
import edu.iu.grid.oim.model.db.VOReportNameFqanModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;


public class VOReportNamesDE extends FormElementDEBase {

	//ArrayList<VOReportNameEditor> vo_report_names = new ArrayList<VOReportNameEditor>();
	ArrayList<Integer> vo_report_id_taken = new ArrayList<Integer>();
	private ContactModel cmodel;
	private ArrayList<VOReportNameRecord> vorepname_records;
	private ButtonDE add_button;
	
	class VOReportNameEditor extends FormElementDEBase
	{
		//VO Report Name Details
		private Integer id;
		private Integer vo_id;
		private TextFormElementDE vo_report_name;
		private VOReportNameFqanDE vo_report_name_fqan;
		private ContactEditorDE vorc_editor ; // = new ContactEditorDE;

		private ButtonDE remove_button;
		private VOReportNameEditor myself;

		protected VOReportNameEditor(DivEx parent, 
				VOReportNameRecord vorepname_record,
				Collection<VOReportNameFqanRecord> vorepnamefqan_list,
				Collection<VOReportContactRecord> vorc_list) 
		{
			super(parent);
			myself = this;

			id    = vorepname_record.id;
			vo_id = vorepname_record.vo_id;

			new StaticDE(this, "<h3>Report Name</h3>");

			vo_report_name = new TextFormElementDE(this);
			//vo_report_name.setLabel("");
			vo_report_name.setRequired(true);
			vo_report_name.setValue(vorepname_record.name);

			new StaticDE(this, "<h3>Associated FQANs</h3>");
			vo_report_name_fqan = new VOReportNameFqanDE (this);

			if (vorepnamefqan_list != null) { 
				for(VOReportNameFqanRecord fqan_record : vorepnamefqan_list) {
					vo_report_name_fqan.addVOReportNameFqan (fqan_record);
				}
			}
			new StaticDE(this, "<h3>Subscriber Information</h3>");
			vorc_editor = new ContactEditorDE (this, cmodel, false, false);
			vorc_editor.setShowRank(false);
			vorc_editor.setMinContacts(ContactEditorDE.Rank.PRIMARY, 0);
			vorc_editor.setMaxContacts(ContactEditorDE.Rank.PRIMARY, 128);
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

		public VOReport getVOReport()
		{
			VOReport report = new VOReport();
			report.name = getVOReportNameRecord();
			report.fqans = vo_report_name_fqan.getVOReportNameFqanRecords();
			report.contacts = vorc_editor.getContactRecordsByRank(Integer.valueOf(1));
			return report;
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
			out.write("<div id=\""+getNodeID()+"\" class=\"vo_report_name_editor\">");
			
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
		remove(vo_report_name);
		redraw();
	}
	
	public void addVOReportName(VOReportNameRecord vorepname_record, 
					Collection<VOReportNameFqanRecord> vorepnamefqan_list,
					Collection<VOReportContactRecord> vorc_list) {

		VOReportNameEditor elem = new VOReportNameEditor(this, vorepname_record,
										vorepnamefqan_list, vorc_list);
		//vo_report_names.add(elem);
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
	/*
	public void validate()
	{
		redraw();
		valid = true;
		
		for(DivEx node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
				if(!vo_report_name.isValid()) {
					valid = false;
				}
			}
		}
	}
	*/
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		
		for(DivEx node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
				vo_report_name.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

	public ArrayList<VOReport> getVOReports()
	{
		ArrayList<VOReport> voreports = new ArrayList<VOReport>();
		
		for(DivEx node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
				voreports.add(vo_report_name.getVOReport());
			}
		}
		return voreports;
	}

//	public ArrayList<VOReportNameRecord> getVOReportNameRecords()
//	{
//		ArrayList<VOReportNameRecord> vorepname_records = new ArrayList<VOReportNameRecord>();
//		for(DivEx node : childnodes) {
//			if(node instanceof VOReportNameEditor) {
//				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
//				vorepname_records.add(vo_report_name.getVOReportNameRecord());
//			}
//		}
//		return vorepname_records;
//	}
//	
//
//	public ArrayList<VOReportNameFqanRecord> getVOReportNameFqanRecords()
//	{
//		ArrayList<VOReportNameFqanRecord> vorepnamefqan_records = new ArrayList<VOReportNameFqanRecord>();
//		for(DivEx node : childnodes) {
//			if(node instanceof VOReportNameEditor) {
//				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
//				//vorepnamefqan_records.addAll(vo_report_name.vo_report_name_fqan.getVOReportNameFqanRecords());
//				vorepnamefqan_records.addAll(vo_report_name.vo_report_name_fqan.getVOReportNameFqanRecords());
//			}
//		}
//		return vorepnamefqan_records;
//	}
//	
//
//	public ArrayList<VOReportContactRecord> getVOReportContactRecords()
//	{
//		ArrayList<VOReportContactRecord> vorepcontact_records = new ArrayList<VOReportContactRecord>();
//		for(DivEx node : childnodes) {
//			if(node instanceof VOReportNameEditor) {
//				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
//				ArrayList <ContactRecord> contacts = vo_report_name.contact_editor.getContactRecordsByRank(1);
//				
//				for (ContactRecord contact : contacts) {
//					vorepcontact_records.add(createVOReportContact(contact,vo_report_name.id));
//				}
//			}
//		}
//		return vorepcontact_records;
//	}
//
	// Should this be in model/record code? Too tired to think right now. -agopu
	// Set VOReportContact record for vo_report_name from the ContactRecord
	// Beware that VOContactRecord's vo_report_name_id is not populated.. 
	//  we need to fill it out with appropriate value later

}
