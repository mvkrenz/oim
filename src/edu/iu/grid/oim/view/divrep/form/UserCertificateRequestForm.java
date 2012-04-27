package edu.iu.grid.oim.view.divrep.form;

import java.io.IOException;
import java.util.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepEmailValidator;
import com.divrep.validator.DivRepIValidator;
import com.divrep.validator.DivRepLengthValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Footprints;
import edu.iu.grid.oim.lib.Footprints.FPTicket;
import edu.iu.grid.oim.lib.HashHelper;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.cert.DivRepPassStrengthValidator;
import edu.iu.grid.oim.model.cert.GenerateCSR;
import edu.iu.grid.oim.model.cert.ICertificateSigner;
import edu.iu.grid.oim.model.db.UserCertificateRequestModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.servlet.CertificateRequestServlet;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.DivRepReCaptcha;
import edu.iu.grid.oim.view.divrep.DivRepSimpleCaptcha;
import edu.iu.grid.oim.view.divrep.VOResourceOwnership;

public class UserCertificateRequestForm extends DivRepForm
{
    static Logger log = Logger.getLogger(UserCertificateRequestForm.class);
	private UserContext context;
	private Authorization auth;
	
	//subscriber identify
	private DivRepTextBox fullname;
	private DivRepTextBox email;
	private DivRepTextBox phone;
	//private DivRepTextBox orgunit, orgname;
	//private DivRepTextBox address_line_1, address_line_2;
	private DivRepTextBox city, state, country, zipcode;
	private DivRepSelectBox timezone;
	private HashMap<Integer, String> timezone_id2tz;
	
	private DivRepTextArea profile;
	private DivRepCheckBox use_twiki;
	private DivRepTextBox twiki_id;
	
	private DivRepPassword passphrase; //for guest
	private DivRepCheckBox agreement;
	
	private DivRepSelectBox vo;//, sponsor;
	
	/*
	//oim profile
	private DivRepTextBox secondary_email ;
	private DivRepTextBox primary_phone, secondary_phone;
	private DivRepTextBox primary_phone_ext, secondary_phone_ext;
	private DivRepTextBox sms_address;
	private DivRepTextBox im;
	private DivRepSelectBox timezone;
	private HashMap<Integer, String> timezone_id2tz;
	private DivRepTextArea profile;
	private DivRepTextArea contact_preference;
	private DivRepCheckBox use_twiki;
	private DivRepTextBox twiki_id;
	*/
	
