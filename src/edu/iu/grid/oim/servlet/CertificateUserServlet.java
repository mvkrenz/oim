package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepTextArea;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase.LogDetail;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.UserCertificateTable;
import edu.iu.grid.oim.view.divrep.ChoosePassword;
import edu.iu.grid.oim.view.divrep.EditableContent;
import edu.iu.grid.oim.view.divrep.UserCNEditor;
import edu.iu.grid.oim.view.divrep.form.validator.MustbeCheckedValidator;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

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
					content = createDetailView(context, rec, logs, submenu);
				} catch (SQLException e) {
					throw new ServletException("Failed to load specified certificate", e);
				} catch (NumberFormatException e2) {
					throw new ServletException("Prpobably failed to parse request id", e2);
				}
			} else {
				//display list view
				content = createListView(context);
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
				} else {
					//finished! - redirect
					out.write("<script>document.location='certificateuser?id="+rec.id+"';</script>");
				}
			}
		};
	}
	
	protected IView createDetailView(
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
				
				renderDetail(out);
				renderLog(out);
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
				
				out.write("</div>"); //content
			}
			
			public void renderDetail(PrintWriter out) {
				
				//editable content
				ConfigModel config = new ConfigModel(context);
				Config home_content = config.new Config(config, "certificate_user", "");
				Authorization auth = context.getAuthorization();
				if(auth.allows("admin") || auth.allows("admin_ra")) {
					EditableContent content = new EditableContent(context.getPageRoot(), context, home_content);
					content.render(out);
				} else {
					out.write(home_content.getString());
				}
				
				out.write("<table class=\"table nohover\">");
				out.write("<tbody>");

				out.write("<tr>");
				out.write("<th>Status</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.status));
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					out.write("<div id=\"status_progress\">Loading...</div>");
					out.write("<script type=\"text/javascript\">");
					out.write("function loadstatus() { ");
					//why adding new Date()? see http://stackoverflow.com/questions/1061525/jquerys-load-not-working-in-ie-but-fine-in-firefox-chrome-and-safari
					out.write("$('#status_progress').load('certificateuser?id="+rec.id+"&status&'+new Date().getTime() );"); 
					out.write("setTimeout('loadstatus()', 1000);");
					out.write("}");
					out.write("$(function() {loadstatus();});");
					out.write("</script>");
				} 
				out.write("</td>");
				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th style=\"min-width: 100px;\">DN</th>");
				out.write("<td>");
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
				UserCNEditor cn_override = null;
				if(model.canOverrideCN(rec)) {
					cn_override = new UserCNEditor(context.getPageRoot());
					cn_override.setRequired(true);
					cn_override.setValue(rec.getCN());
					//cn_override.setDisabled(false);
					cn_override.render(out);
				} else {
					out.write(StringEscapeUtils.escapeHtml(rec.dn));	
				}
				out.write("</td>");
				out.write("</tr>");
				
				GenericView action_control = nextActionControl(context, rec, cn_override, logs);
				out.write("<tr>");
				out.write("<th>Action</th>");
				out.write("<td>");
				action_control.render(out);
				out.write("</td>");
				out.write("</tr>");
				
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
				
				out.write("<tr>");
				out.write("<th>Requested Time</th>");
				out.write("<td>"+dformat.format(rec.request_time)+"</td>");
				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th>Last Approved Time</th>");
				LogDetail approve_log = model.getLastLog(CertificateRequestStatus.APPROVED, logs);
				if(approve_log != null) {
					out.write("<td>"+dformat.format(approve_log.time)+"</td>");
				} else {
					out.write("<td>N/A</td>");
				}

				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th>GOC Ticket</th>");
				out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
				out.write("</tr>");			
				
				if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
					out.write("<tr><th>Serial Number</th><td>");
					out.write(rec.cert_serial_id);
					out.write("</td></tr>");
				}
				
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
						//out.write("<ul>");
						for(ContactRecord ra : ras) {
							/*
							if(auth.isUser()) {
								out.write("<p>");
								out.write("<a href=\"mailto:"+ra.primary_email+"\">"+StringEscapeUtils.escapeHtml(ra.name)+"</a>");
								out.write(" Phone: "+ra.primary_phone+"</p>");
							} else {
								out.write("<p>"+ra.name+"</p>");
							}
							*/
							out.write("<p>");
							out.write("<b>"+StringEscapeUtils.escapeHtml(ra.name)+"</b>");
							if(auth.isUser()) {
								out.write(" <code><a href=\"mailto:"+ra.primary_email+"\">"+ra.primary_email+"</a></code>");
								out.write(" Phone: "+ra.primary_phone);
							}
							out.write("</p>");
							
						}
						//out.write("</ul>");
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
				
				out.write("</tbody>");
				
				out.write("</table>");
			}
			
			public void renderLog(PrintWriter out) {
				//logs
				out.write("<h2>Log</h2>");
				out.write("<table class=\"table nohover\">");
				out.write("<thead><tr><th>By</th><th>IP</th><th>Status</th><th>Note</th><th>Timestamp</th></tr></thead>");
				
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
					out.write("<td>"+dformat.format(log.time)+"</td>");
					out.write("</tr>");			
				}
				out.write("</tbody>");
				out.write("</table>");
			}
		};
	}
	
	protected GenericView nextActionControl(
			final UserContext context, 
			final CertificateRequestUserRecord rec, 
			final UserCNEditor cn_override, 
			final ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs) {
		GenericView v = new GenericView();
		
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		final String url = "certificateuser?id="+rec.id;
	
		BootTabView tabview = new BootTabView();
		
		if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
			GenericView pane = new GenericView();

			if(model.getPrivateKey(rec.id) != null) {
				//pkcs12 available
				pane.add(new HtmlView("<p><a class=\"btn btn-primary btn-large\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs12\">Download Certificate &amp; Private Key (PKCS12)</a></p>"));
				pane.add(new HtmlView("<p class=\"alert alert-error\">You need to download your certificate and private key now, while your browser session is active. When your session times out, the server will delete your private key for security reasons and you will need to request a new certificate.</p>"));
			} else {
				//only pkcs7
				pane.add(new HtmlView("<p><a class=\"btn btn-primary\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs7\">Download Certificate (PKCS7 - For Browser)</a></p>"));
				pane.add(new HtmlView("<p><a class=\"btn\" href=\"certificatedownload?id="+rec.id+"&type=user&download=x509\">Download Certificate (X509 PEM - For Commandline)</a></p>"));
			}
			
			pane.add(new HtmlView("<p><a target=\"_blank\" href=\"https://confluence.grid.iu.edu/pages/viewpage.action?pageId=3244066\">How to import user certificate on your browser</a></p>"));
			pane.add(new HtmlView("<p><a target=\"_blank\" href=\"https://confluence.grid.iu.edu/display/CENTRAL/Importing+User+Certificate+for+Command+Line+Use\">How to import user certificate for command line use (grid-proxy-init).</a></p>"));
			
			tabview.addtab("Download", pane);
		}
		
		if(model.canApprove(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Approve</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		
                		//TODO - need to move this check to model.approve() - before we add REST interface
                		if(model.canOverrideCN(rec)) {
                			if(cn_override.validate()) {
		                		//Regenerate DN using provided CN
		                		X500Name name = model.generateDN(cn_override.getValue());
		                		rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
		                		
		                		//make sure we don't have duplicate CN requested already.
								try {
									DNModel dnmodel = new DNModel(context);
			                		DNRecord duplicate = dnmodel.getEnabledByDNString(rec.dn);
			                		if(duplicate != null/* && !duplicate.contact_id.equals(rec.requester_contact_id)*/) {
			                			button.alert("The same DN is already registered in OIM for user id:"+duplicate.contact_id + ". Please specify different CN");
			                			return;
			                		}
								} catch (SQLException e1) {
									log.error("Failed to test duplicate DN during approval process", e1);
									button.alert("Failed to test duplicate DN.");
									return;
								}	                		
                			} else {
                				button.alert("Failed to validate provided CN.");
                				return;
                			}
                		}
                			                	
                		try {
                			//check access again - request status might have changed
                			if(model.canApprove(rec)) {
                				model.approve(rec);
	                			context.message(MessageType.SUCCESS, "Successfully approved a request with ID: " + rec.id);
								button.redirect(url);
                			} else {
                				button.alert("Reques status has changed. Please reload.");
                			}
                		} catch (CertificateRequestException ex) {
                			String message = "Failed to approve request: " + ex.getMessage();
                			if(ex.getCause() != null) {
                				message += "\n\n" + ex.getCause().getMessage();
                			}
	                		button.alert(message);
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Approve", pane);
		}
		if(model.canRenew(rec, logs)) {
			GenericView pane = new GenericView();
				
			pane.add(new HtmlView("<p class=\"help-block\">Please choose a password to encrypt your renewed certificate / private key</p>"));
			
			final DivRepPassword pass = new DivRepPassword(context.getPageRoot());
			pass.setLabel("Password");
			pass.setRequired(true);
			pass.addValidator(new PKIPassStrengthValidator());
			pane.add(pass);
			
			final DivRepPassword pass_confirm = new DivRepPassword(context.getPageRoot());
			pass.addEventListener(new DivRepEventListener() {
				@Override
				public void handleEvent(DivRepEvent event) {
					if(pass_confirm.getValue() != null) {
						pass_confirm.validate();
					}
				}
			});
			pass_confirm.setLabel("Re-enter password");
			pass_confirm.setRequired(true);
			pass_confirm.addValidator(new DivRepIValidator<String>() {
				String message;
				@Override
				public Boolean isValid(String value) {
					if(value.equals(pass.getValue())) return true;
					message = "Password does not match";
					return false;
				}

				@Override
				public String getErrorMessage() {
					return message;
				}
			});
			pane.add(pass_confirm);
						
			//RA / GA agrement
			final DivRepCheckBox ra_agree = new DivRepCheckBox(context.getPageRoot());
			final DivRepCheckBox ga_agree = new DivRepCheckBox(context.getPageRoot());
			//agree.addClass("pull-right");
			Authorization auth = context.getAuthorization();
			try {
				//list VOs that this user is RA of
				ArrayList<VORecord> vos = model.getVOIApprove(auth.getContact().id);
				if(vos.size() > 0) {
					/*
					pane.add(new HtmlView("<h3>RA Agreement</h3>"));
					pane.add(new HtmlView("<p>You are currently RA for following VOs</p>"));
					pane.add(new HtmlView("<p>"));
					for(VORecord vo : vos) {
						pane.add(new HtmlView("<span class=\"label\">"+vo.name+"</span> "));
					}
					pane.add(new HtmlView("</p>"));
					pane.add(new HtmlView("<p>Therefore, you must agree to following RA agreement before renewing your certificate.</p>"));
					*/
					pane.add(new HtmlView("<div class=\"well\">"));
					pane.add(new HtmlFileView(getClass().getResourceAsStream("ra_agreement.html")));
					ra_agree.setLabel("I have read and agree to above.");
					ra_agree.addValidator(new MustbeCheckedValidator("You must agree before renewing your certificate."));			
					pane.add(ra_agree);
					pane.add(new HtmlView("</div>"));
				}
				
				GridAdminModel gamodel = new GridAdminModel(context);
				ArrayList<GridAdminRecord> gas = gamodel.getGridAdminsByContactID(auth.getContact().id);
				if(gas.size() > 0) {
					pane.add(new HtmlView("<div class=\"well\">"));
					pane.add(new HtmlFileView(getClass().getResourceAsStream("ga_agreement.html")));
					ga_agree.setLabel("I have read and agree to above.");
					ga_agree.addValidator(new MustbeCheckedValidator("You must agree before renewing your certificate."));			
					pane.add(ga_agree);
					pane.add(new HtmlView("</div>"));
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//TODO list domains that this user is GA of
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-success\"><i class=\"icon-refresh icon-white\"></i> Renew</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(pass.validate() && pass_confirm.validate() && ra_agree.validate() && ga_agree.validate()) {
                		context.setComment("User requesting / issueing renewed user certificate");
                		try {
                			//check access again - request status might have changed
                			if(model.canRenew(rec, logs)) {
                				model.renew(rec, pass.getValue());
	                			context.message(MessageType.SUCCESS, "Successfully renewed certificate request with ID: " + rec.id);
								button.redirect(url);
                			} else {
                				button.alert("Reques status has changed. Please reload.");
                			}
                		} catch (CertificateRequestException ex) {
                			String message = "Failed to renew certificate: " + ex.getMessage();
                			if(ex.getCause() != null) {
                				message += "\n\n" + ex.getCause().getMessage();
                			}
	                		button.alert(message);
	                	}
                	}
                }
            });
			pane.add(button);
			
			tabview.addtab("Renew", pane);
		}
		if(model.canRequestRevoke(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-exclamation-sign icon-white\"></i> Request Revocation</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		try {
                			//check access again - request status might have changed
                			if(model.canRequestRevoke(rec)) {
                				model.requestRevoke(rec);
	                			context.message(MessageType.SUCCESS, "Successfully requested certificate revocation for a request with ID: " + rec.id);
								button.redirect(url);
                			} else {
                				button.alert("Reques status has changed. Please reload.");
                			}
                		} catch (CertificateRequestException ex) {
                			String message = "Failed to request revocation: " + ex.getMessage();
                			if(ex.getCause() != null) {
                				message += "\n\n" + ex.getCause().getMessage();
                			}
	                		button.alert(message);
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Request Revoke", pane);
		}
		if(model.canIssue(rec)) {
			GenericView pane = new GenericView();
			
			//password
			if(rec.requester_passphrase == null) {
				pane.add(new HtmlView("<p class=\"help-block\">Please choose a password to encrypt your certificate / private key</p>"));
			} else {
				pane.add(new HtmlView("<p class=\"help-block\">Please enter the password you chose during a request submission to retrieve your certificate & encrypt your private key. If you don't remember, please read <a target=\"_blank\" href=\"https://confluence.grid.iu.edu/display/CENTRAL/Forgot+retrieval+password\">this doc.</a></p>"));
			}
			final DivRepPassword pass = new DivRepPassword(context.getPageRoot());
			pass.setLabel("Password");
			pass.setRequired(true);
			pane.add(pass);
			
			final DivRepPassword pass_confirm = new DivRepPassword(context.getPageRoot());
			pass_confirm.setLabel("Re-enter password");
			pass_confirm.setHidden(true);
			
			if(rec.requester_passphrase == null) {
				//new password - need to validate
				pass.addValidator(new PKIPassStrengthValidator());
				
				//let user confirm the new password.
				pass.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent event) {
						if(pass_confirm.getValue() != null) {
							pass_confirm.validate();
						}
					}
				});
				pass_confirm.setHidden(false);
				pass_confirm.addValidator(new DivRepIValidator<String>() {
					String message;
					@Override
					public Boolean isValid(String value) {
						if(value.equals(pass.getValue())) return true;
						message = "Password does not match";
						return false;
					}
	
					@Override
					public String getErrorMessage() {
						return message;
					}
				});
				pass_confirm.setRequired(true);
				pane.add(pass_confirm);
			} else {
				pass.addValidator(new DivRepIValidator<String>(){
					@Override
					public Boolean isValid(String value) {
						if(model.checkPassphrase(rec, value)) {
							return true;
						} else {
							return false;
						}
					}

					@Override
					public String getErrorMessage() {
						return "Passphrase is incorrect!";
					}});
			}
						
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificate ...</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(pass.validate() && pass_confirm.validate()) {
                		context.setComment("User requested to issue certificate");
                		//start process thread 
                		try {
                			//check access again - request status might have changed
                			if(model.canIssue(rec)) {
                				model.startissue(rec, pass.getValue());
	                			//context.message(MessageType.SUCCESS, "Successfully started issing a certificate for a request with ID: " + rec.id);
								button.redirect(url);
                			} else {
                				button.alert("Reques status has changed. Please reload.");
                			}
                    	} catch(CertificateRequestException ex) {
                    		log.warn("CertificateRequestException while issuging certificate -- request ID:"+rec.id, ex);
                			String message = "Failed to issue certificate: " + ex.getMessage();
                			if(ex.getCause() != null) {
                				message += "\n\n" + ex.getCause().getMessage();
                			}
	                		button.alert(message);
                    	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Issue", pane);
		}
		if(model.canCancelWithPass(rec)) {
			GenericView pane = new GenericView();
			Authorization auth = context.getAuthorization();
			
			pane.add(new HtmlView("<p class=\"help-block\">Please enter password used to submit this request in order to cancel this request.</p>"));
			final DivRepPassword pass = new DivRepPassword(context.getPageRoot());
			pass.setLabel("Password");
			pass.setRequired(true);
			//pass.addValidator(new PKIPassStrengthValidator());
			pane.add(pass);
						
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Cancel Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(pass.validate()/* && note.validate()*/) {
                		//context.setComment(note.getValue());
                		context.setComment("Submitter canceled request.");
                		try {
                			//check access again - request status might have changed
                			if(model.canCancelWithPass(rec)) {
                				model.cancelWithPass(rec, pass.getValue());
	                			context.message(MessageType.SUCCESS, "Successfully canceled a certificate request with ID: " + rec.id);
								button.redirect(url);
                			} else {
                				button.alert("Reques status has changed. Please reload.");
                			}
                    	} catch(CertificateRequestException ex) {
                    		log.warn("CertificateRequestException while canceling certificate request:", ex);
                			String message = "Failed to cancel request: " + ex.getMessage();
                			if(ex.getCause() != null) {
                				message += "\n\n" + ex.getCause().getMessage();
                			}
	                		button.alert(message);
                    	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Cancel", pane);
		}
		if(model.canCancel(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn\">Cancel Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                	try {
	            			//check access again - request status might have changed
		        			if(model.canCancel(rec)) {
		                    	model.cancel(rec);
	                			context.message(MessageType.SUCCESS, "Successfully canceled a certificate request with ID: " + rec.id);
								button.redirect(url);
		        			} else {
		        				button.alert("Reques status has changed. Please reload.");
		        			}
	                	} catch(CertificateRequestException ex) {
	                		log.warn("CertificateRequestException while canceling certificate request:", ex);
	            			String message = "Failed to cancel request: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		button.alert(message);
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Cancel", pane);
		}
		if(model.canReject(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
		
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-remove icon-white\"></i> Reject Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		try {
                			//check access again - request status might have changed
                			if(model.canReject(rec)) {
                				model.reject(rec);
	                			context.message(MessageType.SUCCESS, "Successfully rejected a certificate request with ID: " + rec.id);
								button.redirect(url);
                			} else {
    	        				button.alert("Reques status has changed. Please reload.");
                			}
                		} catch (CertificateRequestException e) {
                    		log.warn("CertificateRequestException while rejecting certificate request:", e);
                			String message = "Failed to cancel request: " + e.getMessage();
                			if(e.getCause() != null) {
                				message += "\n\n" + e.getCause().getMessage();
                			}
                    		button.alert(message);
                		}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Reject", pane);
		}
		if(model.canRevoke(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-exclamation-sign icon-white\"></i> Revoke</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	//button.alert("Currently we can not revoke user certificate. We are waiting for DigiCert to provide us the user certificate revocation API.");
                
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		try {
                			//check access again - request status might have changed
                			if(model.canRevoke(rec)) {
                				model.revoke(rec);
	                			context.message(MessageType.SUCCESS, "Successfully revoked a certificate request with ID: " + rec.id);
								button.redirect(url);
                			} else {
    	        				button.alert("Reques status has changed. Please reload.");
                			}
                		} catch (CertificateRequestException ex) {
                			String message = "Failed to revoke: " + ex.getMessage();
                			if(ex.getCause() != null) {
                				message += "\n\n" + ex.getCause().getMessage();
                			}
	                		button.alert(message);
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Revoke", pane);
		}
		
		if(model.canReRequest(rec)) {
			final Authorization auth = context.getAuthorization();
			GenericView pane = new GenericView();
			
			//allow guest to re-request with retrieval password
			final ChoosePassword pass = new ChoosePassword(context.getPageRoot(), context);
			if(!auth.isUser()) {
				pane.add(new HtmlView("<p class=\"help-block\">If you are the original requester of this request, you can re-request to issue another certificate with the same CN.</p>"));
				pane.add(pass);
			}
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-refresh icon-white\"></i> Re-request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		
                		//guest must provide password
                		if(!auth.isUser()) {
                			if(!pass.validate()) return;
                		}
                		
                		try {
                			//check access again - request status might have changed
    	        			if(model.canReRequest(rec)) {
    	        				model.rerequest(rec, pass.getValue());
	                			context.message(MessageType.SUCCESS, "Successfully re-requested a certificate request with ID: " + rec.id);
								button.redirect(url);
    	        			} else {
    	        				button.alert("Reques status has changed. Please reload.");
    	        			}
                		} catch (CertificateRequestException ex) {
	                		button.alert("Failed to re-request: " + ex.getMessage());
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Re-Request", pane);
		}
		
		if(tabview.size() == 0) {
			if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
				v.add(new HtmlView("<p class=\"alert alert-warning\">Certificate is being issued. Please wait for a few minutes..</p>"));				
			} else {
				v.add(new HtmlView("<p class=\"alert alert-warning\">You can not perform any action on this certificate. Please contact GOC for assistance.</p>"));
			}
		}
		
		v.add(tabview);
		return v;
	}
	
	protected IView createListView(final UserContext context) throws ServletException
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
				//IDForm form = new IDForm(context.getPageRoot());
				//form.render(out);
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
