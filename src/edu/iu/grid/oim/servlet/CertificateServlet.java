package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepDialog;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.DivRepPassStrengthValidator;
import edu.iu.grid.oim.model.db.UserCertificateRequestModel;
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

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		Authorization auth = context.getAuthorization();
		
		BootMenuView menuview = new BootMenuView(context, "certificate");
		ContentView content = null;
		String dirty_id = request.getParameter("id");
		
		if(dirty_id != null) {
			int id = Integer.parseInt(dirty_id);
			String type = request.getParameter("type");
			if(type.equals("user")) {
				try {
					UserCertificateRequestModel model = new UserCertificateRequestModel(context);
					CertificateRequestUserRecord rec = model.get(id);
					if(!model.canView(rec)) {
						throw new AuthorizationException("You don't have access to view this certificate");
					}
					//display certificate detail
					ArrayList<UserCertificateRequestModel.Log> logs = model.getLogs(id);
					content = createCertificateView(context, rec, logs);
				} catch (SQLException e) {
					log.error("Failed to load user certificate", e);
				}
			} else if(type.equals("host")) {
				//TODO - lost host certificate detail
				
				
			}
		} else {
			//for both user and host certificates
			content = createIndexView(context);
		}
		BootPage page = new BootPage(context, menuview, content, null);
		//page.putSideViewLeft(true);
		page.render(response.getWriter());
	}
	
	protected ContentView createCertificateView(final UserContext context, final CertificateRequestUserRecord rec, ArrayList<UserCertificateRequestModel.Log> logs) throws ServletException
	{
		Authorization auth = context.getAuthorization();
		SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		ContentView v = new ContentView();
		//setup crumbs
		BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
		bread_crumb.addCrumb("User Certificat Requests", "certificate");
		bread_crumb.addCrumb(Integer.toString(rec.id),  null);
		v.setBreadCrumb(bread_crumb);
		
		/*
		//guest
		final DivRepButton guestbutton = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Test</button>");
		guestbutton.setStyle(DivRepButton.Style.HTML);
		guestbutton.addClass("inline");
		guestbutton.addEventListener(new DivRepEventListener() {
            public void handleEvent(DivRepEvent e) {
            	if(context.getAuthorization().isGuest()) {
            		guestbutton.alert("You are guest :" + context.getPageRoot().toString());
            	}
            	if(context.getAuthorization().isUser()) {
            		guestbutton.alert("Logged in");
            	}
            }
        });
		v.add(guestbutton);
		*/
	
		
		//details
		//v.add(new HtmlView("<h2>Details</h2>"));
		
		v.add(nextActionControl(context, rec));
		
		v.add(new HtmlView("<table class=\"table nohover\">"));
	
		v.add(new HtmlView("<tbody>"));
		
		/*
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>ID</th>"));
		v.add(new HtmlView("<td>"+rec.id.toString()+"</td>"));
		v.add(new HtmlView("</tr>"));
		*/
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>Status</th>"));
		v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.status)));
		if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
			String gencsr_class = "progressing";
			if(rec.csr != null) {
				gencsr_class = "completed";
			}
			String sign_class = "notstarted";
			if(rec.csr != null && rec.cert_pkcs7 == null) {
				sign_class = "progressing";
			}
			v.add(new HtmlView("<ul class=\"progress_display\">"));
			v.add(new HtmlView("<li class=\""+gencsr_class+"\">Generating CSR/Private Key</li>"));
			v.add(new HtmlView("<li class=\""+sign_class+"\">Signing Certificate</li>"));
			v.add(new HtmlView("</ul>"));
			v.add(new HtmlView("<script>setTimeout('window.location.reload()', 3000);</script>"));
		}
		v.add(new HtmlView("</td>"));
		v.add(new HtmlView("</tr>"));

		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>Requester Name</th>"));
		String auth_status = "(Unauthenticated)";
		if(rec.requester_contact_id != null) {
			auth_status = "(OIM Authenticated)";
		}
		v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.requester_name)+" " +  auth_status+ "</td>"));
		v.add(new HtmlView("</tr>"));
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>Requested Time</th>"));
		v.add(new HtmlView("<td>"+dformat.format(rec.request_time)+"</td>"));
		v.add(new HtmlView("</tr>"));
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>GOC Ticket</th>"));
		v.add(new HtmlView("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>"));
		v.add(new HtmlView("</tr>"));
		
		v.add(new HtmlView("<tr>"));
		v.add(new HtmlView("<th>DN</th>"));
		v.add(new HtmlView("<td>"+StringEscapeUtils.escapeHtml(rec.dn)+"</td>"));
		v.add(new HtmlView("</tr>"));
		
		if(rec.cert_pkcs7 != null) {
			v.add(new HtmlView("<tr>"));
			v.add(new HtmlView("<th>Certificates</th>"));
			v.add(new HtmlView("<td>"));
			HttpSession session = context.getSession();

			UserCertificateRequestModel model = new UserCertificateRequestModel(context);
			if(model.getPrivateKey(rec.id) != null) {
				v.add(new HtmlView("<a href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs12\">Download PKCS12</a>"));
			} 
			v.add(new HtmlView("<ul>"));
			v.add(new HtmlView("<li><a href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs7\">Download PKCS7</a></li>"));
			//v.add(new HtmlView("<li><a href=\"certificatedownload?id="+rec.id+"&type=user&download=intermediate\">Download Intermediate Certificate</a></li>"));
			//v.add(new HtmlView("<li><a href=\"certificatedownload?id="+rec.id+"&type=user&download=certificate\">Download Certificate</a></li>"));
			v.add(new HtmlView("</ul>"));
			v.add(new HtmlView("</td>"));
			v.add(new HtmlView("</tr>"));
		}
		
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
		v.add(new HtmlView("<table class=\"table nohover\">"));
		v.add(new HtmlView("<thead><tr><th>By</th><th>IP</th><th>Status</th><th>Note</th><th>Timestamp</th></tr></thead>"));
		
		v.add(new HtmlView("<tbody>"));
		
		boolean latest = true;
		for(UserCertificateRequestModel.Log log : logs) {
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
	
	protected GenericView nextActionControl(final UserContext context, final CertificateRequestUserRecord rec) {
		GenericView v = new GenericView();
		
		final String url = "certificate?id="+rec.id+"&type=user";
		
		final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
		note.setLabel("Action Note");
		note.setRequired(true);
		note.setHidden(true);
		v.add(note);
		
		//controls
		final UserCertificateRequestModel model = new UserCertificateRequestModel(context);
		if(model.canApprove(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Approve</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                	if(model.approve(rec)) {
	                		button.redirect(url);
	                	} else {
	                		button.alert("Failed to approve request");
	                	}
                	}
                }
            });
			note.setHidden(false);
			v.add(button);
		}
		if(model.canRequestRenew(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-refresh icon-white\"></i> Request Renew</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                  	if(model.requestRenew(rec)) {
	                		button.redirect(url);
	                	} else {
	                		button.alert("Failed to request renewal");
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		if(model.canRequestRevoke(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-exclamation-sign icon-white\"></i> Request Revocation</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                  	if(model.requestRevoke(rec)) {
	                		button.redirect(url);
	                	} else {
	                		button.alert("Failed to request revoke request");
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		if(model.canIssue(rec)) {
			v.add(new HtmlView("<p class=\"help-block\">Please provide passphrase to encrypt your p12 certificate</p>"));
			final DivRepPassword pass = new DivRepPassword(context.getPageRoot());
			pass.setLabel("Passphrase");
			pass.setRequired(true);
			pass.addValidator(new DivRepPassStrengthValidator());
			v.add(pass);
			/*
			class IssueDialog extends DivRepDialog {
				DivRepPassword issue_passphrase;
				
				public IssueDialog(DivRep parent) {
					super(parent);
					setHasCancelButton(true);
					setTitle("Issue Certificate");
					DivRepStaticContent l = new DivRepStaticContent(this ,"<p class=\"help-block\">Please provide passphrase to retreive & encryp your p12 certificate</p>");
					issue_passphrase = new DivRepPassword(this);
					issue_passphrase.setLabel("Passphrase");
					issue_passphrase.addValidator(new DivRepPassStrengthValidator());
				}

				@Override
				public void onSubmit() {
					String passphrase = issue_passphrase.getValue();
					if(model.issue(rec, passphrase)) {
						redirect(url);
					} else {
						alert("Failed to issue certificate");
					}
					
				}

				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
				
			}
			final DivRepDialog issue_dialog = new IssueDialog(context.getPageRoot());
			v.add(issue_dialog);
			*/
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificate ...</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	/*
                  	if(model.issue(rec)) {
                		button.redirect(url);
                	} else {
                		button.alert("Failed to issue certificate");
                	}*/
                	if(pass.validate()) {
                		context.setComment(note.getValue());
                		//start process thread 
                      	if(model.startissue(rec, pass.getValue())) {
                    		button.redirect(url);
                    	} else {
                    		button.alert("Failed to issue certificate");
                    	}
                	}
                }
            });
			v.add(button);
		}
		if(model.canCancel(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn\">Cancel Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
            		context.setComment(note.getValue());
                	if(model.cancel(rec)) {
                		button.redirect(url);
                	} else {
                		button.alert("Failed to cancel request");
                	}
                }
            });
			v.add(button);
		}
		if(model.canReject(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-remove icon-white\"></i> Reject Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                  	if(model.reject(rec)) {
	                		button.redirect(url);
	                	} else {
	                		button.alert("Failed to reject request");
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		if(model.canRevoke(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-exclamation-sign icon-white\"></i> Revoke</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                 	if(model.revoke(rec)) {
	                		button.redirect(url);
	                	} else {
	                		button.alert("Failed to cancel request");
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		
		return v;
	}
	
	protected ContentView createIndexView(UserContext context) throws ServletException
	{
		ContentView v = new ContentView();
	
		Authorization auth = context.getAuthorization();
		//if(auth.isUser()) {	
		//v.add(new HtmlView("<h1>Certificates</h1>"));
		v.add(new HtmlView("<a class=\"btn pull-right\" href=\"certificaterequest\"><i class=\"icon-plus-sign\"></i> Request New Certificate</a>"));
		
		UserCertificateRequestModel usermodel = new UserCertificateRequestModel(context);
		
		if(auth.isUser()) {
			//load my user certificate requests
			try {
				ArrayList<CertificateRequestUserRecord> recs = usermodel.getMine(auth.getContact().id);
				v.add(createMyUserCertList(context, recs));
			} catch (SQLException e) {
				v.add(new HtmlView("<div class=\"alert\">Failed to load your user certificate requests</div>"));
			}
						
			//load my host certificate requests
			v.add(new HtmlView("<h2>TODO: My Host Certificate Requests</h2>"));
			/*
			try {
				ArrayList<CertificateRequestHostRecord> recs = hostmodel.getMine(auth.getContact().id);
				v.add(createMyHostCertList(context, recs));
			} catch (SQLException e) {
				v.add(new HtmlView("<div class=\"alert\">Failed to load your host certificate requests</div>"));
			}
			*/
		} else {
			/*
			//load guest user certificate requests
			try {
				ArrayList<CertificateRequestUserRecord> recs = usermodel.getGuest();
				v.add(createGuestUserCertList(context, recs));
			} catch (SQLException e) {
				v.add(new HtmlView("<div class=\"alert\">Failed to load your user certificate requests</div>"));
			}
			*/
			v.add(new HtmlView("<div class=\"alert\">TODO - guest can't list but lookup certificate using certificate id</div>"));

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
	
	protected GenericView createMyUserCertList(UserContext context, ArrayList<CertificateRequestUserRecord> recs) {
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
	/*
	protected GenericView createGuestUserCertList(UserContext context, ArrayList<CertificateRequestUserRecord> recs) {
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
	*/
	
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