	public UserCertificateRequestForm(final UserContext context, String origin_url) {
		
		super(context.getPageRoot(), origin_url);
		this.context = context;
		auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
	
	
		/*
		class CheckValidator implements DivRepIValidator
		{
			DivRepTextBox other;
			public CheckValidator(DivRepTextBox _other) {
				other = _other;
			}
			public String getErrorMessage() {
				return "Email address doesn't match.";
			}
			public Boolean isValid(Object value) {
				if(other.getValue() == null) return false;
				return other.getValue().equals((String)value);
			}
		}
		
		primary_email_check = new DivRepTextBox(this);
		primary_email_check.setLabel("Email (Confirm)");
		primary_email_check.setRequired(true);
		primary_email_check.addValidator(new CheckValidator(primary_email));
		*/
		/*
		orgunit = new DivRepTextBox(this);
		orgunit.setLabel("Organization Unit");
		orgunit.setRequired(true);
		orgunit.setSampleValue("PKITesting");
		
		orgname = new DivRepTextBox(this);
		orgname.setLabel("Organization Name");
		orgname.setRequired(true);
		orgname.setValue("OSG");
		orgname.setDisabled(true);
		*/
		
		/*
		address_line_1 = new DivRepTextBox(this);
		address_line_1.setLabel("Address Line 1");

		address_line_2 = new DivRepTextBox(this);
		address_line_2.setLabel("Address Line 2");
		*/
		if(auth.isGuest()) {
			new DivRepStaticContent(this, "<h2>Contact Information</h2>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Following information will be used to contact you during the approval process.</p>");
					
			fullname = new DivRepTextBox(this);
			fullname.setLabel("Full Name");
			fullname.setRequired(true);
			if(contact != null) {
				fullname.setValue(contact.name);
			}
			
			email = new DivRepTextBox(this);
			email.setLabel("Email");
			email.setRequired(true);
			if(contact != null) {
				email.setValue(contact.primary_email);
			}
			
			phone = new DivRepTextBox(this);
			phone.setLabel("Phone");
			phone.setRequired(true);
			if(contact != null) {
				phone.setValue(contact.primary_phone);
			}
			
			new DivRepStaticContent(this, "<h2>Profile Information</h2>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Following information will be used to register you as a new OIM user.</p>");
			
			city = new DivRepTextBox(this);
			city.setLabel("City");
			city.setRequired(true);
			if(contact != null) {
				city.setValue(contact.city);
			}
	
			state = new DivRepTextBox(this);
			state.setLabel("State");
			state.setRequired(true);
			if(contact != null) {
				state.setValue(contact.state);
			}
			
			zipcode = new DivRepTextBox(this);
			zipcode.setLabel("Zipcode");
			zipcode.setRequired(true);
	
			country = new DivRepTextBox(this);
			country.setLabel("Country");
			country.setRequired(true);
			if(contact != null) {
				country.setValue(contact.country);
			}
			
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
			timezone.setLabel("Time Zone - Please choose location based timezone such as America/Chicago");
			timezone.setRequired(true);
			
			profile = new DivRepTextArea(this);
			profile.setLabel("Profile");
			profile.setRequired(true);
			profile.setSampleValue("Please enter your role within OSG community, and maybe a small introduction of who you are and what you do.");
		
			use_twiki = new DivRepCheckBox(this);
			use_twiki.setLabel("Use OSG TWiki");
			use_twiki.setValue(false);
			
			twiki_id = new DivRepTextBox(this);
			twiki_id.setLabel("OSG TWiki ID - Generated from your name");
			twiki_id.setDisabled(true);
			fullname.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					if(e.action.equals("change")) {
						ContactModel model = new ContactModel(context);
						try {
							twiki_id.setValue(model.generateTwikiID(e.value, null));
							twiki_id.redraw();	
						} catch (SQLException e1) {
							alert(e1.toString());
						}
					}
				}});
			
			new DivRepStaticContent(this, "<h2>Passphrase</h2>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Please pick a passphrase to retrieve your certificate once issued.</p>");
			if(auth.isGuest()) {
				new DivRepStaticContent(this, "<p class=\"help-block\">This passphrase will also be used to encrypt your certificate.</p>");
			}
			passphrase = new DivRepPassword(this);
			passphrase.addValidator(new DivRepPassStrengthValidator());
			passphrase.setRequired(true);

			/*
			new DivRepReCaptcha(this, 
					StaticConfig.conf.getProperty("recaptcha.public_key"), 
					StaticConfig.conf.getProperty("recaptcha.private_key"),
					context.getRemoteAddr());
			*/
			new DivRepStaticContent(this, "<h2>Captcha</h2>");
			new DivRepSimpleCaptcha(this, context.getSession());
		}
		
		new DivRepStaticContent(this, "<h2>DigiCert Policy Agreement</h2>");
		new DivRepStaticContent(this, "<div class=\"well\">TBD... we display Digicert policy here for user to read..</div>");
		agreement = new DivRepCheckBox(this);
		agreement.setLabel("I agree with above policies");
		agreement.setRequired(true);
		agreement.addValidator(new DivRepIValidator<Boolean>(){

			@Override
			public Boolean isValid(Boolean value) {
				return value;
			}

			@Override
			public String getErrorMessage() {
				return "You must agree to these policies";
			}});
		
		new DivRepStaticContent(this, "<h2>Sponsor</h2>");
		new DivRepStaticContent(this, "<p class=\"help-block\">Please select VO who should approve your request.</p>");
		
		VOModel vo_model = new VOModel(context);
		LinkedHashMap<Integer, String> kv = new LinkedHashMap();
		try {
			ArrayList<VORecord> recs = vo_model.getAll();
			Collections.sort(recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(VORecord vo_rec : recs) {
				kv.put(vo_rec.id, vo_rec.name);
			}
			vo = new DivRepSelectBox(this, kv);
			vo.setLabel("Virtual Organization");
			vo.setRequired(true);
			/*
			vo.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent e) {
					if(e.value != null) {
						Integer vo_id = Integer.parseInt(e.value);
						VOContactModel model = new VOContactModel(context);
						ContactModel cmodel = new ContactModel(context);
						try {
							LinkedHashMap<Integer, String> kv = new LinkedHashMap();
							ArrayList<VOContactRecord> recs = model.getByVOID(vo_id);
							
							for(VOContactRecord rec : recs) {
								if(rec.contact_type_id.equals(11) && rec.contact_rank_id.equals(3)) {
									//tertiary ra contact (sponsor)
									ContactRecord contact = cmodel.get(rec.contact_id);
									kv.put(rec.contact_id, contact.name);
								}
							}
							sponsor.setValues(kv);
						} catch (SQLException e1) {
							alert("Failed to load sponsor for selected VO");
						}
						sponsor.setHidden(false);
					} else {
						sponsor.setHidden(true);
					}
					sponsor.redraw();
				}
				
			});
			
			sponsor = new DivRepSelectBox(this, kv);
			sponsor.setLabel("Sponsor");
			sponsor.setRequired(true);
			sponsor.setHidden(true);
			*/
			
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}

		
		
		
		/*
		///////////////////////////////////////////////////////////////////////////////////////////
		//
		// Extra OIM Profile
		//
		new DivRepStaticContent(this, "<h3>Profile Information</h3>");
		
		secondary_email = new DivRepTextBox(this);
		secondary_email.setLabel("Secondary Email");
		secondary_email.addValidator(new DivRepEmailValidator());

		// TODO Need formatting help here -agopu  -- looks like the phone number used by OSG community is very diverse. I don't know how simple is simple enough
		// validation in our case.. -- hayashis
		primary_phone = new DivRepTextBox(this);
		primary_phone.setLabel("Primary Phone");

		primary_phone_ext = new DivRepTextBox(this);
		primary_phone_ext.setLabel("Primary Phone Extension");
		primary_phone.setRequired(true);

		secondary_phone = new DivRepTextBox(this);
		secondary_phone.setLabel("Secondary Phone");

		secondary_phone_ext = new DivRepTextBox(this);
		secondary_phone_ext.setLabel("Secondary Phone Extension");
		
		sms_address = new DivRepTextBox(this);
		sms_address.setLabel("SMS Address");
		sms_address.setSampleValue("8127771234@txt.att.net");
		sms_address.addValidator(DivRepEmailValidator.getInstance());
		
		contact_preference = new DivRepTextArea(this);
		contact_preference.setLabel("Enter Additional Contact Preferences");
		contact_preference.setSampleValue("Please contact me via phone during the day.");
		
		im = new DivRepTextBox(this);
		im.setLabel("Instant Messaging Information");
		im.setSampleValue("soichih@gtalk");
		
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
		timezone.setLabel("Time Zone - Please choose location based timezone such as America/Chicago");
		timezone.setRequired(true);

		profile = new DivRepTextArea(this);
		profile.setLabel("Profile");
		profile.setRequired(true);
		profile.setSampleValue("Please enter your role within OSG community, and maybe a small introduction of who you are and what you do.");
	
		use_twiki = new DivRepCheckBox(this);
		use_twiki.setLabel("Use OSG TWiki");
		use_twiki.setValue(false);
		
		twiki_id = new DivRepTextBox(this);
		twiki_id.setLabel("OSG TWiki ID - Generated from your name");
		twiki_id.setDisabled(true);
		name.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				if(e.action.equals("change")) {
					ContactModel model = new ContactModel(context);
					try {
						twiki_id.setValue(model.generateTwikiID(e.value, null));
						twiki_id.redraw();	
					} catch (SQLException e1) {
						alert(e1.toString());
					}
				}
			}
		});
		
		*/
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Boolean doSubmit() {
		Boolean ret = true;
	
        /*
		String cn = fullname.getValue() + "/emailAddress=" + email.getValue();
		X500NameBuilder builder = X500NameBuilder();
		x500 = new X500Name(cn,
				"PKITesting", //org unit
				"OSG", //osg name
				city.getValue(), state.getValue(), country.getValue()
		);
		*/

		ContactRecord user;
		if(auth.isUser()) {
			user = auth.getContact();
		} else {
			ContactModel model = new ContactModel(context);
			DNModel dnmodel = new DNModel(context);
			try {
				//Find contact record with the same email address
				ContactRecord rec = model.getByemail(email.getValue());
				//Create new one if none is found
				if(rec == null) {
					rec = new ContactRecord();
					rec.name = fullname.getValue();
					rec.primary_email = email.getValue();
					rec.primary_phone = phone.getValue();
					rec.city = city.getValue();
					rec.state = state.getValue();
					rec.zipcode = zipcode.getValue();
					rec.country = country.getValue();
					rec.timezone = timezone_id2tz.get(timezone.getValue());
					rec.profile = profile.getValue();
					rec.use_twiki = use_twiki.getValue();
					rec.twiki_id = twiki_id.getValue();
					rec.person = true;
					rec.disable = true; //don't enable until the request gets approved
					rec.id = model.insert(rec);
					user = rec;
				} else {
					//Make sure that this contact is not used by any DN already
					if(dnmodel.getByContactID(rec.id) != null) {
						alert("The email address specified is already associated with a different DN. Please try different email address.");
						return false;
					} 						
					user = rec;
				}
			} catch (SQLException e) {
				alert("Sorry, we couldn't register your contact information to OIM DB..");
				log.error(e);
				return false;
			}
		} 
	
		//do certificate request
		try {
			UserCertificateRequestModel certmodel = new UserCertificateRequestModel(context);
			String requester_passphrase = null;
			if(auth.isGuest()) {
				requester_passphrase = HashHelper.sha1(passphrase.getValue());
			} 
			ret = certmodel.request(vo.getValue(), user, requester_passphrase);
		} catch (Exception e) {
			log.error("Failed to submit request..", e);
			ret = false;
		}

		if(!ret) {
			alert("Sorry, failed to submit request..");
		}

		/*
		ContactModel model = new ContactModel(context);
		DNModel dnmodel = new DNModel(context);
		try {
			//Find contact record with the same email address
			ContactRecord rec = model.getByemail(primary_email.getValue());
			//Create new one if none is found
			if(rec == null) {
				rec = new ContactRecord();
				rec.name = name.getValue();
				rec.primary_email = primary_email.getValue();
				rec.secondary_email = secondary_email.getValue();
				rec.primary_phone = primary_phone.getValue();
				rec.primary_phone_ext = primary_phone_ext.getValue();
				rec.secondary_phone = secondary_phone.getValue();
				rec.secondary_phone_ext = secondary_phone_ext.getValue();
				rec.sms_address = sms_address.getValue();
				rec.address_line_1 = address_line_1.getValue(); 
				rec.address_line_2 = address_line_2.getValue(); 
				rec.city = city.getValue();
				rec.state = state.getValue();
				rec.zipcode = zipcode.getValue();
				rec.country = country.getValue();
				rec.im = im.getValue();
				rec.timezone = timezone_id2tz.get(timezone.getValue());
				rec.profile = profile.getValue();
				rec.contact_preference = profile.getValue();
				rec.use_twiki = use_twiki.getValue();
				rec.twiki_id = twiki_id.getValue();
				rec.person = true;
				rec.disable = false;
				model.insert(rec);
			} else {
				//Make sure that this contact is not used by any DN already
				if(dnmodel.getByContactID(rec.id) != null) {
					alert("The email address specified is already associated with a different DN. Please try different email address.");
					//primary_email.setValue("");
					//primary_email_check.setValue("");

					context.close();
					return false;
				}
			}
			
	
			//jump to profile page for more details
			redirect(StaticConfig.getApplicationBase()+"/profileedit");
			
		} catch (SQLException e) {
			alert(e.toString());
			//redirect(origin_url);
			ret = false;
		}
		*/
		
		//context.storeDivRepSession();
		return ret;
	}
}