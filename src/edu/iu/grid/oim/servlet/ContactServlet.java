package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepToggler;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.ResourceContactModel;
import edu.iu.grid.oim.model.db.ResourceModel;
import edu.iu.grid.oim.model.db.SCContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BreadCrumbView;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.ItemTableView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.RecordTableView;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.divrep.ViewWrapper;

public class ContactServlet extends ServletBase implements Servlet {
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(ContactServlet.class);  
	
    public ContactServlet() {
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		auth.check("edit_my_contact");

		try {
		
			//construct view
			//MenuView menuview = new MenuView(context, "contact");
			//ContentView contentview = createContentView();
			
			BootMenuView menuview = new BootMenuView(context, "contact");
			ContentView contentview = null;
			//display either list, or a single resource
			ContactRecord rec = null;
			String contact_id_str = request.getParameter("id");
			if(contact_id_str != null) {
				Integer contact_id = Integer.parseInt(contact_id_str);
				ContactModel model = new ContactModel(context);
				rec = model.get(contact_id);
				contentview = new ContentView();
				
				// setup crumbs
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				// bread_crumb.addCrumb("Administration", "admin");
				bread_crumb.addCrumb("Contact", "contact");
				bread_crumb.addCrumb(rec.name, null);
				contentview.setBreadCrumb(bread_crumb);

				contentview.add(new HtmlView("<h2>"+StringEscapeUtils.escapeHtml(rec.name)+"</h2>"));	
				contentview.add(createContent(rec, false)); //false = no edit button	

			} else {
				//pull list of all contacts
				contentview = createContentView();
			}
			
			BootPage page = new BootPage(context, menuview, contentview, createSideView(rec));
			page.render(response.getWriter());			
		} catch (SQLException e) {
			log.error(e);
			throw new ServletException(e);
		}
	}
	
	protected ContentView createContentView() 
		throws ServletException, SQLException
	{
		ContactModel model = new ContactModel(context);
		ArrayList<ContactRecord> contacts = model.getAll();

		Collections.sort(contacts, new Comparator<ContactRecord> (){
			public int compare(ContactRecord a, ContactRecord b) {
				return a.getName().compareToIgnoreCase(b.getName()); // We are comparing based on name
			}
		});
		Collections.sort(contacts, new Comparator<ContactRecord> (){
			public int compare(ContactRecord a, ContactRecord b) {
				return a.isPerson().compareTo(b.isPerson()); // We are comparing based on bool person
			}
		});

		Collections.sort(contacts, new Comparator<ContactRecord> (){
			public int compare(ContactRecord a, ContactRecord b) {
				return a.isDisabled().compareTo(b.isDisabled()); // We are comparing based on bool disable (disabled ones will go in the end)
			}
		});

		ContentView contentview = new ContentView();	
		
		ArrayList<ContactRecord> editable_contacts = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> editable_disabled_contacts = new ArrayList<ContactRecord>();
		ArrayList<ContactRecord> readonly_contacts = new ArrayList<ContactRecord>();
		for(ContactRecord rec : contacts) {
			if(model.canEdit(rec.id)) {
				if (rec.isDisabled()) {
					editable_disabled_contacts.add(rec);
				} else {
					editable_contacts.add(rec);
				}
			} else {
				readonly_contacts.add(rec);
			}
		}
		
		return createContentViewHelper (contentview, editable_contacts, editable_disabled_contacts, readonly_contacts);
	}

	protected ContentView createContentViewHelper (ContentView contentview, 
			Collection<ContactRecord> editable_contacts, 
			Collection<ContactRecord> editable_disabled_contacts, 
			Collection<ContactRecord> readonly_contacts) 
		throws ServletException, SQLException
	{  
		contentview.add(new HtmlView("<h1>OSG Contacts</h1>"));
		if(editable_contacts.size() != 0) {
			contentview.add(new HtmlView("<h2>Editable</h2>"));
			contentview.add(new HtmlView("<p>You have edit access to following contacts</p>"));
	
			ItemTableView table = new ItemTableView(4);
			for(ContactRecord rec : editable_contacts) {
				table.add(new HtmlView(getContactHeader(rec, true)));
				//contentview.add(showContact(rec, true)); //true = show edit button
			}
			contentview.add(table);
		}

		if(readonly_contacts.size() != 0) {
			contentview.add(new HtmlView("<h2>Read-Only</h2>"));
			contentview.add(new HtmlView("<p>Following are the contact that are currently registered at OIM that you do not have edit access.</p>"));
	
			ItemTableView table = new ItemTableView(4);
			for(ContactRecord rec : readonly_contacts) {
				table.add(new HtmlView(getContactHeader(rec, false)));
				//contentview.add(showContact(rec, false)); //false = no edit button
			}
			contentview.add(table);
		}
		
		if(auth.allows("admin") && editable_disabled_contacts.size() != 0) {
			contentview.add(new HtmlView("<h2>Disabled (Admin Only)</h2>"));
			contentview.add(new HtmlView("<p>Following are the contacts that are currently disabled.</p>"));
	
			ItemTableView table = new ItemTableView(4);
			for(ContactRecord rec : editable_disabled_contacts) {
				table.add(new HtmlView(getContactHeader(rec, true)));
				//contentview.add(showContact(rec, true)); //true = show edit button
			}
			contentview.add(table);
		}	
		
		return contentview;
	}
	private String getContactHeader(ContactRecord rec, boolean edit)
	{
		String image, name_to_display;
		if(rec.person == true) {
			image = "<img align=\"top\" src=\"images/user.png\"/> ";
		} else {
			image = "<img align=\"top\" src=\"images/group.png\"/> "; 
		}
		String url = "";
		if(edit) {
			url = "contactedit?id="+rec.id;
		} else {
			url = "contact?id="+rec.id;
		}
		if(rec.disable == false) {
			name_to_display = image+"<a href=\""+url+"\">"+StringEscapeUtils.escapeHtml(rec.name)+"</a>";
		} else {
			name_to_display = image+"<a href=\""+url+"\" class=\"disabled\">"+StringEscapeUtils.escapeHtml(rec.name)+"</a>";
		}
		return name_to_display;
	}
	
