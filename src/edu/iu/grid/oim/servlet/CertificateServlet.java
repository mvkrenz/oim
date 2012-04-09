package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepButton;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContactAssociationView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.DivRepWrapper;
import edu.iu.grid.oim.view.ExternalLinkView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.MenuView;
import edu.iu.grid.oim.view.Page;
import edu.iu.grid.oim.view.SideContentView;
import edu.iu.grid.oim.view.ToolTip;
import edu.iu.grid.oim.view.divrep.form.UserCertificateRequestForm;

public class CertificateServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateServlet.class);  
    
    public CertificateServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		BootMenuView menuview = new BootMenuView(context, "certificate");
		ContentView content;
		
		String dirty_id = request.getParameter("id");
		if(dirty_id != null) {
			int id = Integer.parseInt(dirty_id);
			
			//TODO - Access control for this certificateid
			
			content = createCertificateView(id);
		} else {
			content = createIndexView();
		}
		BootPage page = new BootPage(context, menuview, content, null);
		//page.putSideViewLeft(true);
		page.render(response.getWriter());
	}
	
	protected ContentView createCertificateView(int id) throws ServletException
	{
		ContentView v = new ContentView();
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Certificat Requests", "certificate");
		bread_crumb.addCrumb(Integer.toString(id),  null);
		v.setBreadCrumb(bread_crumb);
		
		v.add(new HtmlView("<h2>Details</h2>"));
		v.add(new HtmlView("<p class=\"help-block\">TODO</p>"));
		
		v.add(new HtmlView("<h2>Activity Log</h2>"));
		v.add(new HtmlView("<table class=\"table\">"));
		v.add(new HtmlView("<thead><tr><th>By</th><th>Status</th><th>Note</th><th>Timestamp</th></tr></thead>"));
		
		v.add(new HtmlView("<tbody>"));
		
		v.add(new HtmlView("<tr class=\"latest\">"));
		v.add(new HtmlView("<td>Digicert</td>"));
		v.add(new HtmlView("<td>ISSUED</td>"));
		v.add(new HtmlView("<td>Certificate Signer has issued certificate</td>"));
		v.add(new HtmlView("<td>04/03/2012 12:43 UTC</td>"));
		v.add(new HtmlView("</tr>"));	
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<td>John Foo</td>"));
		v.add(new HtmlView("<td>APPROVED</td>"));
		v.add(new HtmlView("<td>Certificate Request was apporved by RA</td>"));
		v.add(new HtmlView("<td>04/02/2012 12:43 UTC</td>"));
		v.add(new HtmlView("</tr>"));	
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<td>Soichi Hayashi</td>"));
		v.add(new HtmlView("<td>REQUESTED</td>"));
		v.add(new HtmlView("<td>Submitted user certificate request</td>"));
		v.add(new HtmlView("<td>04/01/2012 12:43 UTC</td>"));
		v.add(new HtmlView("</tr>"));	

		
		v.add(new HtmlView("</tbody>"));
		
		v.add(new HtmlView("</table>"));
		return v;
	}
	
	protected ContentView createIndexView() throws ServletException
	{
		ContentView v = new ContentView();
	
		Authorization auth = context.getAuthorization();
		//if(auth.isUser()) {	
		//v.add(new HtmlView("<h1>Certificates</h1>"));
		v.add(createCertificateList());
		/*
		v.add(new HtmlView("<div class=\"tabbable\">"));
		v.add(new HtmlView("  <ul class=\"nav nav-tabs\">"));
		v.add(new HtmlView("    <li class=\"active\"><a href=\"#user\" data-toggle=\"tab\">My Certificates</a></li>"));
		v.add(new HtmlView("    <li><a href=\"#host\" data-toggle=\"tab\">Certificates I Sponsor</a></li>"));
		v.add(new HtmlView("  </ul>"));
		v.add(new HtmlView("  <div class=\"tab-content\">"));
		v.add(new HtmlView("    <div class=\"tab-pane active\" id=\"user\">"));
		v.add(createCertificateIRequested());
		v.add(new HtmlView("    </div>"));
		v.add(new HtmlView("    <div class=\"tab-pane\" id=\"host\">"));
		v.add(new HtmlView("      <p>TODO.. host cert</p>"));
		v.add(new HtmlView("    </div>"));
		v.add(new HtmlView("  </div>"));
		v.add(new HtmlView("</div>"));
		*/
		v.add(new HtmlView("<a class=\"btn\" href=\"certificaterequest\"><i class=\"icon-plus-sign\"></i> Request New Certificate</a>"));
		/*
		} else if(auth.isGuest()) {			
			v.add(new HtmlView("<form class=\"form-horizontal\" method=\"get\">"));
			v.add(new HtmlView("<fieldset>"));
			v.add(new HtmlView("  <legend>Retrieve Certificate</legend>"));
			v.add(new HtmlView("  <div class=\"control-group\">"));
			v.add(new HtmlView("    <label class=\"control-label\" for=\"input01\">Certificate ID</label>"));
			v.add(new HtmlView("    <div class=\"controls\">"));
			v.add(new HtmlView("      <input type=\"text\" class=\"input-xlarge\" name=\"id\" placeholder=\"12345\">"));
			v.add(new HtmlView("      <p class=\"help-block\">Please enter certificate </p>"));
			v.add(new HtmlView("    </div>"));
			v.add(new HtmlView("  </div>"));
			v.add(new HtmlView("  <div class=\"control-group\">"));
			v.add(new HtmlView("    <label class=\"control-label\" for=\"input01\">Passphrase</label>"));
			v.add(new HtmlView("    <div class=\"controls\">"));
			v.add(new HtmlView("      <input type=\"password\" class=\"input-xlarge\" name=\"pass\">"));
			v.add(new HtmlView("      <p class=\"help-block\">Passphrase used to make this request.</p>"));
			v.add(new HtmlView("    </div>"));
			v.add(new HtmlView("  </div>"));
			v.add(new HtmlView("  <div class=\"form-actions\">"));
			v.add(new HtmlView("    <button type=\"submit\" class=\"btn btn-primary\">Open</button>"));
			v.add(new HtmlView("  </div>"));
			v.add(new HtmlView("</fieldset>"));
			v.add(new HtmlView("</form>"));
		}
		*/
	
		return v;
	}
	
	protected GenericView createCertificateList() {
		
		GenericView v = new GenericView();
		
		v.add(new HtmlView("<h2>Certificate Requests</h2>"));
		v.add(new HtmlView("<table class=\"table certificate\">"));
		v.add(new HtmlView("<thead><tr><th>ID</th><th>Title</th><th>Requester</th><th>Sponsor</th><th>Status</th></tr></thead>"));
		
		v.add(new HtmlView("<tbody>"));
		
		v.add(new HtmlView("<tr onclick=\"document.location='certificate?id=123';\">"));
		v.add(new HtmlView("<td>123</td>"));
		v.add(new HtmlView("<td>User certificate for Soichi Hayashi"));
		v.add(new HtmlView("<td>Soichi Hayashi</td>"));
		v.add(new HtmlView("<td>John Foo</td>"));
		v.add(new HtmlView("<td>REQUESTED</td>"));
		v.add(new HtmlView("</tr>"));	
		
		v.add(new HtmlView("<tr onclick=\"document.location='certificate?id=234';\">"));
		v.add(new HtmlView("<td>234</td>"));
		v.add(new HtmlView("<td>Host certificate for soichi.grid.iu.edu</td>"));
		v.add(new HtmlView("<td>Soichi Hayashi</td>"));
		v.add(new HtmlView("<td>John Foo</td>"));
		v.add(new HtmlView("<td>REQUESTED</td>"));
		v.add(new HtmlView("</tr>"));	
		
		v.add(new HtmlView("</tbody>"));
		
		v.add(new HtmlView("</table>"));
		
		return v;
	}
	
	/*
	protected ContentView createSideView() throws ServletException
	{
		ContentView v = new ContentView();
		//v.add(new HtmlView("<a class=\"btn\" href=\"certificaterequest\"><i class=\"icon-plus-sign\"></i> Request New Certificate</a>"));
		v.add(new HtmlView("<div class=\"well\" style=\"padding: 8px 0;\">"));
		v.add(new HtmlView("<ul class=\"nav nav-list\">"));
		v.add(new HtmlView("  <li class=\"active\"><a href=\"certificate\"><i class=\"icon-home icon-white\"></i> List Certificates</a></li>"));
		v.add(new HtmlView("  <li><a href=\"certificateshow\"><i class=\"icon-book\"></i> Show Certificate Request</a></li>"));
		v.add(new HtmlView("  <li><a href=\"certificaterequest\"><i class=\"icon-plus-sign\"></i> Request New Certificate</a></li>"));
		v.add(new HtmlView("</ul>"));
		v.add(new HtmlView("</div>"));
		return v;
	}
	*/
	
}
