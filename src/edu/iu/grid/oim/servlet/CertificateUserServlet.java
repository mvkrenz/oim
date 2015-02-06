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
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase.LogDetail;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.UserCertificateActionView;
import edu.iu.grid.oim.view.UserCertificateTable;
import edu.iu.grid.oim.view.certaction.UserCertRenew;
import edu.iu.grid.oim.view.divrep.EditableContent;
import edu.iu.grid.oim.view.divrep.UserCNEditor;

public class CertificateUserServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateUserServlet.class);  

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
					content = detailView(context, rec, logs, submenu);
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
		}
	}
	
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
	
	protected IView detailView(
			final UserContext context, 
			final CertificateRequestUserRecord rec, 
			final ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs,
			final String submenu) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
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
				//out.write("<pre style=\"background-color: inherit;\">"+StringEscapeUtils.escapeHtml(rec.dn)+"</pre>");
				out.write(StringEscapeUtils.escapeHtml(rec.dn));
				//out.write("<h3>Status"+rec.status+"</h3>");
				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				
				out.write("<div class=\"row-fluid\">");
				out.write("<div class=\"span2\" style=\"margin-top: 4px;\"><b>Status</b></div>");			
				out.write("<div class=\"span10\">");
				out.write("<h3>"+rec.status+"</h3>");
				//out.write("<h3>Status"+rec.status+"</h3>");
				out.write("</div>"); //span9			
				out.write("</div>"); //row-fluid
				//out.write("<hr>");
				out.write("<br>");

				//renderBasicInfo(out);
				
				BootTabView tabview = new BootTabView();
				tabview.addtab("Detail", new DetailView());
				tabview.addtab("Log", new LogView());
				tabview.addtab("Renew", new UserCertRenew(context.getPageRoot()));
				
				//renderAction(out);
				
				tabview.render(out);
				
				out.write("</div>"); //span9
				out.write("</div>"); //row-fluid
				out.write("</div>"); //content
			}	
			
			public void renderAction(PrintWriter out) {
				//action view is a bit more complicated..
				UserCertificateActionView actionview = new UserCertificateActionView(context, rec, logs);
				//out.write("<h2>Action</h2>");
				actionview.render(out);
			}
			
			/*
			public void renderBasicInfo(PrintWriter out) {
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
				
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
					CertificateRequestUserModel model = new CertificateRequestUserModel(context);
					
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
						/*
						if(model.getPrivateKey(rec.id) != null) {
							//pkcs12 available
							out.write("<p><a class=\"btn btn-primary btn-large\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs12\">Download Certificate &amp; Private Key (PKCS12)</a></p>");
							out.write("<p class=\"alert alert-error\">You need to download your certificate and private key now, while your browser session is active. When your session times out, the server will delete your private key for security reasons and you will need to request a new certificate.</p>");
						} else {
							//only pkcs7
							out.write("<p><a class=\"btn btn-primary\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs7\">Download Certificate (PKCS7 - For Browser)</a></p>");
							out.write("<p><a class=\"btn\" href=\"certificatedownload?id="+rec.id+"&type=user&download=x509\">Download Certificate (X509 PEM - For Commandline)</a></p>");
						}
						*/
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
						CertificateRequestUserModel usermodel = new CertificateRequestUserModel(context);
						ArrayList<ContactRecord> ras = usermodel.findRAs(rec);
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
						CertificateRequestUserModel usermodel = new CertificateRequestUserModel(context);
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
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
				
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
