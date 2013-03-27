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
import com.divrep.common.DivRepToggler;
import com.divrep.validator.DivRepEmailValidator;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

import edu.iu.grid.oim.view.divrep.CNEditor;
import edu.iu.grid.oim.view.divrep.DivRepSimpleCaptcha;
import edu.iu.grid.oim.view.divrep.form.validator.CNValidator;
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
	private DivRepSelectBox sponsor;
	
	private SponsorManual sponsor_manual;//used when manually specified
	
	private DivRepTextArea request_comment;
	
	private DivRepTextArea profile;
	private DivRepCheckBox use_twiki;
	private DivRepTextBox twiki_id;
	
	private DivRepPassword passphrase; //for guest
	private DivRepPassword passphrase_confirm; //for guest
	private DivRepCheckBox agreement;
	
	private DivRepSelectBox vo;
	
	class SponsorManual extends DivRepFormElement {
		
		//sponsor info manually selected
		protected DivRepTextBox sponsor_fullname;
		protected DivRepTextBox sponsor_email;
		//protected DivRepTextBox sponsor_phone;
		
		public SponsorManual(DivRep parent) {
			super(parent);
			
			sponsor_fullname = new DivRepTextBox(this);
			sponsor_fullname.setLabel("Full Name");
			sponsor_fullname.setRequired(true);
			sponsor_fullname.addValidator(new CNValidator());
			/*
			sponsor_phone = new DivRepTextBox(this);
			sponsor_phone.setLabel("Phone");
			sponsor_phone.setRequired(true);
			*/

			sponsor_email = new DivRepTextBox(this);
			sponsor_email.setLabel("Email");
			sponsor_email.setRequired(true);
			sponsor_email.addValidator(new DivRepEmailValidator());
		}
		@Override
		protected void onEvent(DivRepEvent event) {
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			if(!isHidden()) {
				//out.write("<div class=\"well well-small\">");
				out.write("<h3>Sponsor Detail</h3>");
				sponsor_fullname.render(out);
				//sponsor_phone.render(out);
				sponsor_email.render(out);
				//out.write("</div>");
			}
			out.write("</div>");
		}
		
		@Override
		public void setRequired(Boolean b) {
			sponsor_fullname.setRequired(b);
			//sponsor_phone.setRequired(b);
			sponsor_email.setRequired(b);
		}
		public ContactRecord getRecord() {
			ContactRecord rec = new ContactRecord();
			rec.name = sponsor_fullname.getValue();
			rec.primary_email = sponsor_email.getValue();
			//rec.primary_phone = sponsor_phone.getValue();
			return rec;
		}
 	}
	
	
	public CertificateRequestUserForm(final UserContext context, String origin_url) {
		
		super(context.getPageRoot(), origin_url);
		this.context = context;
		auth = context.getAuthorization();
		//ContactRecord contact = auth.getContact();
		
		new DivRepStaticContent(this, "<h2>User Certificate Request</h2>");
	
		if(!auth.isUser()) {
			//new DivRepStaticContent(this, "<div class=\"alert\">This is a public certificate request form. If you are already an OIM user, please login first.</div>");
			new DivRepStaticContent(this, "<h3>Contact Information</h3>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Following information will be used to issue your new user certificate, and also used to contact you during the approval process.</p>");
					
			fullname = new DivRepTextBox(this);
			fullname.setLabel("Full Name");
			fullname.setRequired(true);
			fullname.addValidator(new CNValidator());

			phone = new DivRepTextBox(this);
			phone.setLabel("Phone");
			phone.setRequired(true);

			email = new DivRepTextBox(this);
			email.setLabel("Email");
			email.setRequired(true);
			email.addValidator(new DivRepEmailValidator());
			
			new DivRepStaticContent(this, "<h3>Profile Information</h3>");
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
				}
			});
			
			new DivRepStaticContent(this, "<h3>Choose a password</h3>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Please choose a password to issue your certificate and encrypt your private key.</p>");
			if(!auth.isUser()) {
				new DivRepStaticContent(this, "<p class=\"help-block alert alert-error\"><b>IMPORTANT</b>: If you forget this password, you will not be able to issue your certificate and import it your browser after it is approved.</p>");
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
				}
			});
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
				}
			});
			passphrase_confirm.setRequired(true);
		
			new DivRepStaticContent(this, "<h3>Captcha</h3>");
			new DivRepSimpleCaptcha(this, context.getSession());
		} else {
			//OIM user can specify CN
			new DivRepStaticContent(this, "<h3>DN</h3>");
			cn = new CNEditor(this);
			cn.setRequired(true);
			if(auth.isUser()) {
				ContactRecord contact = auth.getContact();
				cn.setValue(contact.name + " " + contact.id);
			}
		}
			
		new DivRepStaticContent(this, "<h3>Sponsor</h3>");
		new DivRepStaticContent(this, "<p class=\"help-block\">Please select VO that should approve your request.</p>");
		DivRepToggler help = new DivRepToggler(this) {
			@Override
			public DivRep createContent() {
				return new DivRepStaticContent(this, 
					"<div class=\"well\">"+
					"<h4>Virtual Organization</h4>" +
					"<p class=\"\">If you do not know which VO to select, please open a <a target=\"_blank\" href=\"https://ticket.grid.iu.edu\">GOC Ticket</a> for an assistance.</p>" +
					"<p class=\"\">If your VO does not appear, it currently has no RA agents assigned to it. Please contact GOC or the VO managers.</p>" +
					"<p class=\"\">If you just need to access OSG secure web servers (OIM, DocDB, etc.), you may select the <b>MIS</b> VO.</p>" +
					"<h4>What is a sponsor?</h4>" +
					"A sponsor is a member of the VO who knows you and can vouch for your membership such as PI or supervisor." +
					"</div>");
			}
		};
		help.setShowHtml("<u class=\"pull-right\">Help me choose</u>");
		help.setHideHtml("");

		VOModel vo_model = new VOModel(context);
		VOContactModel model = new VOContactModel(context);
		LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
		try {
			ArrayList<VORecord> recs = vo_model.getAll();
			Collections.sort(recs, new Comparator<VORecord> () {
				public int compare(VORecord a, VORecord b) {
					return a.getName().compareToIgnoreCase(b.getName());
				}
			});
			for(VORecord vo_rec : recs) {
				//check if the VO has at least 1 RA specified
				ArrayList<VOContactRecord> crecs = model.getByVOID(vo_rec.id);
				boolean hasra = false;
				for(VOContactRecord crec : crecs) {
					if(crec.contact_type_id.equals(11)) { //RA
						hasra = true;
						break;
					}
				}
				if(hasra) {
					keyvalues.put(vo_rec.id, vo_rec.name);
				}
			}
			vo = new DivRepSelectBox(this, keyvalues);
			vo.setLabel("Virtual Organization");
			vo.setRequired(true);
			vo.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent event) {
					//lookup sponsor and if there is one - then require user to select one
					try {
						if(vo.getValue() == null) {
							sponsor.setHidden(true);
							sponsor_manual.setHidden(true);
							sponsor_manual.setRequired(false);
						} else {
							sponsor.setHidden(true);
							ArrayList<ContactRecord> sponsors = getSponsor(vo.getValue());
							Collections.sort(sponsors, new Comparator<ContactRecord> () {
								public int compare(ContactRecord a, ContactRecord b) {
									return a.name.compareToIgnoreCase(b.name);
								}
							});
							LinkedHashMap<Integer, String> keyvalues = new LinkedHashMap();
							for(ContactRecord sponsor : sponsors) {
								if(auth.isUser()) {
									keyvalues.put(sponsor.id, sponsor.name + " <" + sponsor.primary_email + ">");
								} else {
									keyvalues.put(sponsor.id, sponsor.name);
								}
							}
							sponsor.setValues(keyvalues);
							sponsor.setHidden(false);	
							if(sponsors.size() > 0) {
								//select 1st value
								Integer first_id = sponsors.get(0).id;
								sponsor.setValue(first_id);
								sponsor.setHidden(false);
								
								//no need for sponsor detail
								sponsor_manual.setHidden(true);
								sponsor_manual.setRequired(false);
							} else {
								sponsor.setHidden(true);
								sponsor_manual.setHidden(false);
								sponsor_manual.setRequired(true);
							}
						}
						sponsor.redraw();
						sponsor_manual.redraw();
					} catch (NumberFormatException e) {
						log.error("Failed to parse vo ID for "+ event.value, e);
					} catch (SQLException e) {
						log.error("Failed to lookup vo  for ID:"+ event.value, e);
					}
				}
			});
		} catch (SQLException e) {
			log.error("Failed to load vo list while constructing certificat request form", e);
		}
		
		sponsor = new DivRepSelectBox(this);
		sponsor.setLabel("Sponsor");
		sponsor.setNullLabel("(Manually Specify)");
		sponsor.setHidden(true);
		sponsor.addEventListener(new DivRepEventListener() {
			@Override
			public void handleEvent(DivRepEvent event) {
				if(sponsor.getValue() == null) {
					sponsor_manual.setHidden(false);
					sponsor_manual.setRequired(true);
				} else {
					sponsor_manual.setHidden(true);
					sponsor_manual.setRequired(false);
				}
				
				sponsor_manual.redraw();
			}
		});
		
		sponsor_manual = new SponsorManual(this);
		sponsor_manual.setHidden(true);
		sponsor_manual.setRequired(false);
		
		request_comment = new DivRepTextArea(this);
		request_comment.setLabel("Comments");
		request_comment.setSampleValue("Please enter any comments, or request you'd like to make for RA agents / Sponsors.");
		
		new DivRepStaticContent(this, "<h2>OSG Policy Agreement</h2>");
		
		//agreement doc comes from https://twiki.grid.iu.edu/twiki/pub/Operations/DigiCertAgreements/IGTF_Certificate_Subscriber_Agreement_-_Mar_26_2012.doc
		InputStream aup_stream =getClass().getResourceAsStream("osg.certificate.agreement.html");
		StringBuilder aup = ResourceReader.loadContent(aup_stream);
		new DivRepStaticContent(this, aup.toString());
		
		agreement = new DivRepCheckBox(this);
		agreement.setLabel("I AGREE");
		agreement.setRequired(true);
		agreement.addValidator(new DivRepIValidator<Boolean>() {
			@Override
			public Boolean isValid(Boolean value) {
				return value;
			}

			@Override
			public String getErrorMessage() {
				return "You must agree to these policies";
			}
		});
	}
	
	protected ArrayList<ContactRecord> getSponsor(Integer vo_id) throws SQLException {
		VOContactModel model = new VOContactModel(context);
		ContactModel cmodel = new ContactModel(context);
		ArrayList<ContactRecord> sponsors = new ArrayList<ContactRecord>();
		ArrayList<VOContactRecord> crecs = model.getByVOID(vo_id);
		for(VOContactRecord crec : crecs) {
			if(crec.contact_type_id.equals(12)) { //sponsors
				ContactRecord sponsor = cmodel.get(crec.contact_id);
				sponsors.add(sponsor);
			}
		}
		return sponsors;
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
		} 
		
		//sponsor
		ContactRecord sponsor_rec = null;
		if(sponsor.getValue() != null) {
			ContactModel cmodel = new ContactModel(context);
			try {
				sponsor_rec = cmodel.get(sponsor.getValue());
			} catch (SQLException e) {
				log.error("Failed to lookup contact with id: " + sponsor.getValue());
				alert(e.getMessage());
				return false;
			}
		} else {
			//sponsor not selected from drop down - use manual sponsor
			sponsor_rec = sponsor_manual.getRecord();
		}
	
		//do certificate request with no csr
		try {
			CertificateRequestUserModel certmodel = new CertificateRequestUserModel(context);
			CertificateRequestUserRecord rec = null;
			if(!auth.isUser()) {
				//requester_passphrase = HashHelper.sha1(passphrase.getValue());
				rec = certmodel.requestGuestWithNOCSR(vo.getValue(), user, sponsor_rec, passphrase.getValue(), request_comment.getValue());
			} else {
				rec = certmodel.requestUsertWithNOCSR(vo.getValue(), user, sponsor_rec, cn.getValue(), request_comment.getValue());
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
