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

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;

public class CertificateHostServlet extends ServletBase  {
	private static final long serialVersionUID = 1L;
    static Logger log = Logger.getLogger(CertificateHostServlet.class);  

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		UserContext context = new UserContext(request);
		
		CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		BootMenuView menuview = new BootMenuView(context, "certificate");
		IView content = null;
		String dirty_id = request.getParameter("id");
		String status = request.getParameter("status");
		
		if(status != null && dirty_id != null) {
			//display status
			int id = Integer.parseInt(dirty_id);
			CertificateRequestHostRecord rec;
			try {
				rec = model.get(id);
				IView view = statusView(rec);
				view.render(response.getWriter());
			} catch (SQLException e) {
				throw new ServletException("Failed to load specified certificate", e);
			}
		} else {
			if(dirty_id != null) {
				try {
					int id = Integer.parseInt(dirty_id);
					CertificateRequestHostRecord rec = model.get(id);
					if(rec == null) {
						throw new ServletException("No request found with a specified request ID.");
					}
					if(!model.canView(rec)) {
						throw new AuthorizationException("You don't have access to view this certificate");
					}
					ArrayList<CertificateRequestModelBase<CertificateRequestHostRecord>.LogDetail> logs = model.getLogs(CertificateRequestHostModel.class, id);
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
	}
	
	protected IView statusView(final CertificateRequestHostRecord rec) {
		return new IView() {
			@Override
			public void render(PrintWriter out) {
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					//count number of certificate issued
					int issued = 0;
					String[] pkcs7s = rec.getPKCS7s();
					for(String pkcs7 : pkcs7s) {
						if(pkcs7 != null) issued++;
					}
					out.write("Certificates issued so far: "+issued+" of "+pkcs7s.length);
					out.write("<div class=\"progress active\">");
					out.write("<div class=\"bar\" style=\"width: 40%;\"></div>");
					out.write("</div>");
				} else {
					//not issuing anymore - redirect
					out.write("<script>document.location='certificatehost?id="+rec.id+"';</script>");
				}
			}
		};
	}
	
	protected IView createDetailView(
			final UserContext context, 
			final CertificateRequestHostRecord rec, 
			final ArrayList<CertificateRequestModelBase<CertificateRequestHostRecord>.LogDetail> logs) throws ServletException
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
 				CertificateMenuView menu = new CertificateMenuView(context, "certificatehost");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				BootBreadCrumbView bread_crumb = new BootBreadCrumbView();
				bread_crumb.addCrumb("Host Certificat Requests", "certificatehost");
				bread_crumb.addCrumb(Integer.toString(rec.id),  null);
				bread_crumb.render(out);		
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
				out.write("<th>Status</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.status));
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					out.write("<div id=\"status_progress\">Loading...</div>");
					out.write("<script>");
					out.write("function loadstatus() { ");
					out.write("$('#status_progress').load('certificatehost?id="+rec.id+"&status');");
					out.write("setTimeout('loadstatus()', 1000);");
					out.write("}");
					out.write("loadstatus();");
					out.write("</script>");
				}
				out.write("</td>");
				out.write("</tr>");

				out.write("<tr>");
				out.write("<th>Grid Admin</th>");
				try {
					ContactModel cmodel = new ContactModel(context);
					ContactRecord requester = cmodel.get(rec.gridadmin_contact_id);
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
				
				out.write("<tr>");
				out.write("<th>FQDNs</th>");
				
				out.write("<td>");
				String[] cns = rec.getCNs();
				out.write("<ul>");
				int i = 0;
				for(String cn : cns) {
					out.write("<li>"+StringEscapeUtils.escapeHtml(cn));
					if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
						out.write(" <a href=\"certificatedownload?id="+rec.id+"&type=host&download=pkcs7&idx="+i+"\">Download PKCS7</a>");
					}
					out.write("</li>");
				}
				out.write("</ul>");
				out.write("</td>");
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
				for(CertificateRequestModelBase<CertificateRequestHostRecord>.LogDetail log : logs) {
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
	
	protected GenericView nextActionControl(final UserContext context, final CertificateRequestHostRecord rec) {
		GenericView v = new GenericView();
		
		if(rec.status.equals(CertificateRequestStatus.REQUESTED) ||
				rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
				rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
				v.add(new HtmlView("<p class=\"alert alert-info\">GridAdmin to approve request</p>"));
			} else if(rec.status.equals(CertificateRequestStatus.APPROVED)) {
				v.add(new HtmlView("<p class=\"alert alert-info\">Requester to issue certificate & download</p>"));
			} else if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
				v.add(new HtmlView("<p class=\"alert alert-info\">Requester to download certificate</p>"));
			} else if(rec.status.equals(CertificateRequestStatus.REJECTED) ||
					rec.status.equals(CertificateRequestStatus.REVOKED) ||
					rec.status.equals(CertificateRequestStatus.EXPIRED)
					) {
				v.add(new HtmlView("<p class=\"alert alert-info\">No further action.</p>"));
			}  else if(rec.status.equals(CertificateRequestStatus.FAILED)) {
				v.add(new HtmlView("<p class=\"alert alert-info\">GOC engineer to troubleshoot & resubmit</p>"));
			}
		
		final String url = "certificatehost?id="+rec.id;
		
		final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
		note.setLabel("Action Note");
		note.setRequired(true);
		note.setHidden(true);
		v.add(note);
		
		//controls
		final CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		if(model.canApprove(rec)) {
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Approve</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                	try {
							model.approve(rec);
							button.redirect(url);
						} catch (CertificateRequestException e1) {
							log.error("Failed to approve host certificate", e1);
							button.alert(e1.toString());
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
	                  	try {
	                  		model.requestRenew(rec);
	                		button.redirect(url);
	                	} catch (CertificateRequestException e1) {
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
                		try {
                			model.requestRevoke(rec);
	                		button.redirect(url);
	                	} catch (CertificateRequestException e1) {
	                		button.alert("Failed to request revoke request");
	                	}
                	}
                }
            });
			v.add(button);
			note.setHidden(false);
		}
		
