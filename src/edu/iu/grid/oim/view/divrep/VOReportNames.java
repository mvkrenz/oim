package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.VOModel.VOReport;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOReportNames extends DivRepFormElement {

	//ArrayList<VOReportNameEditor> vo_report_names = new ArrayList<VOReportNameEditor>();
	ArrayList<Integer> vo_report_id_taken = new ArrayList<Integer>();
	private ContactModel cmodel;
	private ArrayList<VOReportNameRecord> vorepname_records;
	private DivRepButton add_button;
	
	class VOReportNameEditor extends DivRepFormElement
	{
		//VO Report Name Details
		private Integer id;
		private Integer vo_id;
		private DivRepTextBox vo_report_name;
		private VOReportNameFqan vo_report_name_fqan;
		private ContactEditor vorc_editor ; // = new ContactEditor;

		private DivRepButton remove_button;
		private VOReportNameEditor myself;

		protected VOReportNameEditor(DivRep parent, 
				VOReportNameRecord vorepname_record,
				Collection<VOReportNameFqanRecord> vorepnamefqan_list,
				Collection<VOReportContactRecord> vorc_list) 
		{
			super(parent);
			myself = this;

			id    = vorepname_record.id;
			vo_id = vorepname_record.vo_id;

			new DivRepStaticContent(this, "<h4>Report Name</h4>");

			vo_report_name = new DivRepTextBox(this);
			//vo_report_name.setLabel("");
			vo_report_name.setRequired(true);
			vo_report_name.setValue(vorepname_record.name);
			vo_report_name.addValidator(new DivRepIValidator<String>(){
				public String getErrorMessage() {
					return "The report name is already used in another report. Please choose a different name.";
				}
				public Boolean isValid(String value) {
					return !hasDuplicateName(VOReportNameEditor.this);
				}
			});

			new DivRepStaticContent(this, "<h4>FQANs</h4>");
			vo_report_name_fqan = new VOReportNameFqan (this);

			if (vorepnamefqan_list != null) { 
				for(VOReportNameFqanRecord fqan_record : vorepnamefqan_list) {
					vo_report_name_fqan.addVOReportNameFqan (fqan_record);
				}
			}
			new DivRepStaticContent(this, "<h4>Subscribers (type to search)</h4>");
			vorc_editor = new ContactEditor (this, cmodel, false, false);
			vorc_editor.setShowRank(false);
			vorc_editor.setMinContacts(ContactEditor.Rank.PRIMARY, 0);
			vorc_editor.setMaxContacts(ContactEditor.Rank.PRIMARY, 128);
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

			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			//remove_button.setConfirm(true, "Do you really want to remove this VO Report Name?");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeVOReportName(myself);	
				}
			});
		}	

		public VOReport getVOReport(VOModel model)
		{
			VOReport report = model.new VOReport();
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
		
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"well\">");
			
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");

			for(DivRep child : childnodes) {
				if(child == remove_button) continue;
				
				if(child instanceof DivRepFormElement) {
					DivRepFormElement elem = (DivRepFormElement)child;
					if(!elem.isHidden()) {
						out.print("<div class=\"divrep_form_element\">");
						child.render(out);
						out.print("</div>");
					}
				} else {
					//non form element..
					if(child instanceof DivRepFormElement.ErrorDE) continue;
					child.render(out);
				}
			}
			error.render(out);
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
		validate_count();
		redraw();
		modified(true);
	}
	
	public void addVOReportName(VOReportNameRecord vorepname_record, 
					Collection<VOReportNameFqanRecord> vorepnamefqan_list,
					Collection<VOReportContactRecord> vorc_list) {

		VOReportNameEditor elem = new VOReportNameEditor(this, vorepname_record,
										vorepnamefqan_list, vorc_list);
		//vo_report_names.add(elem);
		redraw();
	}
	
	public VOReportNames(DivRep parent, 
						ArrayList<VOReportNameRecord> _vorepname_records, 
						ContactModel _cmodel) {
		super(parent);
		vorepname_records = _vorepname_records;
		cmodel = _cmodel;
		
		add_button = new DivRepButton(this, "Add New Report Name");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addVOReportName(new VOReportNameRecord(),null,null);
				validate_count();
				modified(true);
			}
			
		});
	}

	public boolean validate()
	{
		super.validate();
		validate_count();
		return getValid();
	}
	public boolean hasDuplicateName(VOReportNameEditor that)
	{
		String that_name = that.vo_report_name.getValue();
		for(DivRep node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				if(node == that) break;
				VOReportNameEditor editor = (VOReportNameEditor)node;
				if(editor.vo_report_name.getValue().equals(that_name)) return true;
			}
		}
		return false;
	}
	
	public void validate_count()
	{
		int report_count = 0;
		for(DivRep node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				++report_count;
			}
		}
		if(report_count == 0) {
			setValid(false);
			error.set("Please provide at least one VO Report");
			error.redraw();
			return;
		} else {
			error.set(null);
			error.redraw();
		}
	}
	
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		
		for(DivRep node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
				vo_report_name.render(out);
			}
		}
		add_button.render(out);
		error.render(out);
		out.print("</div>");
	}

	public ArrayList<VOReport> getVOReports(VOModel model)
	{
		ArrayList<VOReport> voreports = new ArrayList<VOReport>();
		
		for(DivRep node : childnodes) {
			if(node instanceof VOReportNameEditor) {
				VOReportNameEditor vo_report_name = (VOReportNameEditor)node;
				voreports.add(vo_report_name.getVOReport(model));
			}
		}
		return voreports;
	}

//	public ArrayList<VOReportNameRecord> getVOReportNameRecords()
//	{
//		ArrayList<VOReportNameRecord> vorepname_records = new ArrayList<VOReportNameRecord>();
//		for(DivRep node : childnodes) {
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
//		for(DivRep node : childnodes) {
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
//		for(DivRep node : childnodes) {
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
