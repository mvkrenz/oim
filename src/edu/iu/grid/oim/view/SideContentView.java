package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.SCRecord;
import edu.iu.grid.oim.model.db.record.VOContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.divrep.form.RARequestForm;

//put each content under a side content header
public class SideContentView implements IView {
    static Logger log = Logger.getLogger(SideContentView.class);  
	
	private ArrayList<IView> children = new ArrayList<IView>();
	
	public void add(IView v) {
		children.add(v);
	}
	
	public void add(DivRepPage de) {
		add(new DivRepWrapper(de));
	}
	
	public void add(String html) {
		add(new HtmlView(html));
	}
	public void add(String title, IView v) {
		children.add(new HtmlView("<h2>"+title+"</h2>"));
		children.add(new HtmlView("<p>"));
		children.add(v);
		children.add(new HtmlView("</p>"));
	}
	
	public void add(String title, DivRep de) {
		add(title, new DivRepWrapper(de));
	}
	
	public void add(String title, String html) {
		add(title, new HtmlView(html));
	}
	
	public void addContactNote () {
		//TODO agopu need external link icon in CSS
		add("Contacts", new HtmlView("The contact editor allow you to search and assign contacts. When assigning various contacts on this form, if you do not find the contact you are searching for, then <a href=\"contactedit\" target=\"_blank\">add a new contact</a> first."));		
	}
	public void addContactLegend () {
		add(new HtmlView("<h2>Contact Ranks</h2><div class=\'contact_rank contact_Primary\'>Primary</div><div class=\'contact_rank contact_Secondary\'>Secondary</div><div class=\'contact_rank contact_Tertiary\'>Tertiary</div>"));		
	}
	public void addContactGroupFlagLegend () {
		add("Contact Grouping Legend", new HtmlView("Contacts are flagged based on whether they are tagged as a group contact or not:</p><p><div class=\'contact_rank contact_flag_Group\'>Group Contact (Mailing list, etc.)</div><div class=\'contact_rank contact_flag_Person\'>Non-Group Contact (Human user or Service-cert. mapped user)</div>"));		
	}
	public void render(PrintWriter out)
	{
		out.println("<div id=\"sideContent\">");
		for(IView v : children) {
			v.render(out);
		}
		out.println("</div>");
	}

	public void addRARequest(UserContext context, VORecord vo) {
		Authorization auth = context.getAuthorization();
		if(vo.id != null && auth.isUser() && !auth.allows("admin_ra")) {
			//lookup vomanager
			VOContactModel model = new VOContactModel(context);
			ContactRecord vomanager = null;
			try {
				for(VOContactRecord crec : model.getByVOID(vo.id)) {
					if(crec.contact_type_id.equals(6)) { //== VO manager
						ContactModel cmodel = new ContactModel(context);
						vomanager = cmodel.get(crec.contact_id);
						break;
					}
				}
				
				//lookup sc
				SCModel smodel = new SCModel(context);
				try {
					SCRecord screc = smodel.get(vo.sc_id);
					
					final RARequestForm form = new RARequestForm(context, vomanager, vo, screc);
					add(new DivRepWrapper(form));
					
					DivRepButton request = new DivRepButton(context.getPageRoot(), "Request for RA Enrollment");
					request.addClass("btn");
					//request.addClass("pull-right");
					add(new DivRepWrapper(request));
					request.addEventListener(new DivRepEventListener() {
						@Override
						public void handleEvent(DivRepEvent e) {
							form.show();
						}});
					
				} catch (SQLException e1) {
					log.error("Failed to lookup SC for with SC ID" + vo.sc_id, e1);
				}
				
				
			} catch (SQLException e) {
				log.error("Failed to lookup VO manager for VO: " + vo.id, e);
			}
			
		}
	}

}
