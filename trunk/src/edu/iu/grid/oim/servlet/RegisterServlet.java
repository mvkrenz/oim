package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepEmailValidator;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.SideContentView;

public class RegisterServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(RegisterServlet.class);  
    private String origin_url = "home";
   
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		if(!auth.isUnregistered()) {
			//user don't meet the requirement to register. send it to home
			response.sendRedirect("home");
			context.message(MessageType.INFO, "Your current DN: "+auth.getUserDN()+" is already registered.");
			return;
		}
		
		/*//sample
		context.message(MessageType.SUCCESS, "This is a success message");
		context.message(MessageType.ERROR, "This is a error message");
		context.message(MessageType.INFO, "This is a info message");
		context.message(MessageType.WARNING, "This is a warning message");
		*/
		
		BootMenuView menuview = new BootMenuView(context, "register");
		ContentView contentview = createContentView(context);
		BootPage page = new BootPage(context, menuview, contentview, null);
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView(UserContext context)
	{
		ContentView contentview = new ContentView(context);
		
		contentview.add(new HtmlView("<p class=\"alert\">If you already have an OIM account and wish to associate another DN to your existing account, please open a <a href=\"https://ticket.grid.iu.edu\">GOC ticket</a> requesting for contact DN association.</p>"));		
		
		contentview.add(new HtmlView("<h2>OIM Registration</h2>"));
		//contentview.add(new HtmlView("<p>Your X509 certificate is not registered on OIM. </p>"));
		contentview.add(new HtmlView("<p>You can register your certificate and gain access to OIM by submitting following form.</p>"));			
		RegistraitonForm form = new RegistraitonForm(context);
		contentview.add(new DivRepWrapper(form));
	
		
		return contentview;
	}

	class RegistraitonForm extends DivRepForm
 	{
		private UserContext context;
		private DivRepTextBox name;
		private DivRepTextBox primary_email, secondary_email, primary_email_check ;
		private DivRepTextBox primary_phone, secondary_phone;
		private DivRepTextBox primary_phone_ext, secondary_phone_ext;
		private DivRepTextBox sms_address;
		
		private DivRepTextBox address_line_1, address_line_2;
		private DivRepTextBox city, state, zipcode, country;
		private DivRepTextBox im;
		// private PhotoDE photo_url;
		private DivRepSelectBox timezone;
		private HashMap<Integer, String> timezone_id2tz;
		private DivRepTextArea profile;
		private DivRepTextArea contact_preference;
		private DivRepCheckBox use_twiki;
		private DivRepTextBox twiki_id;
		
		public RegistraitonForm(final UserContext context) {
			
			super(context.getPageRoot(), origin_url);
			this.context = context;

			new DivRepStaticContent(this, "<div class=\"divrep_form_element\"><label>Your Certificate DN</label><br/><input type=\"text\" disabled=\"disabled\" class=\"input-xxlarge\" value=\""+context.getAuthorization().getUserDN()+"\"/></div>");
			
			name = new DivRepTextBox(this);
			name.setLabel("Enter Your Full Name");
			name.setRequired(true);
			
			primary_email = new DivRepTextBox(this);
			primary_email.setLabel("Enter Your Email");
			primary_email.setRequired(true);
			
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
			primary_email_check.setLabel("Re-enter Your Email");
			primary_email_check.setRequired(true);
			primary_email_check.addValidator(new CheckValidator(primary_email));
			
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

			address_line_1 = new DivRepTextBox(this);
			address_line_1.setLabel("Address Line 1");
	
			address_line_2 = new DivRepTextBox(this);
			address_line_2.setLabel("Address Line 2");
			
			city = new DivRepTextBox(this);
			city.setLabel("City");
			city.setRequired(true);
	
			state = new DivRepTextBox(this);
			state.setLabel("State");
			state.setRequired(true);
	
			zipcode = new DivRepTextBox(this);
			zipcode.setLabel("Zipcode");
			zipcode.setRequired(true);
	
			country = new DivRepTextBox(this);
			country.setLabel("Country");
			country.setRequired(true);
			
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
				}});
			
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected Boolean doSubmit() {			
			ContactModel model = new ContactModel(context);
			DNModel dnmodel = new DNModel(context);
			try {
				//Find contact record with the same email address
				ContactRecord rec = model.getEnabledByemail(primary_email.getValue());
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
					
					//reset  counter
					rec.count_hostcert_day = 0;
					rec.count_hostcert_year = 0;
					rec.count_usercert_year = 0;
					
					rec.person = true;
					rec.disable = false;
					model.insert(rec);
				} else {
					//Make sure that this contact is not used by any DN already
					if(dnmodel.getEnabledByContactID(rec.id) != null) {
						alert("The email address specified is already associated with a different non-disabled DN. Please try different email address.");
						return false;
					}
				}
				
				//Then insert a new DN record
				DNRecord dnrec = new DNRecord();
				dnrec.contact_id = rec.id;
				Authorization auth = context.getAuthorization();
				dnrec.dn_string = auth.getUserDN();
				dnrec.disable = false;
				dnmodel.insert(dnrec);
				
				//Give user OSG end user access
				DNAuthorizationTypeModel dnauthmodel = new DNAuthorizationTypeModel(context);
				DNAuthorizationTypeRecord dnauthrec = new DNAuthorizationTypeRecord();
				dnauthrec.dn_id = dnrec.id;
				dnauthrec.authorization_type_id = 1; //OSG End User
				dnauthmodel.insert(dnauthrec);
				
				//jump to profile page for more details
				context.message(MessageType.SUCCESS, "Thank you for registering your DN. Your registration was successful.");
				redirect("home");
				return true;
				
			} catch (SQLException e) {
				log.error("SQLException while submitting registration", e);
				alert(e.toString());
				return false;
			}
		}
	}
	
	/*
	private SideContentView createSideView(UserContext context) {
		SideContentView view = new SideContentView();
		view.add(new HtmlView("<p class=\"alert\">If you already have an OIM account and wish to associate another DN to your existing account, please open a <a href=\"https://ticket.grid.iu.edu\">GOC ticket</a> requesting for contact DN association.</p>"));		
		return view;
	}
	*/
}
