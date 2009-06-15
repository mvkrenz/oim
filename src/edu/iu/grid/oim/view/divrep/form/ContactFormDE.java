package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import org.apache.log4j.Logger;
import com.webif.divrep.DivRep;
import com.webif.divrep.Event;
import com.webif.divrep.EventListener;
import com.webif.divrep.Static;
import com.webif.divrep.form.CheckBoxFormElement;
import com.webif.divrep.form.FormBase;
import com.webif.divrep.form.FormElementBase;
import com.webif.divrep.form.SelectFormElement;
import com.webif.divrep.form.TextAreaFormElement;
import com.webif.divrep.form.TextFormElement;
import com.webif.divrep.form.validator.EmailValidator;
import com.webif.divrep.form.validator.UrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;

public class ContactFormDE extends FormBase 
{
    static Logger log = Logger.getLogger(ContactFormDE.class); 
   
    private Context context;
    private Authorization auth;
	private Integer id;
	
	private TextFormElement name;
	private TextFormElement primary_email, secondary_email;
	private TextFormElement primary_phone, secondary_phone;
	private TextFormElement primary_phone_ext, secondary_phone_ext;
	private TextFormElement address_line_1, address_line_2;
	private TextFormElement city, state, zipcode, country;
	private CheckBoxFormElement active;
	private CheckBoxFormElement disable;
	private TextFormElement im;
	private PhotoDE photo_url;
	private CheckBoxFormElement person;
	private TextAreaFormElement contact_preference;
	private SelectFormElement submitter_dn;
	
	class PhotoDE extends FormElementBase<String>
	{
		public PhotoDE(DivRep _parent) {
			super(_parent);
			url = new TextFormElement(this);
			url.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					PhotoDE.this.redraw();
				}});
			url.addValidator(UrlValidator.getInstance());
		}

		private TextFormElement url;

		@Override
		protected void onEvent(Event e) {
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
				img = Config.getApplicationBase() + "/images/noavatar.gif";
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

		name = new TextFormElement(this);
		name.setLabel("Full Name");
		name.setValue(rec.name);
		name.setRequired(true);
		
		primary_email = new TextFormElement(this);
		primary_email.setLabel("Primary Email");
		primary_email.setValue(rec.primary_email);
		primary_email.setRequired(true);
		primary_email.addValidator(new EmailValidator());
		
		secondary_email = new TextFormElement(this);
		secondary_email.setLabel("Secondary Email");
		secondary_email.setValue(rec.secondary_email);
		secondary_email.addValidator(new EmailValidator());

		primary_phone = new TextFormElement(this);
		primary_phone.setLabel("Primary Phone");
		primary_phone.setValue(rec.primary_phone);
		//primary_phone.setRequired(true);

		primary_phone_ext = new TextFormElement(this);
		primary_phone_ext.setLabel("Primary Phone Extension");
		primary_phone_ext.setValue(rec.primary_phone_ext);

		secondary_phone = new TextFormElement(this);
		secondary_phone.setLabel("Secondary Phone");
		secondary_phone.setValue(rec.secondary_phone);

		secondary_phone_ext = new TextFormElement(this);
		secondary_phone_ext.setLabel("Secondary Phone Extension");
		secondary_phone_ext.setValue(rec.secondary_phone_ext);
		
		contact_preference = new TextAreaFormElement(this);
		contact_preference.setLabel("Enter Additional Contact Preferences");
		contact_preference.setValue(rec.contact_preference);
		contact_preference.setSampleValue("Please contact me via phone during the day.");

		new Static(this, "<h2>Personal Information</h2>");
		
		person = new CheckBoxFormElement(this);
		person.setLabel("This is a personal contact (not mailing list, group contact, etc...)");
		person.setValue(rec.person);
		person.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				showHidePersonalDetail();
			}});
		
		new Static(this, "<div class=\"indent\">");
		{
			address_line_1 = new TextFormElement(this);
			address_line_1.setLabel("Address Line 1");
			address_line_1.setValue(rec.address_line_1);
	
			address_line_2 = new TextFormElement(this);
			address_line_2.setLabel("Address Line 2");
			address_line_2.setValue(rec.address_line_2);
			
			city = new TextFormElement(this);
			city.setLabel("City");
			city.setValue(rec.city);
			city.setRequired(true);
	
			state = new TextFormElement(this);
			state.setLabel("State");
			state.setValue(rec.state);
			state.setRequired(true);
	
			zipcode = new TextFormElement(this);
			zipcode.setLabel("Zipcode");
			zipcode.setValue(rec.zipcode);
			zipcode.setRequired(true);
	
			country = new TextFormElement(this);
			country.setLabel("Country");
			country.setValue(rec.country);
			country.setRequired(true);
			
			im = new TextFormElement(this);
			im.setLabel("Instant Messaging Information");
			im.setValue(rec.im);
			im.setSampleValue("soichih@gtalk");
			
			photo_url = new PhotoDE(this);
			photo_url.setLabel("Photo URL");
			photo_url.setSampleValue("http://somewhere.com/myphoto.png");
			photo_url.setValue(rec.photo_url);
		}
		new Static(this, "</div>");

		if(auth.allows("admin")) {
			new Static(this, "<h2>Administrative Tasks</h2>");
		}

		if(rec.person == null || rec.person == false) {
			showHidePersonalDetail();
		}
		
		active = new CheckBoxFormElement(this);
		active.setLabel("Active");
		active.setValue(rec.active);
		if(!auth.allows("admin")) {
			active.setHidden(true);
		}
		
		disable = new CheckBoxFormElement(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		if(!auth.allows("admin")) {
			disable.setHidden(true);
		}
		
		//create DN selector
		HashMap<Integer, String> dns = new HashMap();
		try {
			DNModel dnmodel = new DNModel(context);;
			for(DNRecord dnrec : dnmodel.getAll()) {
				dns.put(dnrec.id, dnrec.dn_string);
			}
		} catch (SQLException e) {
			log.error(e);
		}
		submitter_dn = new SelectFormElement(this, dns);
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
		rec.address_line_1 = address_line_1.getValue();
		rec.address_line_2 = address_line_2.getValue();
		rec.city = city.getValue();
		rec.state = state.getValue();
		rec.zipcode = zipcode.getValue();
		rec.country = country.getValue();
		rec.active = active.getValue();
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
