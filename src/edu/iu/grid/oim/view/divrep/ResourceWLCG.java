package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepDoubleValidator;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.view.ToolTip;

public class ResourceWLCG extends DivRepFormElement {

	private UserContext context;
	private WLCGEditor editor;
	private DivRepButton add_button;

	class WLCGEditor extends DivRepFormElement
	{
		//WLCG Interop details
		private DivRepCheckBox interop_bdii;
		private DivRepCheckBox interop_monitoring;
		private DivRepCheckBox interop_accounting;
		private DivRepTextBox wlcg_accounting_name;
		private DivRepTextBox ksi2k_minimum;
		private DivRepTextBox ksi2k_maximum;
		private DivRepTextBox hepspec;
		private DivRepTextBox apel_normal_factor;
		private DivRepTextBox storage_capacity_minimum;
		private DivRepTextBox storage_capacity_maximum;
		private DivRepTextBox tape_capacity;

		private WLCGEditor myself;
		
		protected WLCGEditor(DivRep parent, ResourceWLCGRecord wrec) {
			super(parent);
			myself = this;
			
			/*
			 * I believe http://lcg-bdii-conf.cern.ch/bdii-conf/bdii.conf is generated via MyOSG which
			 * relies on these flags
			 */
			
			interop_bdii = new DivRepCheckBox(this);
			interop_bdii.setLabel("Enable Interop BDII");
			new DivRepStaticContent(this, "<p class=\"help-block\">Forward this resource's BDII data to WLCG BDII server (lcg-bdii.cern.ch)</p>");
			
			interop_monitoring = new DivRepCheckBox(this);
			interop_monitoring.setLabel("Enable Interop Monitoring");
			new DivRepStaticContent(this, "<p class=\"help-block\"> Forward this resource's RSV data to WLCG Monitoring system (<a href=\"http://gridview.cern.ch/GRIDVIEW/ace_index.php\" target=\"_blank\">SAM/GridView</a> )</p>");

			interop_accounting = new DivRepCheckBox(this);
			interop_accounting.setLabel("Enable Interop Accounting - Forward Gratia Accounting Data to WLCG Accounting");
			new DivRepStaticContent(this, "<p class=\"help-block\">Include this resource in <b>Capacity and Benchmarking Report</b> sent to [osg-wlcg-reports@OPENSCIENCEGRID.ORG]. Please check this only if your site is MOU agreed Tier 1/2 sites.</p>");
			interop_accounting.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {	
					if(((String)e.value).compareTo("true") == 0) {
						hideWLCGAccountingName(false);
					} else {
						hideWLCGAccountingName(true);
					}
				}
			});
			wlcg_accounting_name = new DivRepTextBox(this);
			wlcg_accounting_name.addClass("divrep_indent");//make it look like it's part of above checkbox
			wlcg_accounting_name.setLabel("WLCG Accounting Name");
			wlcg_accounting_name.setSampleValue("ABC Accounting");
			wlcg_accounting_name.setRequired(true);
			
			ksi2k_minimum = new DivRepTextBox(this);
			ksi2k_minimum.setLabel("KSI2K Minimum");
			ksi2k_minimum.addValidator(DivRepDoubleValidator.getInstance());
			ksi2k_minimum.setSampleValue("100.0");
			ksi2k_minimum.setRequired(true);

			ksi2k_maximum = new DivRepTextBox(this);
			ksi2k_maximum.setLabel("KSI2K Maximum");
			ksi2k_maximum.addValidator(DivRepDoubleValidator.getInstance());
			ksi2k_maximum.setSampleValue("500.0");
			ksi2k_maximum.setRequired(true);

			hepspec = new DivRepTextBox(this);
			hepspec.setLabel("HEPSPEC Value");
			hepspec.addValidator(DivRepDoubleValidator.getInstance());
			hepspec.setSampleValue("85.0");
			hepspec.setRequired(true);
			
			apel_normal_factor = new DivRepTextBox(this);
			apel_normal_factor.setLabel("APEL Normalization Factor");
			apel_normal_factor.addValidator(DivRepDoubleValidator.getInstance());
			apel_normal_factor.setSampleValue("0.0");
			apel_normal_factor.setRequired(true);

			storage_capacity_minimum = new DivRepTextBox(this);
			storage_capacity_minimum.setLabel("Storage Capacity Minimum (in TeraBytes)");
			storage_capacity_minimum.addValidator(DivRepDoubleValidator.getInstance());
			storage_capacity_minimum.setSampleValue("1.0");
			storage_capacity_minimum.setRequired(true);

