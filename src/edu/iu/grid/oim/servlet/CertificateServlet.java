package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;

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
		ContentView content = null;
		
		String dirty_id = request.getParameter("id");
		if(dirty_id != null) {
			int id = Integer.parseInt(dirty_id);
		
			try {
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
				CertificateRequestUserRecord rec = model.get(id);
				if(!model.canView(rec)) {
					throw new AuthorizationException("You don't have access to view this certificate");
				}
				ArrayList<CertificateRequestUserModel.Log> logs = model.getLogs(id);
				content = createCertificateView(rec, logs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			content = createIndexView();
		}
		BootPage page = new BootPage(context, menuview, content, null);
		//page.putSideViewLeft(true);
		page.render(response.getWriter());
	}
	
	protected ContentView createCertificateView(CertificateRequestUserRecord rec, ArrayList<CertificateRequestUserModel.Log> logs) throws ServletException
	{
		ContentView v = new ContentView();
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("Certificat Requests", "certificate");
		bread_crumb.addCrumb(Integer.toString(rec.id),  null);
		v.setBreadCrumb(bread_crumb);
		
		//details
		v.add(new HtmlView("<h2>Details</h2>"));
		v.add(new HtmlView("<table class=\"table\">"));
	
		v.add(new HtmlView("<tbody>"));
		
		/*
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>ID</th>"));
		v.add(new HtmlView("<td>"+rec.id.toString()+"</td>"));
		v.add(new HtmlView("</tr>"));
		*/
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>Requester Name</th>"));
		String auth_status = "(Unauthenticated)";
		if(rec.requester_contact_id != null) {
			auth_status = "(OIM Authenticated)";
		}
		v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.requester_name)+" " +  auth_status+ "</td>"));
		v.add(new HtmlView("</tr>"));
		
		/*
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>Status</th>"));
		v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.status)+"</td>"));
		v.add(new HtmlView("</tr>"));
		*/
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>GOC Ticket</th>"));
		v.add(new HtmlView("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>"));
		v.add(new HtmlView("</tr>"));
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>DN</th>"));
		v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.dn)+"</td>"));
		v.add(new HtmlView("</tr>"));
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>VO</th>"));
		VOModel vmodel = new VOModel(context);
		VORecord vo;
		try {
			vo = vmodel.get(rec.vo_id);
			v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(vo.name)+"</td>"));
		} catch (SQLException e) {
			log.error("Failed to find vo information for certificate view", e);
			v.add(new HtmlView("<td>N/A</td>"));
		}
		v.add(new HtmlView("</tr>"));
		
		v.add(new HtmlView("</tbody>"));
		
		v.add(new HtmlView("</table>"));
		
		//logs
		v.add(new HtmlView("<h2>Activity Log</h2>"));
		v.add(new HtmlView("<table class=\"table\">"));
		v.add(new HtmlView("<thead><tr><th>By</th><th>IP</th><th>Status</th><th>Note</th><th>Timestamp</th></tr></thead>"));
		
		v.add(new HtmlView("<tbody>"));
		
		SimpleDateFormat dformat = new SimpleDateFormat();
		boolean latest = true;
		for(CertificateRequestUserModel.Log log : logs) {
			if(latest) {
				v.add(new HtmlView("<tr class=\"latest\">"));
				latest = false;
			} else {
				v.add(new HtmlView("<tr>"));
			}
			if(log.contact != null) {
				v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(log.contact.name)+"</td>"));
			} else {
				v.add(new HtmlView("<td>(Guest)</td>"));
			}
			v.add(new HtmlView("<td>"+log.ip+"</td>"));
			v.add(new HtmlView("<td>"+log.status+"</td>"));
			v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(log.comment)+"</td>"));
			v.add(new HtmlView("<td>"+dformat.format(log.time)+"</td>"));
			v.add(new HtmlView("</tr>"));			
		}
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
		v.add(new HtmlView("<a class=\"btn pull-right\" href=\"certificaterequest\"><i class=\"icon-plus-sign\"></i> Request New Certificate</a>"));
		
		CertificateRequestUserModel usermodel = new CertificateRequestUserModel(context);
		
		if(auth.isUser()) {
			//load my user certificate requests
			try {
				ArrayList<CertificateRequestUserRecord> recs = usermodel.getMine(auth.getContact().id);
				v.add(createMyUserCertList(recs));
			} catch (SQLException e) {
				v.add(new HtmlView("<div class=\"alert\">Failed to load your user certificate requests</div>"));
			}
						
			//load my host certificate requests
		} else {
			//load guest user certificate requests
			try {
				ArrayList<CertificateRequestUserRecord> recs = usermodel.getGuest();
				v.add(createGuestUserCertList(recs));
			} catch (SQLException e) {
				v.add(new HtmlView("<div class=\"alert\">Failed to load your user certificate requests</div>"));
			}

			//load guest host certificate requests
		}
		
		//TODO - order by update timestamp
		
		//v.add(createCertificateList(list));
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
	
	protected GenericView createMyUserCertList(ArrayList<CertificateRequestUserRecord> recs) {
		GenericView v = new GenericView();	
		v.add(new HtmlView("<h2>My User Certificate Requests</h2>"));
		v.add(new HtmlView("<table class=\"table certificate\">"));
		v.add(new HtmlView("<thead><tr><th>ID</th><th>Status</th><th>GOC Ticket</th><th>DN</th><th>VO</th></tr></thead>"));
		
		v.add(new HtmlView("<tbody>"));
		
		for(CertificateRequestUserRecord rec : recs) {
			v.add(new HtmlView("<tr onclick=\"document.location='certificate?id="+rec.id+"&type=user';\">"));
			v.add(new HtmlView("<td>USER"+rec.id+"</td>"));
			v.add(new HtmlView("<td>"+rec.status+"</td>"));
			//TODO - use configured goc ticket URL
			v.add(new HtmlView("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>"));
			v.add(new HtmlView("<td>"+rec.dn+"</td>"));
			try {
				VOModel vomodel = new VOModel(context);
				VORecord vo = vomodel.get(rec.vo_id);
				v.add(new HtmlView("<td>"+vo.name+"</td>"));
			} catch (SQLException e) {
				v.add(new HtmlView("<td>sql error</td>"));
			}
			v.add(new HtmlView("</tr>"));	
		}
		v.add(new HtmlView("</tbody>"));
		
		v.add(new HtmlView("</table>"));
		return v;
	}
	protected GenericView createGuestUserCertList(ArrayList<CertificateRequestUserRecord> recs) {
		GenericView v = new GenericView();	
		v.add(new HtmlView("<h2>My User Certificate Requests</h2>"));
		v.add(new HtmlView("<table class=\"table certificate\">"));
		v.add(new HtmlView("<thead><tr><th>ID</th><th>Requester</th><th>Status</th><th>GOC Ticket</th><th>DN</th><th>VO</th></tr></thead>"));
		
		v.add(new HtmlView("<tbody>"));
		
		for(CertificateRequestUserRecord rec : recs) {
			v.add(new HtmlView("<tr onclick=\"document.location='certificate?id="+rec.id+"&type=user';\">"));
			v.add(new HtmlView("<td>USER"+rec.id+"</td>"));
			v.add(new HtmlView("<td>"+rec.requester_name+"</td>"));
			v.add(new HtmlView("<td>"+rec.status+"</td>"));
			//TODO - use configured goc ticket URL
			v.add(new HtmlView("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>"));
			v.add(new HtmlView("<td>"+rec.dn+"</td>"));
			try {
				VOModel vomodel = new VOModel(context);
				VORecord vo = vomodel.get(rec.vo_id);
				v.add(new HtmlView("<td>"+vo.name+"</td>"));
			} catch (SQLException e) {
				v.add(new HtmlView("<td>sql error</td>"));
			}
			v.add(new HtmlView("</tr>"));	
		}
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
