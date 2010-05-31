package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import java.util.GregorianCalendar;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepButton.Style;
import com.divrep.validator.DivRepEmailValidator;
import com.divrep.validator.DivRepIValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.divrep.Confirmation;

//alter table contact add column timezone varchar(16) default value "UTC";

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
	private DivRepCheckBox disable;
	private DivRepTextBox im;
	private PhotoDE photo_url;
	private DivRepSelectBox timezone;
	private HashMap<Integer, String> timezone_id2tz;
	private DivRepCheckBox person;
	private DivRepTextArea profile;
	private Confirmation confirmation;
	private DivRepTextArea contact_preference;
	private DivRepSelectBox submitter_dn;
	
	private PersonalInfo personal_info;
	
	class PersonalInfo extends DivRepFormElement
	{
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}	
		
		PersonalInfo(DivRep _parent, ContactRecord rec, DNRecord associated_dn_rec) {
			super(_parent);
			
			String associated_dn_string = "Contact not registered on OIM";
			if (associated_dn_rec != null ) {
				associated_dn_string = associated_dn_rec.dn_string;
			}
			new DivRepStaticContent(this, "<div class=\"divrep_form_element\"><label>Associated DN</label><br/><input type=\"text\" disabled=\"disabled\" style=\"width: 400px;\" value=\""+ associated_dn_string +"\"/><br/><sub>* Can only be modified by GOC Staff on request using Admin interface</sub></div>");

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
			photo_url.setLabel("Profile Picture URL (where we can pick up an image)");
			photo_url.setSampleValue("http://somewhere.com/myphoto.png");
			photo_url.setValue(rec.photo_url);
			
			timezone = new DivRepSelectBox(this);
			timezone_id2tz = new HashMap<Integer, String>();
			int i = 0;
			for(int offset = -12;offset < 12;++offset) {
				LinkedHashMap<Integer, String> group = new LinkedHashMap<Integer, String>();
				for(String tz : TimeZone.getAvailableIDs(offset*1000*3600)) {
					Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(tz));
					String tstr = String.format("%02d", cal.get(Calendar.HOUR)) + ":" + String.format("%02d", cal.get(Calendar.MINUTE));
					switch(cal.get(Calendar.AM_PM)) {
					case Calendar.AM:
						tstr += " AM";
						break;
					default:
						tstr += " PM";
					}
					tstr += String.format("%2d", cal.get(Calendar.MONTH)+1) + "/" + cal.get(Calendar.DAY_OF_MONTH);
					group.put(i, tstr + " " + tz);
					timezone_id2tz.put(i, tz);
			
					if(tz.equals(rec.timezone)) {
						timezone.setValue(i);
					}
					
					++i;
				}
				String group_name = "GMT";
				if(offset < 0) {
					group_name += offset;
				} else if(offset > 0) {
					group_name += "+" + offset;
				}
				timezone.addGroup(group_name, group);
			}
			timezone.setLabel("Time Zone");
			timezone.setRequired(true);

			profile = new DivRepTextArea(this);
			profile.setLabel("Profile Details - Tell OSG about yourself, your role, etc.");
			profile.setSampleValue("Please enter your role within OSG community, and maybe a small introduction of who you are and what you do.");
			profile.setValue(rec.profile);
		}
		
		public void render(PrintWriter out) {
			out.print("<div class=\"indent\" id=\""+getNodeID()+"\">");	
			if(!hidden) {
				for(DivRep child : childnodes) {
					if(child instanceof DivRepFormElement) {
						out.print("<div class=\"divrep_form_element\">");
						child.render(out);
						out.print("</div>");
					
					} else {
						//non form element..
						child.render(out);
					}
				}
				error.render(out);
			}
			out.print("</div>");
		}
	}
	
	/*
	public void setConfirmed(Timestamp _confirmed)
	{
		confirmed = _confirmed;
	}
	*/
	
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
		/*
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
		*/
	
		Boolean required = person.getValue();
		city.setRequired(required);
		state.setRequired(required);
		zipcode.setRequired(required);
		country.setRequired(required);

		personal_info.setHidden(!person.getValue());
		personal_info.redraw();
	}
	
	public ContactFormDE(Context _context, ContactRecord rec, String origin_url,
			boolean profileEdit) //, boolean newRegistration)
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		DNRecord associated_dn_rec = null;
		if (id != null) {
			try {
				associated_dn_rec = new DNRecord();
				DNModel dnmodel = new DNModel(context);;
				associated_dn_rec= dnmodel.getByContactID(id);
			} catch (SQLException e) {
				log.error(e);
			}
			if ((associated_dn_rec != null) && (!profileEdit)) {
				if (associated_dn_rec.dn_string.equals(context.getAuthorization().getUserDN())) {
					new DivRepStaticContent(this, "<div><h2>NOTE: This is your profile!</h2></div>");
				}
			}
		}

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
		
		person = new DivRepCheckBox(this);
		person.setLabel("This is a person/service contact (not a group mailing list, etc...) that in the future may attempt to register an X509 certificate on OIM.");
		person.setValue(rec.person);

		personal_info = new PersonalInfo(this, rec, associated_dn_rec);

		if ((profileEdit == true) || (associated_dn_rec!= null)) {
			person.setValue(true);
			person.setLabel("Person contact flag (Cannot modify: Always true for users whose DN is registered with OIM)");
			person.setDisabled(true);
		}
		else {
			person.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					showHidePersonalDetail();
				}}
			);
		}
		if(rec.person == null || rec.person == false) {
			showHidePersonalDetail();
		}
		
		new DivRepStaticContent(this, "<h2>Confirmation</h2>");
		confirmation = new Confirmation(this, rec, auth);
		
		if(auth.allows("admin")) {
			new DivRepStaticContent(this, "<h2>Administrative Tasks</h2>");
		}

		//create DN selector
		LinkedHashMap<Integer, String> dns = new LinkedHashMap();
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

		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);

		if (profileEdit == true) {
			disable.setLabel("Disable (Not allowed on your own profile! Ask other GOC staff for help.)");
			disable.setDisabled(true);
		}
		if(!auth.allows("admin")) {
			disable.setHidden(true);
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
		rec.disable = disable.getValue();
		rec.person = person.getValue();
		rec.im = im.getValue();
		rec.photo_url = photo_url.getValue();
		rec.contact_preference = contact_preference.getValue();
		rec.submitter_dn_id = submitter_dn.getValue();
		rec.confirmed = confirmation.getTimestamp();
		rec.timezone = timezone_id2tz.get(timezone.getValue());
		rec.profile = profile.getValue();
		
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
