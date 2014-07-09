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
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.AuthorizationException;
import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.BootBreadCrumbView;
import edu.iu.grid.oim.view.BootMenuView;
import edu.iu.grid.oim.view.BootPage;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HostCertificateTable;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.EditableContent;

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
				log.info("Failed to load specified certificate", e);
				return;
			}
		} else {
			if(dirty_id != null) {
				try {
					int id = Integer.parseInt(dirty_id);
					CertificateRequestHostRecord rec = model.get(id);
					if(rec == null) {
						log.info("No request found with a specified request ID.");
						return;
					}
					if(!model.canView(rec)) {
						throw new AuthorizationException("You don't have access to view this certificate");
					}
					ArrayList<CertificateRequestModelBase<CertificateRequestHostRecord>.LogDetail> logs = model.getLogs(CertificateRequestHostModel.class, id);
					String submenu = "certificatehost";
					if(request.getParameter("search") != null) {
						submenu = "certificatesearchhost";
					}
					content = createDetailView(context, rec, logs, submenu);
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
					String[] statuses = rec.getStatuses();
					for(String status : statuses) {
						if(status.equals(CertificateRequestStatus.ISSUED)) issued++;
					}
					int percent = issued*100/statuses.length;
					out.write("<div class=\"alert alert-info\">");
					out.write("<p><img src=\"images/loading.gif\"/> <span>"+issued+" of "+statuses.length+" certificates issued..</p>");
					out.write("<div class=\"progress progress-striped active\">");
					out.write("<div class=\"bar\" style=\"width: "+percent+"%;\"></div>");
					out.write("</div>"); //progress
					out.write("</div>"); //alert
									
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
			final ArrayList<CertificateRequestModelBase<CertificateRequestHostRecord>.LogDetail> logs,
			final String submenu) throws ServletException
	{
		final Authorization auth = context.getAuthorization();
		final SimpleDateFormat dformat = new SimpleDateFormat();
		dformat.setTimeZone(auth.getTimeZone());
		final CertificateRequestHostModel model = new CertificateRequestHostModel(context);
		
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
				bread_crumb.addCrumb("Host Certificate Requests", "certificatehost");
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
				Config home_content = config.new Config(config, "certificate_host", "");
				Authorization auth = context.getAuthorization();
				if(auth.allows("admin") || auth.allows("admin_gridadmin")) {
					EditableContent content = new EditableContent(context.getPageRoot(), context, home_content);
					content.render(out);
				} else {
					out.write(home_content.getString());
				}
				
				out.write("<table class=\"table nohover\">");
				out.write("<tbody>");
						
				out.write("<tr>");
				out.write("<th>Request Status</th>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(rec.status));
				if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
					out.write("<div id=\"status_progress\">Loading...</div>");
					out.write("<script>");
					out.write("function loadstatus() { ");
					out.write("$('#status_progress').load('certificatehost?id="+rec.id+"&status');");
					out.write("setTimeout('loadstatus()', 2000);");
					out.write("}");
					out.write("loadstatus();");
					out.write("</script>");
				}
				out.write("</td>");
				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th>FQDNs</th>");
				out.write("<td>");
				String[] cns = rec.getCNs();
				String[] statuses = rec.getStatuses();
				String[] serial_ids = rec.getSerialIDs();
				/*
				if(rec.status.equals(CertificateRequestStatus.ISSUED)) {
					serial_ids = rec.getSerialIDs();
				}
				*/
				out.write("<table class=\"table table-bordered table-striped\">");
				out.write("<thead><tr><th>CN</th><th>Cert. Status</th><th>Certificates</th><th>Serial Number</th></tr></thead>");
				int i = 0;
				out.write("<tbody>");
				for(String cn : cns) {
					out.write("<tr>");
					out.write("<th>"+StringEscapeUtils.escapeHtml(cn));
					out.write("<a class=\"muted pull-right\" href=\"certificatedownload?id="+rec.id+"&type=host&download=pkcs10&idx="+i+"\">CSR</a>");
					out.write("</th>");
					out.write("<td>"+statuses[i]+"</td>");
					if(serial_ids != null && serial_ids[i] != null && !serial_ids[i].isEmpty()) {
						//out.write("<td><a href=\"certificatedownload?id="+rec.id+"&type=host&download=pkcs7&idx="+i+"\">Download PKCS7</a></td>");
						out.write("<td><a href=\"certificatedownload?id="+rec.id+"&type=host&download=x509&idx="+i+"\">Download PEM</a></td>");
						out.write("<td>"+serial_ids[i]);
						if(model.canRevoke(rec, i)) {
							class RevokeButton extends DivRepButton {
								int idx;
								public RevokeButton(DivRep parent, int idx) {
									super(parent, "Revoke");
									addClass("btn");
									addClass("btn-mini");
									addClass("pull-right");
									this.idx = idx;
								}
								protected void onClick(DivRepEvent e) {
									try {
										model.revoke(rec, idx);
										js("location.reload();");  //TODO - use DivRep for FQDN indicator?
									} catch (CertificateRequestException e1) {
										alert(e1.getMessage());
									}
								}
							}
							RevokeButton button = new RevokeButton(context.getPageRoot(), i);
							button.render(out);
						} /*else if(model.canRequestRevoke(rec)) {
							class RevokeButton extends DivRepButton {
								int idx;
								public RevokeButton(DivRep parent, int idx) {
									super(parent, "Request Revoke");
									addClass("btn");
									addClass("btn-mini");
									addClass("pull-right");
									this.idx = idx;
								}
								protected void onClick(DivRepEvent e) {
									//TODO - request revoke
									System.out.println("click revoke on " + idx);
								}
							}
							RevokeButton button = new RevokeButton(context.getPageRoot(), i);
							button.render(out);
						}*/
						out.write("</td>");
					} else {
						out.write("<td colspan=\"3\"><span class=\"muted\">N/A</span></td>");
					}
					out.write("</tr>");
					++i;
				}
				out.write("</tbody></table>");
				out.write("</td>");
				out.write("</tr>");
			
				out.write("<tr>");
				out.write("<th>Action</th>");
				out.write("<td>");
				GenericView action_control = nextActionControl(context, rec);
				action_control.render(out);
				out.write("</td>");
				out.write("</tr>");

				out.write("<tr>");
				out.write("<th>Requester</th>");
				try {
					ContactModel cmodel = new ContactModel(context);
					if(rec.requester_contact_id != null) {
						ContactRecord requester = cmodel.get(rec.requester_contact_id);
						out.write("<td>");
						if(auth.isUser()) {
							out.write("<b>"+StringEscapeUtils.escapeHtml(requester.name)+"</b>");
							out.write(" <code><a href=\"mailto:"+requester.primary_email+"\">"+requester.primary_email+"</a></code>");
							out.write(" Phone: "+requester.primary_phone);
						} else {
							out.write(StringEscapeUtils.escapeHtml(requester.name)+"</td>");
						}
						out.write("</td>");
					} else {
						out.write("<td><span class=\"label label-warning\">Unconfirmed</span> "+rec.requester_name+"</td>");
					}

				} catch (SQLException e1) {
					out.write("<td>(sql error)</td>");
				}
				out.write("</tr>");
								
				out.write("<tr>");
				out.write("<th>Requested Time</th>");
				out.write("<td>"+dformat.format(rec.request_time)+"</td>");
				out.write("</tr>");
				
				out.write("<tr>");
				out.write("<th>Grid Admins</th>");
				out.write("<td>");
				
				CertificateRequestHostModel model = new CertificateRequestHostModel(context);
				try {
					ArrayList<ContactRecord> gas = model.findGridAdmin(rec);
					//out.write("<ul>");
					for(ContactRecord ga : gas) {
						out.write("<p>");
						out.write("<b>"+StringEscapeUtils.escapeHtml(ga.name)+"</b>");
						if(auth.isUser()) {
							out.write(" <code><a href=\"mailto:"+ga.primary_email+"\">"+ga.primary_email+"</a></code>");
							out.write(" Phone: "+ga.primary_phone);
						}
						out.write("</p>");
					}
					//out.write("</ul>");
				} catch (CertificateRequestException e) {
					out.write("<span class=\"label label-important\">No GridAdmin</span>");
				}
				out.write("</td></tr>");
				
				out.write("<tr>");
				out.write("<th>Approver VO</th>");
				out.write("<td>");
				if(rec.approver_vo_id == null) {
					out.write("<span class=\"label label-important\">Not Set</span>");
				} else {
					try {
						VOModel vmodel = new VOModel(context);
						VORecord vo = vmodel.get(rec.approver_vo_id);
						out.write(vo.name);
					} catch (SQLException e) {
						log.error("Failed to lookup vo", e);
					}
				}
				out.write("</td></tr>");
				
				out.write("<tr>");
				out.write("<th>GOC Ticket</th>");
				out.write("<td><a target=\"_blank\" href=\""+StaticConfig.conf.getProperty("url.gocticket")+"/"+rec.goc_ticket_id+"\">"+rec.goc_ticket_id+"</a></td>");
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
		
		/*
		if( //rec.status.equals(CertificateRequestStatus.RENEW_REQUESTED) ||
			rec.status.equals(CertificateRequestStatus.REQUESTED)) {
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
			} else if(rec.status.equals(CertificateRequestStatus.REVOCATION_REQUESTED)) {
				v.add(new HtmlView("<p class=\"alert alert-info\">GridAdmin to revoke certificates</p>"));
			} else if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
				v.add(new HtmlView("<p class=\"alert alert-info\">Please wait for a minute for signer to sign.</p>"));
			}
		*/
		
		final String url = "certificatehost?id="+rec.id;
		
		BootTabView tabview = new BootTabView();
		
		//controls
		final CertificateRequestHostModel model = new CertificateRequestHostModel(context);
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
	                	try {
                			//check access again - request status might have changed
	                		if(model.canApprove(rec)) {
	                			model.approve(rec);
	                			context.message(MessageType.SUCCESS, "Successfully approved the request ID: " + rec.id);
								button.redirect(url);
	                		} else {
    	        				button.alert("Request status has changed. Please reload.");
	                		}
						} catch (CertificateRequestException e) {
							log.error("Failed to approve host certificate", e);
							button.alert(e.getMessage());
						}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Approve", pane);
		}
		/*
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
		*/
		
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
	                			context.message(MessageType.SUCCESS, "Successfully requested revocation of request ID: " + rec.id);
								button.redirect(url);
	                		} else {
    	        				button.alert("Reques status has changed. Please reload.");
	                		}
	                	} catch (CertificateRequestException e) {
							log.error("Failed to request revoke host certificate", e);
							button.alert(e.getMessage());
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Revoke", pane);
		}
		
		if(model.canIssue(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificates</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
            		try {
               			//check access again - request status might have changed
                		if(model.canIssue(rec)) {
                			model.startissue(rec);
                			//context.message(MessageType.SUCCESS, "Successfully started issuing certificate for request ID: " + rec.id);
							button.redirect(url);
                		} else {
	        				button.alert("Reques status has changed. Please reload.");
                		}
                	} catch(CertificateRequestException e) {
						log.error("Failed to issue host certificate", e);
						button.alert(e.getMessage());
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Issue", pane);
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
	                			context.message(MessageType.SUCCESS, "Successfully canceled a request with ID: " + rec.id);
								button.redirect(url);
	                		} else {
		        				button.alert("Reques status has changed. Please reload.");
	                		}
	                	} catch (CertificateRequestException e) {
							log.error("Failed to cancel host certificate", e);
							button.alert(e.getMessage());
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-remove icon-white\"></i> Reject All Requests</button>");
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
	                			context.message(MessageType.SUCCESS, "Successfully rejected a request with ID: " + rec.id);
								button.redirect(url);
	                		} else {
		        				button.alert("Reques status has changed. Please reload.");
	                		}
	                	} catch (CertificateRequestException e) {
							log.error("Failed to reject host certificate", e);
							button.alert(e.getMessage());
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-exclamation-sign icon-white\"></i> Revoke All</button>");
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			button.addEventListener(new DivRepEventListener() {
                public void handleEvent(DivRepEvent event) {
                	if(note.validate()) {
                		context.setComment(note.getValue());
	                 	try {
	               			//check access again - request status might have changed
	                		if(model.canRevoke(rec)) {
	                			model.revoke(rec);
	                			context.message(MessageType.SUCCESS, "Successfully revoked a request with ID: " + rec.id);
								button.redirect(url);
	                		} else {
		        				button.alert("Reques status has changed. Please reload.");
	                		}
	                	} catch (CertificateRequestException e) {
							log.error("Failed to revoke host certificate", e);
							button.alert(e.getMessage());
	                	}
                	}
                }
            });
			pane.add(button);
			tabview.addtab("Revoke", pane);
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
		/*
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
							redirect("certificatehost?id="+id.getValue());
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
				CertificateMenuView menu = new CertificateMenuView(context, "certificatehost");
				menu.render(out);
				out.write("</div>"); //span3
				
				out.write("<div class=\"span9\">");
				/*
				IDForm form = new IDForm(context.getPageRoot());
				form.render(out);
				*/
				if(auth.isUser()) {
					renderMyList(out);
				}
				out.write("</div>"); //span9
				
				out.write("</div>"); //row-fluid
			}
			
			public void renderMyList(PrintWriter out) {
				CertificateRequestHostModel model = new CertificateRequestHostModel(context);
				try {
					ArrayList<CertificateRequestHostRecord> recs = model.getIApprove(auth.getContact().id);
					if(recs.size() != 0) {
						out.write("<h2>Host Certificate Requests that I Approve</h2>");
						HostCertificateTable table = new HostCertificateTable(context, recs, false);
						table.render(out);
					}
				} catch (SQLException e1) {
					out.write("<div class=\"alert\">Failed to load host certificate requests that I am gridadmin of</div>");
					log.error(e1);
				}
				
				try {
					ArrayList<CertificateRequestHostRecord> recs = model.getISubmitted(auth.getContact().id);
					if(recs.size() == 0) {
						out.write("<p class=\"muted\">You have not requested any host certificate.</p>");
					} else {
						out.write("<h2>Host Certificate Requests that I Requested</h2>");
						HostCertificateTable table = new HostCertificateTable(context, recs, false);
						table.render(out);
					}
				} catch (SQLException e1) {
					out.write("<div class=\"alert\">Failed to load my user certificate requests</div>");
					log.error(e1);
				}
				
				
				out.write("</div>");//content
			}
		};
	}

}
