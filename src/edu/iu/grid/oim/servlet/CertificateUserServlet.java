package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase.LogDetail;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.BootTabView.Tab;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.UserCertificateTable;
import edu.iu.grid.oim.view.certaction.UserCertApprove;
import edu.iu.grid.oim.view.certaction.UserCertCancel;
import edu.iu.grid.oim.view.certaction.UserCertCancelWithPass;
import edu.iu.grid.oim.view.certaction.UserCertIssue;
import edu.iu.grid.oim.view.certaction.UserCertReRequest;
import edu.iu.grid.oim.view.certaction.UserCertReject;
import edu.iu.grid.oim.view.certaction.UserCertRenew;
import edu.iu.grid.oim.view.certaction.UserCertRequestRevoke;
import edu.iu.grid.oim.view.certaction.UserCertRevoke;
import edu.iu.grid.oim.view.divrep.Wizard.WizardPage;

public class CertificateUserServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateUserServlet.class);  
    
    public class TabLabels {
    	public final static String issue = "issue";
    	public final static String renew = "renew";
    	public final static String approve = "approve";
    	public final static String reject = "reject";
    	public final static String cancel_with_pass = "cancel_with_pass";
    	public final static String cancel = "cancel";
    	public final static String request_revoke = "request_revoke";
    	public final static String revoke = "revoke";
    	public final static String re_request = "re_request";
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		if(!request.isSecure()) {
			//force redirection to https - this page could transmit password
			response.sendRedirect(context.getSecureUrl());
			return;
		}
		
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		BootMenuView menuview = new BootMenuView(context, "certificate");
		IView content = null;
		String dirty_id = request.getParameter("id");
		String select_tab = request.getParameter("t");
		if(select_tab == null) select_tab = "_none";
		/*
				String status = request.getParameter("status");
		if(status != null && dirty_id != null) {
			//display status
			int id = Integer.parseInt(dirty_id);
			CertificateRequestUserRecord rec;
			try {
				rec = model.get(id);
				
				ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs = model.getLogs(CertificateRequestUserModel.class, id);
				LogDetail issued_log = model.getLastLog(CertificateRequestStatus.ISSUED, logs);
				boolean need_generate_csr = false;
				if(issued_log == null) {
					need_generate_csr = true;
				}
				
				IView view = statusView(rec, need_generate_csr);
				view.render(response.getWriter());
			} catch (SQLException e) {
				throw new ServletException("Failed to load specified certificate", e);
			}
		} else {
		*/
		if(dirty_id != null) {
			//display detail view
			try {
				int id = Integer.parseInt(dirty_id);
				CertificateRequestUserRecord rec = model.get(id);
				if(rec == null) {
					throw new ServletException("No request found with a specified request ID.");
				}
				if(!model.canView(rec)) {
					throw new AuthorizationException("You don't have access to view this certificate");
				}
				ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs = model.getLogs(CertificateRequestUserModel.class, id);
				String submenu = "certificateuser";
				if(request.getParameter("search") != null) {
					submenu = "certificatesearchuser";
				}
				content = detailView(context, rec, logs, submenu, select_tab);
			} catch (SQLException e) {
				throw new ServletException("Failed to load specified certificate", e);
			} catch (NumberFormatException e2) {
				throw new ServletException("Prpobably failed to parse request id", e2);
			}
		} else {
			//display list view
			content = listView(context);
		}

		BootPage page = new BootPage(context, menuview, content, null);
		page.render(response.getWriter());
		//}
	}
	/*
	protected IView statusView(final CertificateRequestUserRecord rec, final boolean generate_csr) {
		return new IView() {

			@Override
			public void render(PrintWriter out) {
			
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {

					out.write("<ul class=\"progress_display\">");
					
					if(generate_csr) {
						String gencsr_class = "progressing";
						if(rec.csr != null) {
							gencsr_class = "completed";
						}
						out.write("<li class=\""+gencsr_class+"\">Generating CSR/Private Key</li>");
					}
					
					String sign_class = "notstarted";
					if(rec.csr != null && rec.cert_pkcs7 == null) {
						sign_class = "progressing";
					}
					out.write("<li class=\""+sign_class+"\">Signing Certificate</li>");

					out.write("</ul>");
					
					out.write("<script>setTimeout(loadstatus, 1000);</script>"); //reload status in 3 sec
				} else {
					//finished - reload page to refresh all fields
					out.write("<script>document.location.reload(true);</script>"); //true -- force get instead of using cache
				}
			}
		};
	}
	*/
	protected IView detailView(
			final UserContext context, 
			final CertificateRequestUserRecord rec, 
			final ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs,
			final String submenu,
			final String select_tab) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
		
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
 				CertificateMenuView menu = new CertificateMenuView(context, submenu);
				menu.render(out);
				out.write("</div>"); //span3
		
				out.write("<div class=\"span9\">");
		
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("User Certificate Requests", "certificateuser");
				bread_crumb.addCrumb(Integer.toString(rec.id),  null);
				bread_crumb.render(out);	
				
				/* I believe this is deprecated by admin/config/page banner
				//editable content
				ConfigModel config = new ConfigModel(context);
				Config home_content = config.new Config(config, "certificate_user", "");
				Authorization auth = context.getAuthorization();
				if(auth.allows("admin") || auth.allows("admin_ra")) {
					EditableContent content = new EditableContent(context.getPageRoot(), context, home_content);
					content.render(out);
					//out.write("<br style=\"clear: both;\">");
				} else {
					out.write(home_content.getString());
				}
				*/
								
				//header
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span2\"><b>DN</b></div>");			
				out.write("<div class=\"span10\">");
				out.write(StringEscapeUtils.escapeHtml(rec.dn));
				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span2\" style=\"margin-top: 4px;\"><b>Status</b></div>");			
				out.write("<div class=\"span10\">");
				
				//TODO - put come color on this
				out.write("<h3>"+rec.status+"</h3>");
				
				//show the most recent log
				if(logs.size() > 0) {
					CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail lastlog = logs.get(0);
					out.write("<p><i>"+StringEscapeUtils.escapeHtml(lastlog.comment)+"</i></p>");
				}
				
				/*
				//display chevlons
				String cls = "";
				out.write("<ul class=\"divrep_chevs\">");
				
				cls = "";
				if(rec.status.equals(CertificateRequestStatus.REQUESTED)) cls = "active";
				out.write("<li class=\""+cls+"\"><a nohref>Requested</a></li>");
				
				cls = "";
				if(rec.status.equals(CertificateRequestStatus.CANCELED)) {
					out.write("<li class=\"active\"><a nohref>Canceled</a></li>");
				}
				
				cls = "";
				if(rec.status.equals(CertificateRequestStatus.APPROVED)) cls = "active";
				out.write("<li class=\""+cls+"\"><a nohref>Approved</a></li>");

				cls = "";
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) cls = "active";
				out.write("<li class=\""+cls+"\"><a nohref>Issuing</a></li>");
				
				cls = "";
				if(rec.status.equals(CertificateRequestStatus.ISSUED)) cls = "active";
				out.write("<li class=\""+cls+"\"><a nohref>Issued</a></li>");
				
				//revocation request puts it in the different track..
				if(rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
					out.write("<li class=\"active\"><a nohref>Revocation Requested</a></li>");
				}
				//only show one terminal state
				if(rec.status.equals(CertificateRequestStatus.REJECTED)) {
					out.write("<li class=\"active\"><a nohref>Rejected</a></li>");	
				} else if(rec.status.equals(CertificateRequestStatus.EXPIRED)) {
					out.write("<li class=\"active\"><a nohref>Expired</a></li>");	
				} else if(rec.status.equals(CertificateRequestStatus.REVOKED)) {
					out.write("<li class=\"active\"><a nohref>Revoked</a></li>");	
				} else if(rec.status.equals(CertificateRequestStatus.FAILED)) {
					out.write("<li class=\"active\"><a nohref>FAILED</a></li>");	
				} else {
					//by default, show expired
					cls = "";
					if(rec.status.equals(CertificateRequestStatus.EXPIRED)) cls = "active";
					out.write("<li class=\""+cls+"\"><a nohref>Expired</a></li>");	
				}
				out.write("</ul>");
				*/
				
				out.write("</div>"); //span9			
				out.write("</div>"); //row-fluid
				out.write("<br>");
				
				BootTabView tabview = new BootTabView();
				Tab tab;
				
				/////////////////////////////////////////////////////////////////////////////
				//RA item should probably come first..
				if(model.canApprove(rec)) {
					tab = tabview.addtab("Approve", new UserCertApprove(context, rec));
					if(select_tab.equals(TabLabels.approve)) tabview.setActive(tab);
				}
				AuthorizationCriterias criterias = model.canIssue(rec);
				if(criterias.passAll()) {
					tab = tabview.addtab("Issue", new UserCertIssue(context, rec, criterias));
					if(select_tab.equals(TabLabels.issue)) tabview.setActive(tab);
				}
				if(model.canReject(rec)) {
					tab = tabview.addtab("Reject", new UserCertReject(context, rec));
					if(select_tab.equals(TabLabels.reject)) tabview.setActive(tab);
				}
				
				/////////////////////////////////////////////////////////////////////////////
				//the some user flow control
				if(model.canCancel(rec)) {
					tab = tabview.addtab("Cancel", new UserCertCancel(context, rec));
					if(select_tab.equals(TabLabels.cancel)) tabview.setActive(tab);
				} else {
					//if user can cancel without pass, then no need to show cancel_with_pass
					if(model.canCancelWithPass(rec)) {
						tab = tabview.addtab("Cancel", new UserCertCancelWithPass(context, rec));
						if(select_tab.equals(TabLabels.cancel_with_pass)) tabview.setActive(tab);
					}
				}
				
				//always show this.. UserCertRenew can handle in case user can't renew
				tab = tabview.addtab("Renew", new UserCertRenew(context, rec, model.canRenew(rec, logs)));
				if(select_tab.equals(TabLabels.renew)) tabview.setActive(tab);
				
				if(model.canRevoke(rec)) {
					tab = tabview.addtab("Revoke", new UserCertRevoke(context, rec));
					if(select_tab.equals(TabLabels.revoke)) tabview.setActive(tab);
				} else {
					//if user can revoke immediately, no need to *request* revocation.
					if(model.canRequestRevoke(rec)) {
						tab = tabview.addtab("Revocation Request", new UserCertRequestRevoke(context, rec));
						if(select_tab.equals(TabLabels.request_revoke)) tabview.setActive(tab);
					}
				}
				
				if(model.canReRequest(rec)) {
					tab = tabview.addtab("Re-Request", new UserCertReRequest(context, rec));
					if(select_tab.equals(TabLabels.re_request)) tabview.setActive(tab);
				}
				
				tabview.addtab("Log", new LogView(), true);
				tabview.addtab("Detail", new DetailView(), true);
				
				tabview.render(out);
				
				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				out.write("</div>"); //content
			}	
			/*
			public void renderAction(PrintWriter out) {
				//action view is a bit more complicated..
				UserCertificateActionView actionview = new UserCertificateActionView(context, rec, logs);
				//out.write("<h2>Action</h2>");
				actionview.render(out);
			}
			*/
			
			/*
			public void renderBasicInfo(PrintWriter out) {
				
				out.write("<table class=\"table nohover\">");
				out.write("<tbody>");
				
				//ID
				out.write("<tr>");
				out.write("<th style=\"min-width: 150px;\">Request ID</th>");
				out.write("<td>");
				out.write(rec.id.toString());
				out.write("</td>");
				out.write("<tr>");
				
				//STATUS
				out.write("<tr>");
				out.write("<th>Status</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.status));
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					out.write("<div id=\"status_progress\">Loading...</div>");
					
					out.write("<script type=\"text/javascript\">");
					out.write("function loadstatus() { ");
					//why adding new Date()? see http://stackoverflow.com/questions/1061525/jquerys-load-not-working-in-ie-but-fine-in-firefox-chrome-and-safari
					out.write("    $('#status_progress').load('certificateuser?id="+rec.id+"&status&'+new Date().getTime() );"); 
					out.write("}");
					out.write("loadstatus();");
					out.write("</script>");
				} 
				out.write("</td>");
				out.write("</tr>");
				
				//DN
				out.write("<tr>");
				out.write("<th>DN");
				out.write("</th>");
				out.write("<td>");
				UserCNEditor cn_override = null;
				if(model.canOverrideCN(rec)) {
					cn_override = new UserCNEditor(context.getPageRoot());
					cn_override.setRequired(true);
					cn_override.setValue(rec.getCN());
					cn_override.render(out);
				} else {
					out.write(StringEscapeUtils.escapeHtml(rec.dn));	
				}
				if(rec.csr != null) {
					out.write("<a class=\"muted pull-right\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs10\">CSR</a>");
				}
				out.write("</td>");
				out.write("</tr>");
				out.write("</tbody>");
				out.write("</table>");
			}
			*/
			
			class DetailView extends GenericView {
				public void render(PrintWriter out) {
					
					//out.write("<h2>Detail</h2>");
					
					out.write("<table class=\"table nohover\">");
					out.write("<tbody>");
									
					if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
						out.write("<tr class=\"no-border-top\"><th>Serial Number</th><td>");
						out.write(rec.cert_serial_id);
						out.write("</td></tr>");
					}
					
					//certificates
					out.write("<tr>");
					out.write("<th style=\"min-width: 150px;\">Public Certificates</th>");
					out.write("<td>");
					if(rec.cert_certificate != null) {
						//only pkcs7
						out.write("<p class=\"pull-right\"><a class=\"muted\" target=\"_blank\" href=\"https://confluence.grid.iu.edu/pages/viewpage.action?pageId=3244066\">How to import user certificate on your browser</a></p>");
						out.write("<p><a href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs7\">PKCS7 (For Browser)</a></p>");

						out.write("<p class=\"pull-right\"><a class=\"muted\" target=\"_blank\" href=\"https://confluence.grid.iu.edu/display/CENTRAL/Importing+User+Certificate+for+Command+Line+Use\">How to import user certificate for command line use (grid-proxy-init).</a></p>");
						out.write("<p><a href=\"certificatedownload?id="+rec.id+"&type=user&download=x509\">X509 PEM (For Commandline)</a></p>");						
					} else {
						out.write("<p><span class=\"muted\">Not Issued</span></p>");
					}
					if(rec.csr != null) {
						out.write("<a href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs10\">CSR</a>");
					}
					out.write("</td>");
					out.write("</tr>");
					
					/*
					GenericView action_control = nextActionControl(context, rec, cn_override, logs);
					out.write("<tr>");
					out.write("<th>Action</th>");
					out.write("<td>");
					action_control.render(out);
					out.write("</td>");
					out.write("</tr>");
					*/
					
					//valid dates
					out.write("<tr>");
					out.write("<th>Valid Dates</th>");
					out.write("<td>");
					if(rec.cert_notafter != null && rec.cert_notbefore != null) {
						out.write("Between " + rec.cert_notbefore.toString() + " and " + rec.cert_notafter.toString()); 
					} else {
						out.write("<span class=\"muted\">N/A</span>");
					}
					out.write("</td>");
					out.write("</tr>");
					
					//GOC Ticket
					out.write("<tr>");
					out.write("<th>GOC Ticket</th>");
					out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
					out.write("</tr>");			
					
					//VO
					out.write("<tr>");
					out.write("<th>VO</th>");
					VOModel vmodel = new VOModel(context);
					VORecord vo;
					try {
						vo = vmodel.get(rec.vo_id);
						out.write("<td>"+StringEscapeUtils.escapeHtml(vo.name)+"</td>");
					} catch (SQLException e) {
						log.error("Failed to find vo information for certificate view", e);
						out.write("<td><span class=\"muted\">N/A</span></td>");
					}
					out.write("</tr>");
					
					try {
						out.write("<tr>");
						out.write("<th>RA</th>");
						ArrayList<ContactRecord> ras = model.findRAs(rec);
						if(ras.isEmpty()) {
							out.write("<td><span class=\"muted\">N/A</span></td>");
						} else {
							out.write("<td>");
							for(ContactRecord ra : ras) {
								out.write("<p>");
								out.write("<b>"+StringEscapeUtils.escapeHtml(ra.name)+"</b>");
								if(auth.isUser()) {
									out.write(" <code><a href=\"mailto:"+ra.primary_email+"\">"+ra.primary_email+"</a></code>");
									out.write(" Phone: "+ra.primary_phone);
								}
								out.write("</p>");
								
							}
							out.write("</td>");	
							
						}
						out.write("</tr>");
					} catch (SQLException e) {
						out.write("<td>sql error</td>");
					}
					/* -- sponsor field no longer makes sense - until we store user selected sponsor in db. we can't store this since we allow
					 * user to manually enter name/email
					try {
						out.write("<tr>");
						out.write("<th>Sponsors</th>");
						ArrayList<ContactRecord> sponsors = usermodel.findSponsors(rec);
						if(sponsors.isEmpty()) {
							out.write("<td><span class=\"muted\">N/A</span></td>");
						} else {
							out.write("<td>");
							out.write("<ul>");
							for(ContactRecord sponsor : sponsors) {
								if(auth.isUser()) {
									out.write("<li>");
									out.write("<a href=\"mailto:"+sponsor.primary_email+"\">"+StringEscapeUtils.escapeHtml(sponsor.name)+"</a>");
									out.write(" Phone: "+sponsor.primary_phone+"</li>");
								} else {
									out.write("<li>"+sponsor.name+"</li>");
								}
							}
							out.write("</ul>");
							out.write("</td>");	
						}
						out.write("</tr>");
					} catch (SQLException e) {
						out.write("<td>sql error</td>");
					}
					*/
					
					//Requester
					out.write("<tr>");
					out.write("<th>Requester</th>");
					try {
						ContactModel cmodel = new ContactModel(context);
						ContactRecord requester = cmodel.get(rec.requester_contact_id);
						out.write("<td>");
						if(requester.disable) {
							out.write("<span class=\"label label-warning pull-right\">Guest</span>");
						}
						if(auth.isUser()) {
							out.write("<b>"+StringEscapeUtils.escapeHtml(requester.name)+"</b>");
							out.write(" <code><a href=\"mailto:"+requester.primary_email+"\">"+requester.primary_email+"</a></code>");
							out.write(" Phone: "+requester.primary_phone);
						} else {
							out.write(StringEscapeUtils.escapeHtml(requester.name));
						}
						out.write("</td>");
					} catch (SQLException e1) {
						out.write("<td>(sql error)</td>");
					}
					out.write("</tr>");
					
					//Reuqested Time
					out.write("<tr>");
					out.write("<th>Requested Time</th>");
					out.write("<td>"+dformat.format(rec.request_time)+"</td>");
					out.write("</tr>");
					
					//Last Approved Time
					out.write("<tr>");
					out.write("<th>Last Approved Time</th>");
					LogDetail approve_log = model.getLastLog(CertificateRequestStatus.APPROVED, logs);
					if(approve_log != null) {
						out.write("<td>"+dformat.format(approve_log.time)+"</td>");
					} else {
						out.write("<td>N/A</td>");
					}
					out.write("</tr>");
					
					out.write("</tbody>");
					out.write("</table>");
				}	
			}
			
			class LogView extends GenericView {
				public void render(PrintWriter out) {
					//logs
					//out.write("<h2>Log</h2>");
					out.write("<table class=\"table nohover\">");
					out.write("<thead><tr><th>By</th><th>IP</th><th>Status</th><th>Note</th><th style=\"min-width: 130px;\">Timestamp</th></tr></thead>");
					
					out.write("<tbody>");
					
					boolean latest = true;
					for(LogDetail log : logs) {
						if(latest) {
							out.write("<tr class=\"latest\">");
							latest = false;
						} else {
							out.write("<tr>");
						}
						if(log.contact != null) {
							out.write("<td>"+StringEscapeUtils.escapeHtml(log.contact.name)+"</td>");
						} else {
							out.write("<td>(Guest)</td>");
						}
						out.write("<td>"+log.ip+"</td>");
						out.write("<td>"+log.status+"</td>");
						out.write("<td>"+StringEscapeUtils.escapeHtml(log.comment)+"</td>");
						out.write("<td>"+dformat.format(log.time)+" "+dformat.getTimeZone().getDisplayName(false, TimeZone.SHORT)+"</td>");
						out.write("</tr>");			
					}
					out.write("</tbody>");
					out.write("</table>");
				}
			}
		};
	}
	
	protected IView listView(final UserContext context) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView(context, "certificateuser");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				if(auth.isUser()) {
					renderMyList(out);
				}
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
			}
			
			public void renderMyList(PrintWriter out) {
				
				try {
					ArrayList<CertificateRequestUserRecord> recs = model.getIApprove(auth.getContact().id);
					if(recs.size() != 0) {
						out.write("<h2>User Certificate Requests that I Approve</h2>");
						UserCertificateTable table = new UserCertificateTable(context, recs, false);
						table.render(out);
					}
				} catch (SQLException e1) {
					out.write("<div class=\"alert\">Failed to load user certificate requests that I am ra/sponso of</div>");
					log.error(e1);
				}
				
				try {
					ArrayList<CertificateRequestUserRecord> recs = model.getISubmitted(auth.getContact().id);
					if(recs.size() == 0) {
						out.write("<p class=\"muted\">You have not requested any user certificate.</p>");
					} else {
						out.write("<h2>User Certificate Requests that I Requested</h2>");
						UserCertificateTable table = new UserCertificateTable(context, recs, false);
						table.render(out);
					}
				} catch (SQLException e1) {
					out.write("<div class=\"alert\">Failed to load my certificate requests</div>");
					log.error(e1);
				}
				
				out.write("</div>");//content
			}
		};
	}

}
