package edu.iu.grid.oim.view.divrep.form;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepToggler;
import com.divrep.validator.DivRepEmailValidator;
import com.divrep.validator.DivRepIntegerValidator;
import com.divrep.validator.DivRepUniqueValidator;
import com.divrep.validator.DivRepUrlValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.AuthorizationTypeModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.AuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.divrep.Confirmation;

//alter table contact add column timezone varchar(16) default value "UTC";

public class ContactFormDE extends DivRepForm 
{
    static Logger log = Logger.getLogger(ContactFormDE.class); 
	
    private UserContext context;
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
	private DivRepCheckBox use_twiki;
	private DivRepTextBox twiki_id;
	
	private DivRepTextBox count_hostcert_day;
	private DivRepTextBox count_hostcert_year;
	private DivRepTextBox count_usercert_year;
	
	private PersonalInfo personal_info;
	
	class PersonalInfo extends DivRepFormElement
	{
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}	
		
		PersonalInfo(DivRep _parent, final ContactRecord rec, final ArrayList<DNRecord> associated_dn_recs) {
			super(_parent);
					
			new DivRep(this) {
				Integer user_dnid = auth.getDNID();
				
				@Override
				public void render(PrintWriter out) {
					out.write("<div id=\""+getNodeID()+"\" class=\"divrep_form_element\">");
					if(associated_dn_recs != null) {
						out.write("<label>Associated DNs</label><br>");
						
						if(associated_dn_recs.size() == 0) {
							out.write("<p class=\"muted\">No DNs are associated with this contact.</p>");
						} else {
							DNAuthorizationTypeModel dnauthtypemodel = new DNAuthorizationTypeModel(context);
							AuthorizationTypeModel authtypemodel = new AuthorizationTypeModel(context); 
							
							for(DNRecord dn : associated_dn_recs) {
								String active = "";
								String tag = "";
								if(user_dnid != null && user_dnid.equals(dn.id)) {
									active = " well-active";
									tag += "<span style=\"float: right;\" class=\"label label-info\">You are currently logged in with this DN</span>";
								}
								if(dn.disable) {
									active = " well-disabled";
									tag += "<span style=\"float: right;\" class=\"label\">Disabled DN</span>";
								}
								out.write("<div class=\"well well-small"+active+"\">");
								out.write(tag);
								out.write("<h4>"+StringEscapeUtils.escapeHtml(dn.dn_string)+"</h4>");
								
								//pull all authentication types for this DN
								try {
									Collection<Integer> authtype_ids = dnauthtypemodel.getAuthorizationTypesByDNID(dn.id);
									//out.write("<h3>Authorizations</h3>");
									out.write("<ul>");
									for (Integer authtype_id : authtype_ids) {
										AuthorizationTypeRecord authrec = authtypemodel.get(authtype_id);
										out.write("<li>"+authrec.name+"</li>");
									}
									out.write("</ul>");
								} catch (SQLException e) {
									log.error("Failed to lookup authentication type info", e);
								}
								out.write("</div>");
							}
						} 
					} //end of associated_dn area
					//out.write("<sub>* Can only be modified by GOC Staff on request using Admin interface</sub>");
					out.write("</div>");
				}

				@Override
				protected void onEvent(DivRepEvent e) {
					// TODO Auto-generated method stub
					
				}
				
			};

			
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
			
			use_twiki = new DivRepCheckBox(this);
			use_twiki.setLabel("Use OSG TWiki");
			use_twiki.setValue(rec.use_twiki);
			
			twiki_id = new DivRepTextBox(this);
			twiki_id.setLabel("OSG TWiki ID - Only GOC support staff can edit this");
			twiki_id.setValue(rec.twiki_id);
			//twiki_id.addClass("divrep_indent");
			if(auth.allows("admin")) {
				//admin can edit - so validate
				ArrayList<String> values = new ArrayList<String>();
				ContactModel model = new ContactModel(context);
				try {
					for(ContactRecord crec : model.getAll()) {
						if(rec == crec) continue;//allow using myown!
						if(crec.disable) continue;//ignore disabled contact
						values.add(crec.twiki_id);
					}
				} catch (SQLException e2) {
					log.error("Failed to load existing twiki IDs", e2);
				}
				twiki_id.addValidator(new DivRepUniqueValidator<String>(values));
				
			} else {
				//non-admin can't edit this field
				twiki_id.setDisabled(true);
			}
			if(rec.id == null) {
				//generate new TWiki ID if this is for new contact record
				name.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						if(e.action.equals("change")) {
							ContactModel model = new ContactModel(context);
							try {
								twiki_id.setValue(model.generateTwikiID(e.value, rec));
								if(!personal_info.isHidden()) {
									twiki_id.redraw();
								}	
							} catch (SQLException e1) {
								log.error("Failed to generate twiki ID", e1);
							}
						}
					}
				});
			}
		}
		
		public void render(PrintWriter out) {
			out.print("<div class=\"indent\" id=\""+getNodeID()+"\">");	
			if(!isHidden()) {
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
		/*
		public Boolean isValid() { 
			url.validate();
			return url.getValid();
		}
		*/
		public void setLabel(String label) {
			url.setLabel(label);
		}

		@Override
		public void render(PrintWriter out) {
			String img = url.getValue();
			if(img == null || img.length() == 0) {
				img = "images/noavatar.gif";
			}
			
			out.print("<div ");
			renderClass(out);
			out.write("id=\""+getNodeID()+"\">");
			if(!isHidden()) {
				url.render(out);
				out.write("<img class=\"avatar_preview\" src=\""+img+"\"/>");
			}
			out.write("</div>");
		}
	}
	
	public void showHidePersonalDetail()
	{
		Boolean required = person.getValue();
		city.setRequired(required);
		state.setRequired(required);
		zipcode.setRequired(required);
		country.setRequired(required);

		personal_info.setHidden(!person.getValue());
		personal_info.redraw();
	}
	
	public ContactFormDE(UserContext _context, final ContactRecord rec, String origin_url)
	{	
		super(_context.getPageRoot(), origin_url);
		context = _context;
		auth = context.getAuthorization();
		id = rec.id;

		ArrayList<DNRecord> associated_dn_recs = null;
		if (id != null) {
			try {
				DNModel dnmodel = new DNModel(context);;
				associated_dn_recs = dnmodel.getByContactID(id);
			} catch (SQLException e) {
				log.error(e);
			}
		}

		new DivRepStaticContent(this, "<h2>Contact Information</h2>");
	
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
		person.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				showHidePersonalDetail();
			}}
		);
		if (associated_dn_recs != null && associated_dn_recs.size() > 0) {
			person.setValue(true);
			person.setLabel("Personal Contact");
			person.setDisabled(true);
			new DivRepStaticContent(this, "<p>* Always checked for users whose DN is registered with OIM</p>");
		}
		
		personal_info = new PersonalInfo(this, rec, associated_dn_recs);
		if(rec.person == null || rec.person == false) {
			showHidePersonalDetail();
		}
		
		new DivRepStaticContent(this, "<h2>Confirmation</h2>");
		confirmation = new Confirmation(this, rec, auth);
		
		ContactRecord user = auth.getContact();
		if(rec.id != null) {
			DivRepToggler toggler = new DivRepToggler(this) {
				public DivRep createContent() {
					//ContactAssociationView is GenericView, so I have to wrap it with DivRep object
					return new DivRep(this) {
						ContactAssociationView view;
						@Override
						public void render(PrintWriter out) {
							out.write("<div id=\""+getNodeID()+"\">");
							out.write("<h2>Contact Association</h2>");
							try {
								view = new ContactAssociationView(context, rec.id);
								view.render(out);
							} catch (SQLException e) {
								out.write(e.toString());
							}
							out.write("</div>");
						}

						@Override
						protected void onEvent(DivRepEvent e) {
							// TODO Auto-generated method stub
							
						}
					};
				}
			};
			toggler.setShowHtml("<button class=\"btn\">Show Contact Association</button>");
			toggler.setHideHtml("");
		}

		new DivRepStaticContent(this, "<h2>Administrative</h2>");
		new DivRepStaticContent(this, "<p>* Only GOC staff can modify the following information</p>");
		
		//create DN selector
		DNModel dnmodel = new DNModel(context);
		LinkedHashMap<Integer, String> dns = new LinkedHashMap();
		try {
			ArrayList<DNRecord> dnrecs = dnmodel.getAll();
			for(DNRecord dnrec : dnrecs) {
				dns.put(dnrec.id, dnrec.dn_string);
			}
		} catch (SQLException e) {
			log.error(e);
		}
		submitter_dn = new DivRepSelectBox(this, dns);
		submitter_dn.addClass("dn_selecter");
		submitter_dn.setLabel("Submitter DN");
		submitter_dn.setValue(rec.submitter_dn_id);

		disable = new DivRepCheckBox(this);
		disable.setLabel("Disable");
		disable.setValue(rec.disable);
		
		//disable admin controllers
		if(!auth.allows("admin")) {
			submitter_dn.setDisabled(true);
			disable.setDisabled(true);
		}

		ArrayList<DNRecord> dnrecs = new ArrayList<DNRecord>();
		if(rec.id != null) {
			try {
				dnrecs = dnmodel.getByContactID(rec.id);
			} catch (SQLException e1) {
				log.error("Failed to load user's dns");
			}
		}
		if(dnrecs.size() == 0) {
			//don't show
			count_usercert_year = new DivRepTextBox(this);
			count_usercert_year.setHidden(true);
			
			count_hostcert_year = new DivRepTextBox(this);
			count_hostcert_year.setHidden(true);
			
			count_hostcert_day = new DivRepTextBox(this);
			count_hostcert_day.setHidden(true);
		} else {
			new DivRepStaticContent(this, "<h2>Certificate Request Quota</h2>");
			new DivRepStaticContent(this, "<p>* Only PKI staff can update this information</p>");
	
			ConfigModel config = new ConfigModel(context);
			String usercert_max_year = config.QuotaUserCertYearMax.getString();
			String hostcert_max_year = config.QuotaUserHostYearMax.getString();
			String hostcert_max_day = config.QuotaUserHostDayMax.getString();
			
			count_usercert_year = new DivRepTextBox(this);
			count_usercert_year.setLabel("User Certificate Request Count (This Year)");
			count_usercert_year.setRequired(true);
			count_usercert_year.addValidator(new DivRepIntegerValidator());
			new DivRepStaticContent(this, "<p>* You can request up to <span class=\"label label-info\">"+usercert_max_year+"</span> user certificates per year</p>");
		
			count_hostcert_year = new DivRepTextBox(this);
			count_hostcert_year.setLabel("Host Certificate Approval Count (This Year)");
			count_hostcert_year.setRequired(true);
			count_hostcert_year.addValidator(new DivRepIntegerValidator());
			new DivRepStaticContent(this, "<p>* You can approve up to <span class=\"label label-info\">"+hostcert_max_year+"</span> host certificates per year</p>");
	
			count_hostcert_day = new DivRepTextBox(this);
			count_hostcert_day.setLabel("Host Certificate Approval Count (Today)");
			count_hostcert_day.setRequired(true);
			count_hostcert_day.addValidator(new DivRepIntegerValidator());
			new DivRepStaticContent(this, "<p>* You can approve up to <span class=\"label label-info\">"+hostcert_max_day+"</span> host certificates per day</p>");
			
		}
		
		//disable pki quota controllers
		if(!auth.allows("admin_pki_config")) {
			count_hostcert_day.setDisabled(true);
			count_hostcert_year.setDisabled(true);
			count_usercert_year.setDisabled(true);
		}
		if(rec.id == null) {
			//set to 0
			count_usercert_year.setValue("0");
			count_hostcert_day.setValue("0");
			count_hostcert_year.setValue("0");
		} else {
			count_usercert_year.setValue(String.valueOf(rec.count_usercert_year));
			count_hostcert_day.setValue(String.valueOf(rec.count_hostcert_day));
			count_hostcert_year.setValue(String.valueOf(rec.count_hostcert_year));
		}
	}

	protected Boolean doSubmit() 
	{
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
		rec.use_twiki = use_twiki.getValue();
		rec.twiki_id = twiki_id.getValue();
		rec.count_hostcert_day = Integer.parseInt(count_hostcert_day.getValue());
		rec.count_hostcert_year = Integer.parseInt(count_hostcert_year.getValue());
		rec.count_usercert_year = Integer.parseInt(count_usercert_year.getValue());
		
		ContactModel model = new ContactModel(context);
		try {
			if(rec.id == null) {
				model.insert(rec);
				context.message(MessageType.SUCCESS, "Successfully registered new contact.");
			} else {
				ContactRecord old = model.get(rec);
				model.update(old, rec);
				context.message(MessageType.SUCCESS, "Successfully updated a contact");
			}
			return true;
		} catch (Exception e) {
			alert(e.getMessage());
		}
		return false;
	}
}
