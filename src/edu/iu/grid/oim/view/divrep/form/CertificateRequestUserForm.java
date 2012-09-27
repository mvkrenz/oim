package edu.iu.grid.oim.view.divrep.form;

import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.divrep.CNEditor;
import edu.iu.grid.oim.view.divrep.DivRepSimpleCaptcha;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class CertificateRequestUserForm extends DivRepForm
{
    static Logger log = Logger.getLogger(CertificateRequestUserForm.class);
	private UserContext context;
	private Authorization auth;
	
	//subscriber identify
	private DivRepTextBox fullname;
	private DivRepTextBox email;
	private DivRepTextBox phone;
	private DivRepTextBox city, state, country, zipcode;
	private DivRepSelectBox timezone;
	private HashMap<Integer, String> timezone_id2tz;
	
	private CNEditor cn; //only for OIM user
	
	private DivRepTextArea profile;
	private DivRepCheckBox use_twiki;
	private DivRepTextBox twiki_id;
	
	private DivRepPassword passphrase; //for guest
	private DivRepPassword passphrase_confirm; //for guest
	private DivRepCheckBox agreement;
	
	private DivRepSelectBox vo;//, sponsor;
	
	
	public CertificateRequestUserForm(final UserContext context, String origin_url) {
		
		super(context.getPageRoot(), origin_url);
		this.context = context;
		auth = context.getAuthorization();
		//ContactRecord contact = auth.getContact();
	
		if(!auth.isUser()) {
			new DivRepStaticContent(this, "<div class=\"alert\">This is a public certificate request form. If you are already an OIM user, please login first.</div>");
			new DivRepStaticContent(this, "<h2>Contact Information</h2>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Following information will be used to contact you during the approval process.</p>");
					
			fullname = new DivRepTextBox(this);
			fullname.setLabel("Full Name");
			fullname.setRequired(true);
			/*
			if(contact != null) {
				fullname.setValue(contact.name);
			}
			*/
			
			email = new DivRepTextBox(this);
			email.setLabel("Email");
			email.setRequired(true);
			/*
			if(contact != null) {
				email.setValue(contact.primary_email);
			}
			*/
			
			phone = new DivRepTextBox(this);
			phone.setLabel("Phone");
			phone.setRequired(true);
			/*
			if(contact != null) {
				phone.setValue(contact.primary_phone);
			}
			*/
			
			new DivRepStaticContent(this, "<h2>Profile Information</h2>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Following information will be used to register you as a new OIM user.</p>");
			
			city = new DivRepTextBox(this);
			city.setLabel("City");
			city.setRequired(true);
			/*
			if(contact != null) {
				city.setValue(contact.city);
			}
			*/
	
			state = new DivRepTextBox(this);
			state.setLabel("State");
			state.setRequired(true);
			/*
			if(contact != null) {
				state.setValue(contact.state);
			}
			*/
			
			zipcode = new DivRepTextBox(this);
			zipcode.setLabel("Zipcode");
			zipcode.setRequired(true);
	
			country = new DivRepTextBox(this);
			country.setLabel("Country");
			country.setRequired(true);
			/*
			if(contact != null) {
				country.setValue(contact.country);
			}
			*/
			
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
			profile.setLabel("Profile - Introduce yourself to the rest of OSG community.");
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
			
			new DivRepStaticContent(this, "<h2>Choose a password</h2>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Please choose a password to retrieve your certificate once it's issued.</p>");
			if(!auth.isUser()) {
				new DivRepStaticContent(this, "<p class=\"help-block\">This password will also be used to encrypt your certificate.</p>");
			}
			passphrase = new DivRepPassword(this);
			passphrase.addValidator(new PKIPassStrengthValidator());
			passphrase.setRequired(true);
			passphrase.addEventListener(new DivRepEventListener() {

				@Override
				public void handleEvent(DivRepEvent e) {
					if(passphrase_confirm.getValue() != null) {
						passphrase_confirm.validate();
					}
				}});
			passphrase_confirm = new DivRepPassword(this);
			passphrase_confirm.setLabel("Re-enter password");
			passphrase_confirm.addValidator(new DivRepIValidator<String>() {
				String message;
				@Override
				public Boolean isValid(String value) {
					if(value.equals(passphrase.getValue())) return true;
					message = "Passphrase does not match";
					return false;
				}

				@Override
				public String getErrorMessage() {
					return message;
				}});
			passphrase_confirm.setRequired(true);
		
			new DivRepStaticContent(this, "<h2>Captcha</h2>");
			new DivRepSimpleCaptcha(this, context.getSession());
		} else {
			//OIM user can specify CN
			new DivRepStaticContent(this, "<h2>DN</h2>");
			cn = new CNEditor(this);
			cn.setRequired(true);
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				cn.setValue(contact.name + " " + contact.id);
			}
		}
			
		new DivRepStaticContent(this, "<h2>Sponsor</h2>");
		new DivRepStaticContent(this, "<p class=\"help-block\">Please select VO that should approve your request.</p>");
		new DivRepStaticContent(this, "<p class=\"muted\">If you do not know which VO to select, please open a <a href=\"https://ticket.grid.iu.edu\">GOC Ticket</a> for an assistance.</p>");
		
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
			
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}
		
		new DivRepStaticContent(this, "<h2>OSG Policy Agreement</h2>");
		
		//agreement doc comes from https://twiki.grid.iu.edu/twiki/pub/Operations/DigiCertAgreements/IGTF_Certificate_Subscriber_Agreement_-_Mar_26_2012.doc
		InputStream aup_stream =getClass().getResourceAsStream("osg.certificate.agreement.html");
		StringBuilder aup = ResourceReader.loadContent(aup_stream);
		new DivRepStaticContent(this, aup.toString());
		
		agreement = new DivRepCheckBox(this);
		agreement.setLabel("I AGREE");
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
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Boolean doSubmit() {
		ContactRecord user;
		if(auth.isUser()) {
			user = auth.getContact();
		} else {
			//create new contact record (may or may not be registered)
			user = new ContactRecord();
			user.name = fullname.getValue();
			user.primary_email = email.getValue();
			user.primary_phone = phone.getValue();
			user.city = city.getValue();
			user.state = state.getValue();
			user.zipcode = zipcode.getValue();
			user.country = country.getValue();
			user.timezone = timezone_id2tz.get(timezone.getValue());
			user.profile = profile.getValue();
			user.use_twiki = use_twiki.getValue();
			user.twiki_id = twiki_id.getValue();
			user.person = true;
			//user.disable = true will be set later - I believe it interfares with some lookup.
			
			user.count_hostcert_day = 0;
			user.count_hostcert_year = 0;
			user.count_usercert_year = 0;
			
			/*
			try {
				ContactModel model = new ContactModel(context);
				DNModel dnmodel = new DNModel(context);
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
					
					rec.count_hostcert_day = 0;
					rec.count_hostcert_year = 0;
					rec.count_usercert_year = 0;
					
					//request() will register if 
					//rec.id = model.insert(rec);
					user = rec;
				} else {
					//Make sure that this contact is not used by any DN already
					if(dnmodel.getByContactID(rec.id) != null) {
						alert("The email address specified is already associated with a different DN. Please try different email address.");
						return false;
					} else {
						//contact exist, but not yet associated with any DN (not approved?)
					}
					user = rec;
				}
			} catch (SQLException e) {
				alert("Sorry, we couldn't register your contact information to OIM DB..");
				log.error(e);
				return false;
			}
			*/
		} 
	
		//do certificate request with no csr
		try {
			CertificateRequestUserModel certmodel = new CertificateRequestUserModel(context);
			CertificateRequestUserRecord rec = null;
			if(!auth.isUser()) {
				//requester_passphrase = HashHelper.sha1(passphrase.getValue());
				rec = certmodel.requestGuestWithNOCSR(vo.getValue(), user, passphrase.getValue());
			} else {
				rec = certmodel.requestUsertWithNOCSR(vo.getValue(), user, cn.getValue());
			}
			if(rec != null) {
				redirect("certificateuser?id="+rec.id); //TODO - does this work? I haven't tested it
			}
			context.message(MessageType.SUCCESS, "Successfully requested new user certificate. Your RA will contact you to process your request.");
			return true;
		} catch (CertificateRequestException e) {
			log.warn("User failed to submit request", e);
			alert(e.getMessage());
			return false;
		} catch (Exception e) {
			log.error("Failed to submit request..", e);
			alert("Sorry, failed to submit request: " + e.toString());
			return false;
		}
	}
}
