package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletException;

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
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.cert.CertificateManager;
import edu.iu.grid.oim.model.db.CertificateRequestModelBase;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.DNRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.servlet.CertificateUserServlet;
import edu.iu.grid.oim.view.BootTabView;
import edu.iu.grid.oim.view.CertificateMenuView;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.UserCertificateTable;
import edu.iu.grid.oim.view.divrep.ChoosePassword;
import edu.iu.grid.oim.view.divrep.UserCNEditor;
import edu.iu.grid.oim.view.divrep.form.validator.MustbeCheckedValidator;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class UserCertificateActionView {
    static Logger log = Logger.getLogger(CertificateUserServlet.class);  
    
	UserContext context; 
	CertificateRequestUserRecord rec; 
	UserCNEditor cn_override;
	ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs;
	
	public UserCertificateActionView(
		UserContext context, 
		CertificateRequestUserRecord rec, 
		//UserCNEditor cn_override, 
		ArrayList<CertificateRequestModelBase<CertificateRequestUserRecord>.LogDetail> logs) 
	{
		this.context = context;
		this.rec = rec;
		//this.cn_override = cn_override;
		this.logs = logs;
	}
	public void render(PrintWriter out) {
		
		final CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		final String url = "certificateuser?id="+rec.id;
	
		BootTabView tabview = new BootTabView();
		tabview.setLeftTabl(true);
			
		if(model.canApprove(rec)) {
			GenericView pane = new GenericView();
			
			final DivRepTextArea note = new DivRepTextArea(context.getPageRoot());
			note.setHeight(40);
			note.setLabel("Note");
			note.setSampleValue("Details for this action.");
			note.setRequired(true);
			pane.add(note);
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-ok icon-white\"></i> Approve</button>") {
				protected void onClick(DivRepEvent e) {
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
			                			alert("The same DN is already registered in OIM for user id:"+duplicate.contact_id + ". Please specify different CN");
			                			return;
			                		}
								} catch (SQLException e1) {
									log.error("Failed to test duplicate DN during approval process", e1);
									alert("Failed to test duplicate DN.");
									return;
								}	                		
	            			} else {
	            				alert("Failed to validate provided CN.");
	            				return;
	            			}
	            		}
	            			                	
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canApprove(rec)) {
	            				model.approve(rec);
	                			context.message(MessageType.SUCCESS, "Successfully approved a request with ID: " + rec.id);
								redirect(url);
	            			} else {
	            				alert("Reques status has changed. Please reload.");
	            			}
	            		} catch (CertificateRequestException ex) {
	            			String message = "Failed to approve request: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}					
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
			Authorization auth = context.getAuthorization();
			try {
				//list VOs that this user is RA of
				ArrayList<VORecord> vos = model.getVOIApprove(auth.getContact().id);
				if(vos.size() > 0) {
					pane.add(new HtmlView("<div class=\"well\">"));
					pane.add(new HtmlView("<p class=\"muted\">You are currently RA for following VOs: "));
					for(VORecord vo : vos) {
						pane.add(new HtmlView("<span class=\"label label-info\">"+StringEscapeUtils.escapeHtml(vo.name)+"</span> "));
					}
					pane.add(new HtmlView("</p>"));
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
					pane.add(new HtmlView("<p class=\"muted\">You are currently GridAdmin for following Domains: "));
					for(GridAdminRecord ga : gas) {
						pane.add(new HtmlView("<span class=\"label label-info\">"+StringEscapeUtils.escapeHtml(ga.domain)+"</span> "));
					}
					pane.add(new HtmlView("</p>"));
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-success\"><i class=\"icon-refresh icon-white\"></i> Renew</button>") {
				protected void onClick(DivRepEvent e) {
	            	if(pass.validate() && pass_confirm.validate() && ra_agree.validate() && ga_agree.validate()) {
	            		context.setComment("User requesting / issueing renewed user certificate");
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canRenew(rec, logs)) {
	            				model.renew(rec, pass.getValue());
	                			context.message(MessageType.SUCCESS, "Successfully renewed certificate request with ID: " + rec.id);
								redirect(url);
	            			} else {
	            				alert("Reques status has changed. Please reload.");
	            			}
	            		} catch (CertificateRequestException ex) {
	            			String message = "Failed to renew certificate: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}					
				}	
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-exclamation-sign icon-white\"></i> Request Revocation</button>") {
				protected void onClick(DivRepEvent e) {
					if(note.validate()) {
	            		context.setComment(note.getValue());
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canRequestRevoke(rec)) {
	            				model.requestRevoke(rec);
	                			context.message(MessageType.SUCCESS, "Successfully requested certificate revocation for a request with ID: " + rec.id);
								redirect(url);
	            			} else {
	            				alert("Reques status has changed. Please reload.");
	            			}
	            		} catch (CertificateRequestException ex) {
	            			String message = "Failed to request revocation: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}		
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
						
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Issue Certificate ...</button>") {
				protected void onClick(DivRepEvent e) {
	            	if(pass.validate() && pass_confirm.validate()) {
	            		context.setComment("User requested to issue certificate");
	            		//start process thread 
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canIssue(rec)) {
	            				model.startissue(rec, pass.getValue());
	                			//context.message(MessageType.SUCCESS, "Successfully started issing a certificate for a request with ID: " + rec.id);
								redirect(url);
	            			} else {
	            				alert("Reques status has changed. Please reload.");
	            			}
	                	} catch(CertificateRequestException ex) {
	                		log.warn("CertificateRequestException while issuging certificate -- request ID:"+rec.id, ex);
	            			String message = "Failed to issue certificate: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
						
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-download-alt icon-white\"></i> Cancel Request</button>") {
				protected void onClick(DivRepEvent e) {
	            	if(pass.validate()/* && note.validate()*/) {
	            		//context.setComment(note.getValue());
	            		context.setComment("Submitter canceled request.");
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canCancelWithPass(rec)) {
	            				model.cancelWithPass(rec, pass.getValue());
	                			context.message(MessageType.SUCCESS, "Successfully canceled a certificate request with ID: " + rec.id);
								redirect(url);
	            			} else {
	            				alert("Reques status has changed. Please reload.");
	            			}
	                	} catch(CertificateRequestException ex) {
	                		log.warn("CertificateRequestException while canceling certificate request:", ex);
	            			String message = "Failed to cancel request: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}				
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn\">Cancel Request</button>") {
				protected void onClick(DivRepEvent e) {
	            	if(note.validate()) {
	            		context.setComment(note.getValue());
	                	try {
	            			//check access again - request status might have changed
		        			if(model.canCancel(rec)) {
		                    	model.cancel(rec);
	                			context.message(MessageType.SUCCESS, "Successfully canceled a certificate request with ID: " + rec.id);
								redirect(url);
		        			} else {
		        				alert("Reques status has changed. Please reload.");
		        			}
	                	} catch(CertificateRequestException ex) {
	                		log.warn("CertificateRequestException while canceling certificate request:", ex);
	            			String message = "Failed to cancel request: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}					
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
		
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-remove icon-white\"></i> Reject Request</button>") {
				protected void onClick(DivRepEvent e) {
	            	if(note.validate()) {
	            		context.setComment(note.getValue());
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canReject(rec)) {
	            				model.reject(rec);
	                			context.message(MessageType.SUCCESS, "Successfully rejected a certificate request with ID: " + rec.id);
								redirect(url);
	            			} else {
		        				alert("Reques status has changed. Please reload.");
	            			}
	            		} catch (CertificateRequestException ex) {
	                		log.warn("CertificateRequestException while rejecting certificate request:", ex);
	            			String message = "Failed to cancel request: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	            		}
	            	}					
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-danger\"><i class=\"icon-exclamation-sign icon-white\"></i> Revoke</button>") {
				protected void onClick(DivRepEvent e) {
	            	if(note.validate()) {
	            		context.setComment(note.getValue());
	            		try {
	            			//check access again - request status might have changed
	            			if(model.canRevoke(rec)) {
	            				model.revoke(rec);
	                			context.message(MessageType.SUCCESS, "Successfully revoked a certificate request with ID: " + rec.id);
								redirect(url);
	            			} else {
		        				alert("Reques status has changed. Please reload.");
	            			}
	            		} catch (CertificateRequestException ex) {
	            			String message = "Failed to revoke: " + ex.getMessage();
	            			if(ex.getCause() != null) {
	            				message += "\n\n" + ex.getCause().getMessage();
	            			}
	                		alert(message);
	                	}
	            	}					
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
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
			
			final DivRepButton button = new DivRepButton(context.getPageRoot(), "<button class=\"btn btn-primary\"><i class=\"icon-refresh icon-white\"></i> Re-request</button>") {
				protected void onClick(DivRepEvent e) {
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
								redirect(url);
		        			} else {
		        				alert("Reques status has changed. Please reload.");
		        			}
	            		} catch (CertificateRequestException ex) {
	                		alert("Failed to re-request: " + ex.getMessage());
	                	}
	            	}					
				}
			};
			button.setStyle(DivRepButton.Style.HTML);
			button.addClass("inline");
			pane.add(button);
			tabview.addtab("Re-Request", pane);
		}
		
		if(tabview.size() == 0) {
			if(rec.status.equals(CertificateRequestStatus.ISSUING)) {
				out.write("<p class=\"alert alert-warning\">Certificate is being issued. Please wait for a few minutes..</p>");				
			} else {
				out.write("<p class=\"alert alert-warning\">You can not perform any action on this certificate. Please contact GOC for assistance.</p>");
			}
		}
		
		tabview.render(out);
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

