package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepDoubleValidator;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;

public class ResourceWLCG extends DivRepFormElement {

	private Context context;
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
		private DivRepTextBox storage_capacity_minimum;
		private DivRepTextBox storage_capacity_maximum;

		private WLCGEditor myself;
		
		protected WLCGEditor(DivRep parent, ResourceWLCGRecord wrec) {
			super(parent);
			myself = this;
			
			interop_bdii = new DivRepCheckBox(this);
			interop_bdii.setLabel("Should this resource be part of WLCG Interop BDII?");

			interop_monitoring = new DivRepCheckBox(this);
			interop_monitoring.setLabel("Should this resource be part of WLCG Interop Monitoring?");

			interop_accounting = new DivRepCheckBox(this);
			interop_accounting.setLabel("Should this resource be part of WLCG Interop Accounting?");

			wlcg_accounting_name = new DivRepTextBox(this);
			wlcg_accounting_name.setLabel("WLCG Accounting Name");
			wlcg_accounting_name.setSampleValue("ABC Accounting");
			wlcg_accounting_name.setRequired(true);
			
			hideWLCGAccountingName(true);

			interop_accounting.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {	
					if(((String)e.value).compareTo("true") == 0) {
						hideWLCGAccountingName(false);
					} else {
						hideWLCGAccountingName(true);
					}
				}
			});
			
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
			
			ResourceWLCGModel wmodel = new ResourceWLCGModel(context);
			if(wrec != null) {
					//if WLCG record exist, populate the values
				interop_bdii.setValue(wrec.interop_bdii);
				interop_monitoring.setValue(wrec.interop_monitoring);
				interop_accounting.setValue(wrec.interop_accounting);
				
				if (wrec.interop_accounting) {
					wlcg_accounting_name.setValue(wrec.accounting_name);
					hideWLCGAccountingName(false);
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
				if(wrec.storage_capacity_minimum != null) {
					storage_capacity_minimum.setValue(wrec.storage_capacity_minimum.toString());
				}
				if(wrec.storage_capacity_maximum != null) {
					storage_capacity_maximum.setValue(wrec.storage_capacity_maximum.toString());
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
			rec.storage_capacity_minimum = storage_capacity_minimum.getValueAsDouble();
			rec.storage_capacity_maximum = storage_capacity_maximum.getValueAsDouble();
			return rec;
		}
	}
	
	public ResourceWLCGRecord getWlcgRecord () {
		return editor.getWLCGRecord();
	}

	public void setWlcgRecord (ResourceWLCGRecord _wrec) {
		editor  = new WLCGEditor(this, _wrec);
	}

	public ResourceWLCG(DivRep parent, Context _context, ResourceWLCGRecord _wrec) {
		super(parent);
		context = _context;
		setWlcgRecord (_wrec);
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}
	
	public void validate()
	{
		//validate WLCG interop selections
		redraw();
		valid = true;
		if(!editor.isValid()) {
			valid = false;
		}
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		if (!hidden) {
			editor.render(out);
		}
		out.print("</div>");
	}
}
