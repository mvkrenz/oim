package edu.iu.grid.oim.view.divex.form;

import java.util.HashMap;

import com.webif.divex.DivEx;
import com.webif.divex.form.IFormElementDE;
import com.webif.divex.form.SelectFormElementDE;
import com.webif.divex.form.TextAreaFormElementDE;
import com.webif.divex.form.TextFormElementDE;
import com.webif.divex.form.validator.RequiredValidator;
import com.webif.divex.form.validator.UrlValidator;

import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divex.FormDivex;

public class VOFormDivex extends FormDivex 
{
	public VOFormDivex(DivEx parent, VORecord rec, String origin_url, 
			HashMap<Integer, String> scs,
			HashMap<Integer, String> othervos)
	{	
		super(parent, origin_url);
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "name");
			elem.setLabel("Name");
			elem.setValue(rec.name);
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "long_name");
			elem.setLabel("Long Name");
			elem.setValue(rec.long_name);
			elem.setRequired(true);
		}
		
		{
			TextAreaFormElementDE elem = new TextAreaFormElementDE(this, "description");
			elem.setLabel("Description");
			elem.setValue(rec.description);
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "primary_url");
			elem.setLabel("Primary URL");
			elem.setValue(rec.primary_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "aup_url");
			elem.setLabel("AUP URL");
			elem.setValue(rec.aup_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "membership_services_url");
			elem.setLabel("Membership Services URL");
			elem.setValue(rec.membership_services_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "purpose_url");
			elem.setLabel("Purpose URL"); 
			elem.setValue(rec.purpose_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "support_url");
			elem.setLabel("Support URL"); 
			elem.setValue(rec.support_url);
			elem.setValidator(UrlValidator.getInstance());
			elem.setRequired(true);
		}
		
		{
			TextAreaFormElementDE elem = new TextAreaFormElementDE(this, "app_description");
			elem.setLabel("App Description");
			elem.setValue(rec.app_description);
			elem.setRequired(true);
		}
		
		{
			TextAreaFormElementDE elem = new TextAreaFormElementDE(this, "community");
			elem.setLabel("Community");
			elem.setValue(rec.community);
			elem.setRequired(true);
		}
		
		{
			TextFormElementDE elem = new TextFormElementDE(this, "footprints_id");
			elem.setLabel("Footprints ID");
			elem.setValue(rec.footprints_id);
			elem.setRequired(true);
		}
		
		{
			SelectFormElementDE elem = new SelectFormElementDE(this, "sc_id", scs);
			elem.setLabel("Support Center");
			elem.setValue(rec.sc_id);
			elem.setRequired(true);
		}
		
		{
			SelectFormElementDE elem = new SelectFormElementDE(this, "parent_vo_id", othervos);
			elem.setLabel("Parent Virtual Organization");
			elem.setValue(rec.parent_vo_id);
		}
		/*
		{
			CheckBoxFormElementDE elem = new CheckBoxFormElementDE(this, "active");
			elem.setLabel("Active");
			elem.setValueFromBoolean(rec.active);
		}
		*/
	}

	protected Boolean doSubmit() {
		VORecord rec = new VORecord();
			
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("name");
			rec.name = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("long_name");
			rec.long_name = elem.getValue();
		}
		
		{
			TextAreaFormElementDE elem = (TextAreaFormElementDE) getElement("description");
			rec.description = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("primary_url");
			rec.primary_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("aup_url");
			rec.aup_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("membership_services_url");
			rec.membership_services_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("purpose_url");
			rec.purpose_url = elem.getValue();
		}
		
		{
			TextFormElementDE elem = (TextFormElementDE) getElement("support_url");
			rec.support_url = elem.getValue();
		}
		
		{
			TextAreaFormElementDE elem = (TextAreaFormElementDE) getElement("app_description");
			rec.app_description = elem.getValue();
		}
		
		{
			TextAreaFormElementDE elem = (TextAreaFormElementDE) getElement("community");
			rec.community = elem.getValue();
		}
		
		{
			SelectFormElementDE elem = (SelectFormElementDE) getElement("sc_id");
			rec.sc_id = elem.getValue();
		}
		
		{
			SelectFormElementDE elem = (SelectFormElementDE) getElement("parent_vo_id");
			rec.parent_vo_id = elem.getValue();
		}	
		
		//public Boolean active;
		//public Boolean disable;
		
		{	
			TextFormElementDE elem = (TextFormElementDE) getElement("footprints_id");
			rec.footprints_id = elem.getValue();
		}	
		System.out.println("do submit form");
		return true;
	}
}