		if(model.canIssue(rec)) {
			//Authorization auth = context.getAuthorization();

			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificates</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent e) {
            		try {
                      	model.startissue(rec);
                    	button.redirect(url);
                	} catch(CertificateRequestException ex) {
                		log.warn("CertificateRequestException while issuging certificate:", ex);
                		button.alert(ex.getMessage());
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
                	try {
                		model.cancel(rec);
                		button.redirect(url);
                	} catch (CertificateRequestException e1) {
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
	                  	try {
	                  		model.reject(rec);
	                		button.redirect(url);
	                	} catch (CertificateRequestException e1) {
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
	                 	try {
	                 		model.revoke(rec);
	                		button.redirect(url);
	                	} catch (CertificateRequestException e1) {
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
		
		//guest has to enter request ID
		class IDForm extends DivRepForm {
			final DivRepTextBox id;
			public IDForm(DivRep parent) {
				super(parent, null);
				new DivRepStaticContent(this, "<p>Please enter host certificate request ID to view details</p>");
				id = new DivRepTextBox(this);
				id.setLabel("Request ID");
				id.setRequired(true);
				
				setSubmitLabel("Open");
			}
			
			@Override
			protected Boolean doSubmit() {
				redirect("certificatehost?id="+id.getValue());
				return true;
			}
		};
		
		return new IView(){
			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\"content\">");
				
				out.write("<div class=\"row-fluid\">");
				
				out.write("<div class=\"span3\">");
				CertificateMenuView menu = new CertificateMenuView(context, "certificatehost");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				if(auth.isUser()) {
					renderMyList(out);
				} else {
					IDForm form = new IDForm(context.getPageRoot());
					form.render(out);
				}
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
			}
			
			public void renderMyList(PrintWriter out) {
				CertificateRequestHostModel model = new CertificateRequestHostModel(context);
				ContactModel cmodel = new ContactModel(context);
				out.write("<table class=\"table certificate\">");
				out.write("<thead><tr><th>ID</th><th>Status</th><th>GOC Ticket</th><th>FQDNs</th><th>Grid Admin</th></tr></thead>");
				try {
					ArrayList<CertificateRequestHostRecord> recs = model.getMine(auth.getContact().id);
					out.write("<tbody>");
					for(CertificateRequestHostRecord rec : recs) {
						out.write("<tr onclick=\"document.location='certificatehost?id="+rec.id+"';\">");
						out.write("<td>HOST"+rec.id+"</td>");
						out.write("<td>"+rec.status+"</td>");
						//TODO - use configured goc ticket URL
						out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
	
						//fqdns
						String[] cns = rec.getCNs();
						int idx = 0;
						out.write("<td><ul>");
						for(String cn : cns) {
							out.write("<li>"+cn+"</li>");
							idx++;
							if(idx > 5) {
								out.write("<li>... <span class=\"badge badge-info\">Total "+cns.length+"</span></li>");
								break;
							}
							
						}
						out.write("</ul></td>");
						
						ContactRecord gridadmin = cmodel.get(rec.gridadmin_contact_id);
						out.write("<td>"+StringEscapeUtils.escapeHtml(gridadmin.name)+"</td>");
						
						/*
						try {
							VOModel vomodel = new VOModel(context);
							VORecord vo = vomodel.get(rec.vo_id);
							out.write("<td>"+vo.name+"</td>");
						} catch (SQLException e) {
							out.write("<td>sql error</td>");
						}
						*/
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
