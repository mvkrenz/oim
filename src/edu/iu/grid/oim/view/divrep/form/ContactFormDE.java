package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.common.DivRepStaticContent;
import com.webif.divrep.common.CheckBoxFormElement;
import com.webif.divrep.common.DivRepForm;
import com.webif.divrep.common.DivRepFormElement;
import com.webif.divrep.common.DivRepSelectBox;
import com.webif.divrep.common.DivRepTextArea;
import com.webif.divrep.common.DivRepTextBox;
import com.webif.divrep.validator.DivRepEmailValidator;
import com.webif.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;

public class ContactFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ContactFormDE.class); 
   
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private DivRepTextBox name;
	private DivRepTextBox primary_email, secondary_email;
	private DivRepTextBox primary_phone, secondary_phone;
	private DivRepTextBox primary_phone_ext, secondary_phone_ext;
	private DivRepTextBox sms_address;
	private DivRepTextBox address_line_1, address_line_2;
	private DivRepTextBox city, state, zipcode, country;
	//sprivate CheckBoxFormElement active;
	private CheckBoxFormElement disable;
	private DivRepTextBox im;
	private PhotoDE photo_url;
	private CheckBoxFormElement person;
	private DivRepTextArea contact_preference;
	private DivRepSelectBox submitter_dn;
	
	class PhotoDE extends DivRepFormElement<String>
	{
		public PhotoDE(DivRep _parent) {
			super(_parent);
			url = new DivRepTextBox(this);
			url.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					PhotoDE.this.redraw();
				}});
			url.addValidator(DivRepUrlValidator.getInstance());
		}

		private DivRepTextBox url;

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		//bypass pretty much everything to url element
		public void setSampleValue(String value) {
			url.setSampleValue(value);
		}
		public void setValue(String value) {
			url.setValue(value);
		}
		public String getValue() {
			return url.getValue();
		}
		public Boolean isValid() { 
			return url.isValid();
		}
		public void setLabel(String label) {
			url.setLabel(label);
		}

		@Override
		public void render(PrintWriter out) {
			String img = url.getValue();
			if(img == null || img.length() == 0) {
				img = StaticConfig.getApplicationBase() + "/images/noavatar.gif";
			}
			
			out.print("<div ");
			renderClass(out);
			out.write("id=\""+getNodeID()+"\">");
			if(!hidden) {
				url.render(out);
				out.write("<img class=\"avatar_preview\" src=\""+img+"\"/>");
			}
			out.write("</div>");
		}
	}
	
	public void showHidePersonalDetail()
	{
		Boolean hidden = !person.getValue();
		address_line_1.setHidden(hidden);
		address_line_2.setHidden(hidden);
		city.setHidden(hidden);
		state.setHidden(hidden);
		zipcode.setHidden(hidden);
		country.setHidden(hidden);
		im.setHidden(hidden);
		photo_url.setHidden(hidden);
		ContactFormDE.this.redraw();
		
		Boolean required = person.getValue();
		city.setRequired(required);
		state.setRequired(required);
		zipcode.setRequired(required);
		country.setRequired(required);
	}
	
	public ContactFormDE(Context _context, ContactRecord rec, String origin_url)
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		name = new DivRepTextBox(this);
		name.setLabel("Full Name");
		name.setValue(rec.name);
		name.setRequired(true);
		
		primary_email = new DivRepTextBox(this);
		primary_email.setLabel("Primary Email");
		primary_email.setValue(rec.primary_email);
		primary_email.setRequired(true);
		primary_email.addValidator(new DivRepEmailValidator());
		
		secondary_email = new DivRepTextBox(this);
		secondary_email.setLabel("Secondary Email");
		secondary_email.setValue(rec.secondary_email);
		secondary_email.addValidator(new DivRepEmailValidator());

		primary_phone = new DivRepTextBox(this);
		primary_phone.setLabel("Primary Phone");
		primary_phone.setValue(rec.primary_phone);
		//primary_phone.setRequired(true);

		primary_phone_ext = new DivRepTextBox(this);
		primary_phone_ext.setLabel("Primary Phone Extension");
		primary_phone_ext.setValue(rec.primary_phone_ext);

		secondary_phone = new DivRepTextBox(this);
		secondary_phone.setLabel("Secondary Phone");
		secondary_phone.setValue(rec.secondary_phone);

		secondary_phone_ext = new DivRepTextBox(this);
		secondary_phone_ext.setLabel("Secondary Phone Extension");
		secondary_phone_ext.setValue(rec.secondary_phone_ext);
		
		sms_address = new DivRepTextBox(this);
		sms_address.setLabel("SMS Address");
		sms_address.setValue(rec.sms_address);
		sms_address.setSampleValue("8127771234@txt.att.net");
		sms_address.addValidator(DivRepEmailValidator.getInstance());
		
		contact_preference = new DivRepTextArea(this);
		contact_preference.setLabel("Enter Additional Contact Preferences");
		contact_preference.setValue(rec.contact_preference);
		contact_preference.setSampleValue("Please contact me via phone during the day.");

		new DivRepStaticContent(this, "<h2>Personal Information</h2>");
		
		person = new CheckBoxFormElement(this);
		person.setLabel("This is a personal contact (not mailing list, group contact, etc...)");
		person.setValue(rec.person);
		person.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				showHidePersonalDetail();
			}});
		
		new DivRepStaticContent(this, "<div class=\"indent\">");
		{
			address_line_1 = new DivRepTextBox(this);
			address_line_1.setLabel("Address Line 1");
			address_line_1.setValue(rec.address_line_1);
	
			address_line_2 = new DivRepTextBox(this);
			address_line_2.setLabel("Address Line 2");
			address_line_2.setValue(rec.address_line_2);
			
			city = new DivRepTextBox(this);
			city.setLabel("City");
			city.setValue(rec.city);
			city.setRequired(true);
	
			state = new DivRepTextBox(this);
			state.setLabel("State");
			state.setValue(rec.state);
			state.setRequired(true);
	
			zipcode = new DivRepTextBox(this);
			zipcode.setLabel("Zipcode");
			zipcode.setValue(rec.zipcode);
			zipcode.setRequired(true);
	
			country = new DivRepTextBox(this);
			country.setLabel("Country");
			country.setValue(rec.country);
			country.setRequired(true);
			
			im = new DivRepTextBox(this);
			im.setLabel("Instant Messaging Information");
			im.setValue(rec.im);
			im.setSampleValue("soichih@gtalk");
			
			photo_url = new PhotoDE(this);
			photo_url.setLabel("Photo URL");
			photo_url.setSampleValue("http://somewhere.com/myphoto.png");
			photo_url.setValue(rec.photo_url);
		}
		new DivRepStaticContent(this, "</div>");

		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
		}

		if(rec.person == null || rec.person == false) {
			showHidePersonalDetail();
		}
		/*
		active = new CheckBoxFormElement(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		*/
		disable = new CheckBoxFormElement(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
		
		//create DN selector
		TreeMap<Integer, String> dns = new TreeMap();
		try {
			DNModel dnmodel = new DNModel(context);;
			for(DNRecord dnrec : dnmodel.getAll()) {
				dns.put(dnrec.id, dnrec.dn_string);
			}
		} catch (SQLException e) {
			log.error(e);
		}
		submitter_dn = new DivRepSelectBox(this, dns);
		submitter_dn.setLabel("Submitter DN");
		submitter_dn.setValue(rec.submitter_dn_id);
		if(!auth.allows("admin")) {
			submitter_dn.setHidden(true);
		}
	}

	protected Boolean doSubmit() 
	{
		Boolean ret = true;
		ContactRecord rec = new ContactRecord();
		rec.id = id;
	
		rec.name = name.getValue();
		rec.primary_email = primary_email.getValue();
		rec.secondary_email = secondary_email.getValue();
		rec.primary_phone = primary_phone.getValue();
		rec.secondary_phone = secondary_phone.getValue();
		rec.primary_phone_ext = primary_phone_ext.getValue();
		rec.secondary_phone_ext = secondary_phone_ext.getValue();
		rec.sms_address = sms_address.getValue();
		rec.address_line_1 = address_line_1.getValue();
		rec.address_line_2 = address_line_2.getValue();
		rec.city = city.getValue();
		rec.state = state.getValue();
		rec.zipcode = zipcode.getValue();
		rec.country = country.getValue();
		//rec.active = active.getValue();
		rec.disable = disable.getValue();
		rec.person = person.getValue();
		rec.im = im.getValue();
		rec.photo_url = photo_url.getValue();
		rec.contact_preference = contact_preference.getValue();
		rec.submitter_dn_id = submitter_dn.getValue();
		
		ContactModel model = new ContactModel(context);
		try {
			if(rec.id == null) {
				model.insert(rec);
			} else {
				model.update(model.get(rec), rec);
			}
		} catch (Exception e) {
			alert(e.getMessage());
			ret = false;
		}
		context.close();
		return ret;
	}
}