			storage_capacity_maximum = new DivRepTextBox(this);
			storage_capacity_maximum.setLabel("Storage Capacity Maximum (in TeraBytes)");
			storage_capacity_maximum.addValidator(DivRepDoubleValidator.getInstance());
			storage_capacity_maximum.setSampleValue("5.5");
			storage_capacity_maximum.setRequired(true);
			
			tape_capacity = new DivRepTextBox(this);
			tape_capacity.setLabel("Tape Capacity (in TeraBytes)");
			tape_capacity.addValidator(DivRepDoubleValidator.getInstance());
			tape_capacity.setSampleValue("5.5");
			
			hideWLCGAccountingName(true);
			
			ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
			if(wrec != null) {				
				//if WLCG record exist, populate the values
				interop_bdii.setValue(wrec.interop_bdii);
				interop_monitoring.setValue(wrec.interop_monitoring);
				interop_accounting.setValue(wrec.interop_accounting);

				if (wrec.interop_accounting != null) {
					wlcg_accounting_name.setValue(wrec.accounting_name);
				}
				if(wrec.ksi2k_minimum != null) {
					ksi2k_minimum.setValue(wrec.ksi2k_minimum.toString());
				}
				if(wrec.ksi2k_maximum != null) {
					ksi2k_maximum.setValue(wrec.ksi2k_maximum.toString());
				}
				if(wrec.hepspec != null) {
					hepspec.setValue(wrec.hepspec.toString());
				}
				if(wrec.apel_normal_factor != null) {
					apel_normal_factor.setValue(wrec.apel_normal_factor.toString());
				}
				if(wrec.storage_capacity_minimum != null) {
					storage_capacity_minimum.setValue(wrec.storage_capacity_minimum.toString());
				}
				if(wrec.storage_capacity_maximum != null) {
					storage_capacity_maximum.setValue(wrec.storage_capacity_maximum.toString());
				}
				if(wrec.tape_capacity != null) {
					tape_capacity.setValue(wrec.tape_capacity.toString());
				}
				
				if(wrec.interop_accounting) {
					hideWLCGAccountingName(false);
				}	
			}

		}

		private void hideWLCGAccountingName(Boolean b)
		{
			wlcg_accounting_name.setHidden(b);
			redraw();
			wlcg_accounting_name.setRequired(!b);
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"wlcg_editor\">");
			
			for(DivRep child : childnodes) {
				
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
			
			out.write("</div>");
		}

		//caller should set resource_id
		public ResourceWLCGRecord getWLCGRecord() {
			ResourceWLCGRecord rec = new ResourceWLCGRecord();

			rec.interop_monitoring = interop_monitoring.getValue();
			rec.interop_bdii       = interop_bdii.getValue();
			rec.interop_accounting = interop_accounting.getValue();
			rec.accounting_name    = wlcg_accounting_name.getValue();
			rec.ksi2k_minimum      = ksi2k_minimum.getValueAsDouble();
			rec.ksi2k_maximum      = ksi2k_maximum.getValueAsDouble();
			rec.hepspec			   = hepspec.getValueAsDouble();
			rec.apel_normal_factor = apel_normal_factor.getValueAsDouble();
			rec.storage_capacity_minimum = storage_capacity_minimum.getValueAsDouble();
			rec.storage_capacity_maximum = storage_capacity_maximum.getValueAsDouble();
			rec.tape_capacity = tape_capacity.getValueAsDouble();
			return rec;
		}
	}
	
	public ResourceWLCGRecord getWlcgRecord () {
		return editor.getWLCGRecord();
	}

	public void setWlcgRecord (ResourceWLCGRecord _wrec) {
		editor  = new WLCGEditor(this, _wrec);
	}

	public ResourceWLCG(DivRep parent, UserContext _context, ResourceWLCGRecord _wrec) {
		super(parent);
		context = _context;
		setWlcgRecord (_wrec);
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}
	
	public boolean validate()
	{
		//validate WLCG interop selections
		redraw();
		boolean valid = true;
		if(!editor.validate()) {
			valid = false;
		}
		setValid(valid);
		return valid;
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\" class=\"indent\">");
		if (!isHidden()) {
			editor.render(out);
		}
		out.print("</div>");
	}
}
