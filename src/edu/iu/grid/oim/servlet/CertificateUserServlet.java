package edu.iu.grid.oim.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase.LogDetail;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.UserCertificateTable;
import edu.iu.grid.oim.view.divrep.CNEditor;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class CertificateUserServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateUserServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);

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
				IView view = statusView(rec);
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
				}
			} else {
				//display list view
				content = createListView(context);
			}
			
			BootPage page = new BootPage(context, menuview, content, null);
			page.render(response.getWriter());
		}
	}
	
	protected IView statusView(final CertificateRequestUserRecord rec) {
		return new IView() {

			@Override
			public void render(PrintWriter out) {
			
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					String gencsr_class = "progressing";
					if(rec.csr != null) {
						gencsr_class = "completed";
					}
					String sign_class = "notstarted";
					if(rec.csr != null && rec.cert_pkcs7 == null) {
						sign_class = "progressing";
					}
					out.write("<ul class=\"progress_display\">");
					out.write("<li class=\""+gencsr_class+"\">Generating CSR/Private Key</li>");
					out.write("<li class=\""+sign_class+"\">Signing Certificate</li>");
					out.write("</ul>");
				} else {
					//not issuing anymore - redirect
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
				
				/*
				if(rec.id.equals(userrec.id)) {
					out.write("<div class=\"alert alert-info\">You are currently logged in using certificate issued by this user certificate request.</div>");
				}
				*/
			
				renderDetail(out);
				renderLog(out);
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
				
				out.write("</div>"); //content
			}
			
			public void renderDetail(PrintWriter out) {
				
				out.write("<table class=\"table nohover\">");
				out.write("<tbody>");

				out.write("<tr>");
				out.write("<th style=\"min-width: 100px;\">DN</th>");
				out.write("<td>");
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
				CNEditor cn_override = null;
				if(model.canOverrideCN(rec)) {
					cn_override = new CNEditor(context.getPageRoot());
					cn_override.setRequired(true);
					cn_override.setValue(rec.getCN());
					//cn_override.setDisabled(false);
					cn_override.render(out);
				} else {
					out.write(StringEscapeUtils.escapeHtml(rec.dn));	
				}
				
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
				out.write("<th>Status</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.status));
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					out.write("<div id=\"status_progress\">Loading...</div>");
					out.write("<script>");
					out.write("function loadstatus() { ");
					out.write("$('#status_progress').load('certificateuser?id="+rec.id+"&status');");
					out.write("setTimeout('loadstatus()', 1000);");
					out.write("}");
					out.write("loadstatus();");
					out.write("</script>");
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
				out.write("<th>GOC Ticket</th>");
				out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
				out.write("</tr>");
				
				if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
					out.write("<tr>");
					out.write("<th>Certificates</th>");
					out.write("<td>");
					HttpSession session = context.getSession();

					if(model.getPrivateKey(rec.id) != null) {
						out.write("<p><a class=\"btn btn-primary\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs12\">Download Certificate &amp; Private Key (PKCS12)</a></p>");
						//out.write("<a class=\"btn btn-primary\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pem12\">Download Certificate &amp; Private Key (PEM)</a>");
						out.write("<p class=\"alert\">You need to download your certificate and private key now, while your browser session is active. If your session times out, the server will delete your private key for security reasons and you will need to request a new certificate.</p>");
						
					} else {
						out.write("<p><a class=\"btn btn-primary\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs7\">Download Certificate (PKCS7)</a> ");
						out.write("<a class=\"btn\" href=\"certificatedownload?id="+rec.id+"&type=user&download=pem7\">Download Certificate (PEM)</a>");
						out.write("</p>");
					}
					
					String urlformat = "https://confluence.grid.iu.edu/display/CENTRAL/Importing+User+Certificate+on+{0}";
					String urlformat_in = new Base64(Integer.MAX_VALUE).encodeToString(urlformat.getBytes());
					out.write("<p><a target=\"_blank\" href=\"browserjump?urlformat="+urlformat_in+"\">How to import user certificate on your browser</a></p>");
					
					out.write("<p><a target=\"_blank\" href=\"https://confluence.grid.iu.edu/display/CENTRAL/Importing+User+Certificate+for+Command+Line+Use\">How to import user certificate for command line use (grid-proxy-init).</a></p>");
					
					//https://confluence.grid.iu.edu/display/CENTRAL/Importing+User+Certificate+for+Command+Line+Use
					out.write("</td>");
					out.write("</tr>");
					
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
						out.write("<ul>");
						for(ContactRecord ra : ras) {
							if(auth.isUser()) {
								out.write("<li>");
								out.write("<a href=\"mailto:"+ra.primary_email+"\">"+StringEscapeUtils.escapeHtml(ra.name)+"</a>");
								out.write(" Phone: "+ra.primary_phone+"</li>");
							} else {
								out.write("<li>"+ra.name+"</li>");
							}
						}
						out.write("</ul>");
						out.write("</td>");	
						
					}
					out.write("</tr>");
				} catch (SQLException e) {
					out.write("<td>sql error</td>");
				}
				
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
				
				
				GenericView action_control = nextActionControl(context, rec, cn_override);
				out.write("<tr>");
				out.write("<th>Next Action</th>");
				out.write("<td>");
				action_control.render(out);
				out.write("</td>");
				out.write("</tr>");
				
				
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
	
	protected GenericView nextActionControl(final UserContext context, final CertificateRequestUserRecord rec, final CNEditor cn_override) {
		GenericView v = new GenericView();
		
		if(rec.status.equals(CertificateRequestStatus.REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED)) {
			v.add(new HtmlView("<p class=\"alert alert-info\">RA to approve certificate request</p>"));
		} else if(rec.status.equals(CertificateRequestStatus.APPROVED)) {
			v.add(new HtmlView("<p class=\"alert alert-info\">Requester to issue certificate & download</p>"));
		} else if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
			v.add(new HtmlView("<p class=\"alert alert-info\">Requester to issue certificate & download</p>"));
		} else if(rec.status.equals(CertificateRequestStatus.REJECTED) ||
				rec.status.equals(CertificateRequestStatus.REVOKED) ||
				rec.status.equals(CertificateRequestStatus.EXPIRED) ||
				rec.status.equals(CertificateRequestStatus.CANCELED)
				) {
			//v.add(new HtmlView("<p class=\"alert alert-info\">Re-request</p>"));
		}  else if(rec.status.equals(CertificateRequestStatus.FAILED)) {
			v.add(new HtmlView("<p class=\"alert alert-info\">GOC engineer to troubleshoot & resubmit</p>"));
		} else if(rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
			v.add(new HtmlView("<p class=\"alert alert-info\">RA to revoke certificate</p>"));
		} else if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
			v.add(new HtmlView("<p class=\"alert alert-info\">Please wait for a fews seconds.</p>"));
		}
		
		final String url = "certificateuser?id="+rec.id;
		
		final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
		note.setHeight(40);
		note.setLabel("Action Note");
		note.setRequired(true);
		note.setHidden(true);
		v.add(note);
	
		
		//controls
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		if(model.canApprove(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Approve</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		
                		if(model.canOverrideCN(rec)) {
                			if(cn_override.validate()) {
		                		//Regenerate DN using provided CN
		                		X500Name name = model.generateDN(cn_override.getValue());
		                		rec.dn = CertificateManager.RFC1779_to_ApacheDN(name.toString());
		                		
		                		//make sure we don't have duplicate CN requested already.
								try {
			                		CertificateRequestUserRecord duplicate = model.getByDN(rec.dn);
			                		if(duplicate != null && !duplicate.id.equals(rec.id)) {
			                			button.alert("The same DN is already used by U"+duplicate.id + ". Please specify different CN");
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
                		
	                	if(model.approve(rec)) {
	                		button.redirect(url);
	                	} else {
	                		button.alert("Failed to approve request. Maybe Quota exeeded?");
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		if(model.canRequestRenew(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-refresh icon-white\"></i> Request Renew</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		try {
                			model.requestRenew(rec);
                			button.redirect(url);
                		} catch (CertificateRequestException ex) {
	                		button.alert("Failed to request renewal: " + ex.getMessage());
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
                		try {
                			model.requestRevoke(rec);
                			button.redirect(url);
                		} catch (CertificateRequestException ex) {
	                		button.alert("Failed to request revoke: " + ex.getMessage());
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		if(model.canIssue(rec)) {
			Authorization auth = context.getAuthorization();
			
			if(rec.requester_passphrase != null) {
				v.add(new HtmlView("<p class=\"help-block\">Please enter password to retrieve & encrypt your new certificate (pkcs12)</p>"));
			} else {
				v.add(new HtmlView("<p class=\"help-block\">Please choose a password to associate with your new certificate (pkcs12)</p>"));
			}
			
			final DivRepPassword pass = new DivRepPassword(context.getPageRoot());
			pass.setLabel("Password");
			pass.setRequired(true);
			v.add(pass);
			
			if(rec.requester_passphrase == null) {
				//new password - need to validate
				pass.addValidator(new PKIPassStrengthValidator());
				
				//let user confirm the new password.
				final DivRepPassword pass_confirm = new DivRepPassword(context.getPageRoot());
				pass.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						if(pass_confirm.getValue() != null) {
							pass_confirm.validate();
						}
					}
				});
				pass_confirm.setLabel("Re-enter password");
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
					}});
				pass_confirm.setRequired(true);
				v.add(pass_confirm);
			}
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificate ...</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(pass.validate()) {
                		context.setComment("User requested to issue certificate");
                		//start process thread 
                		try {
	                      	model.startissue(rec, pass.getValue());
	                    	button.redirect(url);
                    	} catch(CertificateRequestException ex) {
                    		log.warn("CertificateRequestException while issuging certificate:", ex);
                    		button.alert(ex.getMessage());
                    	}
                	}
                }
            });
			v.add(button);
		}
		if(model.canCancelWithPass(rec)) {
			Authorization auth = context.getAuthorization();
			
			v.add(new HtmlView("<p class=\"help-block\">Please enter password used to submit this request in order to cancel this request.</p>"));
			final DivRepPassword pass = new DivRepPassword(context.getPageRoot());
			pass.setLabel("Password");
			pass.setRequired(true);
			//pass.addValidator(new PKIPassStrengthValidator());
			v.add(pass);
						
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Cancel Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(pass.validate()/* && note.validate()*/) {
                		//context.setComment(note.getValue());
                		context.setComment("Submitter canceled request.");
                		try {
	                      	model.cancelWithPass(rec, pass.getValue());
	                    	button.redirect(url);
                    	} catch(CertificateRequestException ex) {
                    		log.warn("CertificateRequestException while canceling certificate request:", ex);
                    		button.alert(ex.getMessage());
                    	}
                	}
                }
            });
			v.add(button);
			//note.setHidden(false);
		}
		if(model.canCancel(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn\">Cancel Request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	//if(note.validate()) {
            		//context.setComment(note.getValue());
                	context.setComment("Submitter or RA canceled request.");
                	if(model.cancel(rec)) {
                		button.redirect(url);
                	} else {
                		button.alert("Failed to cancel");
                	}
                	//}
                }
            });
			v.add(button);
			//note.setHidden(false);
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
	                		button.alert("Failed to reject");
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
                	//button.alert("Currently we can not revoke user certificate. We are waiting for DigiCert to provide us the user certificate revocation API.");
                
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		try {
                			model.revoke(rec);
                			button.redirect(url);
                		} catch (CertificateRequestException ex) {
	                		button.alert("Failed to revoke: " + ex.getMessage());
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		if(model.canReRequest(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-refresh icon-white\"></i> Re-request</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
                		try {
                			model.rerequest(rec);
                			button.redirect(url);
                		} catch (CertificateRequestException ex) {
	                		button.alert("Failed to re-request: " + ex.getMessage());
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		
		return v;
	}
	
	protected IView createListView(final UserContext context) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		/*
		//guest has to enter request ID
		class IDForm extends DivRep {
			final DivRepTextBox id;
			final DivRepButton open;
			public IDForm(DivRep parent) {
				super(parent);
				id = new DivRepTextBox(this);
				//id.setLabel("Open by Request ID");
				id.setWidth(150);
				open = new DivRepButton(this, "Open");
				open.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent e) {
						if(id.getValue() == null || id.getValue().trim().isEmpty()) {
							alert("Please enter request ID to open");
						} else {
							redirect("certificateuser?id="+id.getValue());
						}
					}
				});
				open.addClass("btn");
			}
	
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"pull-right\">");
				//out.write("<p>Please enter host certificate request ID to view details</p>");
				
				out.write("<table><tr>");
				out.write("<td>Open By Request ID </td>");
				out.write("<td>");
				id.render(out);
				out.write("</td>");
				out.write("<td style=\"vertical-align: top;\">");
				open.render(out);
				out.write("</td>");
				out.write("</tr></table>");
				
				out.write("</div>");
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}
		};
		*/
		
		
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