	public DivRep createContent(final ContactRecord rec, final boolean show_edit_button) {
		RecordTableView table = new RecordTableView();
		try {	
			table.addRow("Primary Email", new HtmlView("<a class=\"mailto\" href=\"mailto:"+rec.primary_email+"\">"+StringEscapeUtils.escapeHtml(rec.primary_email)+"</a>"));
			table.addRow("Secondary Email", rec.secondary_email);

			table.addRow("Primary Phone", rec.primary_phone);
			table.addRow("Primary Phone Ext", rec.primary_phone_ext);

			table.addRow("Secondary Phone", rec.secondary_phone);
			table.addRow("Secondary Phone Ext", rec.secondary_phone_ext);
			
			table.addRow("SMS Address", rec.sms_address);
			
			if(rec.person == false) {
				table.addRow("Personal Information", new HtmlView("(Not a personal contact)"));
			} else {
				RecordTableView personal_table = new RecordTableView("inner_table");
				table.addRow("Personal Information", personal_table);
	
				personal_table.addRow("Address Line 1", rec.address_line_1);
				personal_table.addRow("Address Line 2", rec.address_line_2);
				personal_table.addRow("City", rec.city);
				personal_table.addRow("State", rec.state);
				personal_table.addRow("ZIP Code", rec.zipcode);
				personal_table.addRow("Country", rec.country);
				personal_table.addRow("Instant Messaging", rec.im);
	
				String img = rec.photo_url;
				if(rec.photo_url == null || rec.photo_url.length() == 0) {
					img = "images/noavatar.gif";
				} 
				personal_table.addRow("Photo", new HtmlView("<img class=\"avatar\" src=\""+img+"\"/>"));
				personal_table.addRow("Contact Preference", rec.contact_preference);	
				personal_table.addRow("Time Zone", rec.timezone);
				personal_table.addRow("Profile", new HtmlView("<div>"+StringEscapeUtils.escapeHtml(rec.profile)+"</div>"));
				personal_table.addRow("Use TWiki", rec.use_twiki);
				personal_table.addRow("TWiki ID", rec.twiki_id);
			}
			
			table.addRow("Contact Associations", new ContactAssociationView(context, rec.id));
			
			//table.addRow("Active", rec.active);
			table.addRow("Disable", rec.disable);
			
			DNModel dnmodel = new DNModel(context);
			if(auth.allows("admin")) {
				String submitter_dn = null;
				if(rec.submitter_dn_id != null) {
					DNRecord dn = dnmodel.get(rec.submitter_dn_id);
					submitter_dn = dn.dn_string;
				}
				table.addRow("Submitter DN", submitter_dn);
			}

			if(auth.allows("admin")) {
				String dn_string = null;
				DNRecord dnrec = dnmodel.getByContactID(rec.id);
				if(dnrec != null) {
					dn_string = dnrec.dn_string;
				}
				table.addRow("Associated DN", dn_string);		
			}

			if(show_edit_button) {
				class EditButtonDE extends DivRepButton
				{
					String url;
					public EditButtonDE(DivRep parent, String _url)
					{
						super(parent, "Edit");
						url = _url;
					}
					protected void onEvent(DivRepEvent e) {
						redirect(url);
					}
				};
				table.add(new DivRepWrapper(new EditButtonDE(context.getPageRoot(), "contactedit?id=" + rec.id)));
			}
		} catch (SQLException e) {
			return new DivRepStaticContent(context.getPageRoot(), e.toString());
		}
		return new ViewWrapper(context.getPageRoot(), table);
	}
	
	private SideContentView createSideView(ContactRecord rec)
	{
		SideContentView view = new SideContentView();
		
		if(rec == null) {
			//view.add(new HtmlView("<h3>Other Actions</h3>"));
			//view.add(new HtmlView("<div class=\"indent\">"));
			view.add(new HtmlView("<p><a class=\"btn\" href=\"contactedit\">Register New Contact</a></p>"));
			//view.add(new HtmlView("</div>"));
			view.add(new HtmlView("This page shows a list of contacts on OIM. Contacts can be a person or a mailing list or a service that needs to be registered on OIM to access privileged information on other OSG services. <p><br/> You as a registered OIM user will be able to edit any contact you added. GOC staff are able to edit all contacts including previous de-activated ones. <p><br/> If you want to map a certain person or group contact (and their email/phone number) to a resource, VO, SC, etc. but cannot find that contact already in OIM, then you can add a new contact. <p><br/>  Note that if you add a person as a new contact, that person will still not be able to perform any actions inside OIM until they register their X509 certificate on OIM."));		
			view.addContactGroupFlagLegend();
		} else {
			//view.addContactLegend();
		}
		return view;
	}
}
