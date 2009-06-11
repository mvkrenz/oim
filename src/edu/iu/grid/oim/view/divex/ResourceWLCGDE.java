package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.sun.jndi.dns.ResourceRecord;
import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.CheckBoxFormElementDE;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.DoubleValidator;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ResourceWLCGModel;
import edu.iu.grid.oim.model.db.record.ResourceServiceRecord;
import edu.iu.grid.oim.model.db.record.ResourceWLCGRecord;
import edu.iu.grid.oim.model.db.record.ServiceRecord;
import edu.iu.grid.oim.view.divex.ResourceAliasDE.AliasEditor;

public class ResourceWLCGDE extends FormElementDEBase {

	private Context context;
	private WLCGEditor editor;
	private ButtonDE add_button;

	class WLCGEditor extends FormElementDEBase
	{
		//WLCG Interop details
		private CheckBoxFormElementDE interop_bdii;
		private CheckBoxFormElementDE interop_monitoring;
		private CheckBoxFormElementDE interop_accounting;
		private TextFormElementDE wlcg_accounting_name;
		private TextFormElementDE ksi2k_minimum;
		private TextFormElementDE ksi2k_maximum;
		private TextFormElementDE storage_capacity_minimum;
		private TextFormElementDE storage_capacity_maximum;

		private WLCGEditor myself;
		
		protected WLCGEditor(DivEx parent, ResourceWLCGRecord wrec) {
			super(parent);
			myself = this;
			
			interop_bdii = new CheckBoxFormElementDE(this);
			interop_bdii.setLabel("Should this resource be part of WLCG Interop BDII?");

			interop_monitoring = new CheckBoxFormElementDE(this);
			interop_monitoring.setLabel("Should this resource be part of WLCG Interop Monitoring?");

			interop_accounting = new CheckBoxFormElementDE(this);
			interop_accounting.setLabel("Should this resource be part of WLCG Interop Accounting?");

			wlcg_accounting_name = new TextFormElementDE(this);
			wlcg_accounting_name.setLabel("WLCG Accounting Name");
			wlcg_accounting_name.setSampleValue("ABC Accounting");
			wlcg_accounting_name.setRequired(true);
			
			hideWLCGAccountingName(true);

			interop_accounting.addEventListener(new EventListener() {
				public void handleEvent(Event e) {	
					if(((String)e.value).compareTo("true") == 0) {
						hideWLCGAccountingName(false);
					} else {
						hideWLCGAccountingName(true);
					}
				}
			});
			
			ksi2k_minimum = new TextFormElementDE(this);
			ksi2k_minimum.setLabel("KSI2K Minimum");
			ksi2k_minimum.addValidator(DoubleValidator.getInstance());
			ksi2k_minimum.setSampleValue("100.0");
			ksi2k_minimum.setRequired(true);

			ksi2k_maximum = new TextFormElementDE(this);
			ksi2k_maximum.setLabel("KSI2K Maximum");
			ksi2k_maximum.addValidator(DoubleValidator.getInstance());
			ksi2k_maximum.setSampleValue("500.0");
			ksi2k_maximum.setRequired(true);

			storage_capacity_minimum = new TextFormElementDE(this);
			storage_capacity_minimum.setLabel("Storage Capacity Minimum (in TeraBytes)");
			storage_capacity_minimum.addValidator(DoubleValidator.getInstance());
			storage_capacity_minimum.setSampleValue("1.0");
			storage_capacity_minimum.setRequired(true);

			storage_capacity_maximum = new TextFormElementDE(this);
			storage_capacity_maximum.setLabel("Storage Capacity Maximum (in TeraBytes)");
			storage_capacity_maximum.addValidator(DoubleValidator.getInstance());
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

		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}

		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"wlcg_editor\">");
			
			for(DivEx child : childnodes) {
				
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

		//caller should set resource_id
		public ResourceWLCGRecord getWLCGRecord() {
			ResourceWLCGRecord rec = new ResourceWLCGRecord();

			rec.interop_monitoring = interop_monitoring.getValue();
			rec.interop_bdii       = interop_bdii.getValue();
			rec.interop_accounting = interop_accounting.getValue();
			rec.accounting_name    = wlcg_accounting_name.getValue();
			rec.ksi2k_minimum      = ksi2k_minimum.getValueAsDouble();
			rec.ksi2k_maximum      = ksi2k_maximum.getValueAsDouble();
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

	public ResourceWLCGDE(DivEx parent, Context _context, ResourceWLCGRecord _wrec) {
		super(parent);
		context = _context;
		setWlcgRecord (_wrec);
	}

	protected void onEvent(Event e) {
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
