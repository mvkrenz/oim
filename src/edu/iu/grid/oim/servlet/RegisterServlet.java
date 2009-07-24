package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;
import com.divrep.validator.DivRepUniqueValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.MenuItem;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNAuthorizationTypeModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNAuthorizationTypeRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.SideContentView;

public class RegisterServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(RegisterServlet.class);  
    private String origin_url = StaticConfig.getApplicationBase() + "/home";
    
    public RegisterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		Authorization auth = context.getAuthorization();
		if(auth.getDNID() != null || auth.getUserCN() == null || auth.getUserCN() == null) {
			//user don't meet the requirement to register. send it to home
			response.sendRedirect(StaticConfig.getApplicationBase()+ "/home");
			return;
		}
		
		MenuView menuview = new MenuView(context, "register");
		ContentView contentview = createContentView();
		Page page = new Page(menuview, contentview, new SideContentView());
		page.render(response.getWriter());	
	}
	
	protected ContentView createContentView()
	{
		ContentView contentview = new ContentView();
		
		contentview.add(new HtmlView("<h1>OIM Registration</h1>"));
		
		WizardDE wizard = new WizardDE(context.getPageRoot());
		wizard.setPage(new GreetingPage(wizard));
		contentview.add(new DivRepWrapper(wizard));
		
		return contentview;
	}

	interface IWizardPage {
		public void render(PrintWriter out);	
	}
	class WizardDE extends DivRep
	{
		IWizardPage currentpage = null;
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		public WizardDE(DivRep _parent) {
			super(_parent);
		}
		public void setPage(IWizardPage page)
		{
			currentpage = page;
			redraw();
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			currentpage.render(out);
			out.write("</div>");
		}
	}
	
	class GreetingPage extends DivRep implements IWizardPage
 	{
		DivRepButton button;
		WizardDE wizard;
		public GreetingPage(WizardDE _wizard) {
			super(_wizard);
			wizard = _wizard;
			button = new DivRepButton(this, "Register");
			button.addEventListener(new DivRepEventListener() {

				@Override
				public void handleEvent(DivRepEvent e) {
					wizard.setPage(new EnterEmailPage(wizard));
				}});
		}
		
		public void render(PrintWriter out) {
			out.write("<p>Welcome to the OSG Information Management (OIM) system. Your DN ("+auth.getUserDN()+"), that is loaded into your web browser, is not registered on OIM. </p>");
			out.write("<p>To get access and begin using OIM, please register now by clicking the button below.</p>");		
			button.render(out);
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	class EnterEmailPage extends DivRepForm implements IWizardPage
 	{
		WizardDE wizard;
		private DivRepTextBox name;
		private DivRepTextBox email;
		private DivRepTextBox email_check;
		private DivRepTextBox phone;
		
		public EnterEmailPage(WizardDE _wizard) {
			super(_wizard, origin_url);
			wizard = _wizard;
			
			name = new DivRepTextBox(this);
			name.setLabel("Enter Your Full Name");
			name.setRequired(true);
			
			email = new DivRepTextBox(this);
			email.setLabel("Enter Your Email");
			email.setRequired(true);
			
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
			
			email_check = new DivRepTextBox(this);
			email_check.setLabel("Re-enter Your Email");
			email_check.setRequired(true);
			email_check.addValidator(new CheckValidator(email));
			
			phone = new DivRepTextBox(this);
			// TODO Need formatting help here -agopu  -- looks like the phone number used by OSG community is very diverse. I don't know how simple is simple enough
			// validation in our case.. -- hayashis
			phone.setLabel("Enter Your Phone Number");
			phone.setRequired(true);
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected Boolean doSubmit() {
			Boolean ret = true;
			
			ContactModel model = new ContactModel(context);
			DNModel dnmodel = new DNModel(context);
			try {
				//Find contact record with the same email address
				ContactRecord rec = model.getByemail(email.getValue());
				//Create new one if none is found
				if(rec == null) {
					rec = new ContactRecord();
					rec.name = name.getValue();
					rec.primary_email = email.getValue();
					rec.primary_phone = phone.getValue();
					rec.person = true;
					// Setting to false by default
					//rec.active = false;
					rec.disable = false;
					model.insert(rec);
				} else {
					//Make sure that this contact is not used by any DN already
					if(dnmodel.getByContactID(rec.id) != null) {
						alert("The email address specified is already associated with a different DN. Please try different email address.");
						email.setValue("");
						email_check.setValue("");

						context.close();
						return false;
					}
				}
				
				//Then insert a new DN record
				DNRecord dnrec = new DNRecord();
				dnrec.contact_id = rec.id;
				dnrec.dn_string = auth.getUserDN();
				dnmodel.insert(dnrec);
				
				//Give him OSG end user access
				DNAuthorizationTypeModel dnauthmodel = new DNAuthorizationTypeModel(context);
				DNAuthorizationTypeRecord dnauthrec = new DNAuthorizationTypeRecord();
				dnauthrec.dn_id = dnrec.id;
				dnauthrec.authorization_type_id = 1; //OSG End User
				dnauthmodel.insert(dnauthrec);
				
				//jump to profile page for more details
				redirect(StaticConfig.getApplicationBase()+"/profileedit");
				
			} catch (SQLException e) {
				alert(e.toString());
				redirect(origin_url);
				ret = false;
			}
			
			context.close();
			return ret;
		}
	}
}
