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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepTextArea;

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
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.ContentView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;

public class CertificateUserServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateUserServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);

		BootMenuView menuview = new BootMenuView(context, "certificate");
		IView content = null;
		String dirty_id = request.getParameter("id");

		if(dirty_id != null) {
			try {
				int id = Integer.parseInt(dirty_id);
				UserCertificateRequestModel model = new UserCertificateRequestModel(context);
				CertificateRequestUserRecord rec = model.get(id);
				if(!model.canView(rec)) {
					throw new AuthorizationException("You don't have access to view this certificate");
				}
				ArrayList<UserCertificateRequestModel.Log> logs = model.getLogs(id);
				content = createDetailView(context, rec, logs);
			} catch (SQLException e) {
				throw new ServletException("Failed to load specified certificate", e);
			}
		} else {
			content = createListView(context);
		}
		
		BootPage page = new BootPage(context, menuview, content, null);
		page.render(response.getWriter());
	}
	
	protected IView createDetailView(final UserContext context, final CertificateRequestUserRecord rec, final ArrayList<UserCertificateRequestModel.Log> logs) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				
				/*
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("User Certificat Requests", "certificate");
				bread_crumb.addCrumb(Integer.toString(rec.id),  null);
				bread_crumb.render(out);
				*/
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView("user_requests");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
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
				out.write("<th>DN</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.dn)+"</td>");
				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th>Status</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.status));
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
					out.write("<script>setTimeout('window.location.reload()', 3000);</script>");
				}
				out.write("</td>");
				out.write("</tr>");

				out.write("<tr>");
				out.write("<th>Requester</th>");
				try {
					ContactModel cmodel = new ContactModel(context);
					ContactRecord requester = cmodel.get(rec.requester_contact_id);
					out.write("<td>"+StringEscapeUtils.escapeHtml(requester.name)+" ("+StringEscapeUtils.escapeHtml(requester.primary_email)+")</td>");

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
				
				if(rec.cert_pkcs7 != null) {
					out.write("<tr>");
					out.write("<th>Certificates</th>");
					out.write("<td>");
					HttpSession session = context.getSession();

					UserCertificateRequestModel model = new UserCertificateRequestModel(context);
					if(model.getPrivateKey(rec.id) != null) {
						out.write("<a href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs12\">Download PKCS12</a>");
					} 
					out.write("<ul>");
					out.write("<li><a href=\"certificatedownload?id="+rec.id+"&type=user&download=pkcs7\">Download PKCS7</a></li>");
					out.write("</ul>");
					out.write("</td>");
					out.write("</tr>");
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
					out.write("<td>N/A</td>");
				}
				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th>Next Action</th>");
				out.write("<td>");
				GenericView action_control = nextActionControl(context, rec);
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
				for(UserCertificateRequestModel.Log log : logs) {
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificate ...</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
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
	
	protected IView createListView(final UserContext context) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		
		if(!auth.isUser()) {
			throw new AuthorizationException("Sorry, this page is only for logged in users.");
		}
		
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView("certificateuser");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				renderList(out);
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
			}
			
			public void renderList(PrintWriter out) {
				UserCertificateRequestModel usermodel = new UserCertificateRequestModel(context);
				out.write("<table class=\"table certificate\">");
				out.write("<thead><tr><th>ID</th><th>Status</th><th>GOC Ticket</th><th>DN</th><th>VO</th></tr></thead>");
				try {
					ArrayList<CertificateRequestUserRecord> recs = usermodel.getMine(auth.getContact().id);
					out.write("<tbody>");
					for(CertificateRequestUserRecord rec : recs) {
						out.write("<tr onclick=\"document.location='certificateuser?id="+rec.id+"\">");
						out.write("<td>USER"+rec.id+"</td>");
						out.write("<td>"+rec.status+"</td>");
						//TODO - use configured goc ticket URL
						out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
						out.write("<td>"+rec.dn+"</td>");
						try {
							VOModel vomodel = new VOModel(context);
							VORecord vo = vomodel.get(rec.vo_id);
							out.write("<td>"+vo.name+"</td>");
						} catch (SQLException e) {
							out.write("<td>sql error</td>");
						}
						out.write("</tr>");	
					}
					out.write("</tbody>");
				} catch (SQLException e1) {
					out.write("<div class=\"alert\">Failed to load my certificate requests</div>");
					log.error(e1);
				}
				
				out.write("</table>");
				out.write("</div>");//content
			}
		};
	}

}
