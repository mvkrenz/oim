package edu.iu.grid.oim.view.certaction;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepStaticContent;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.HtmlFileView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.Wizard;
import edu.iu.grid.oim.view.divrep.Wizard.WizardPage;
import edu.iu.grid.oim.view.divrep.form.CertificateAUPDE;
import edu.iu.grid.oim.view.divrep.form.validator.MustbeCheckedValidator;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class UserCertRenew implements IView {
	CertificateRequestUserRecord rec;
	
	Wizard wizard;
	WizardPage page_begin;
	WizardPage page_agreement;
	WizardPage page_password;
	WizardPage page_approve;
	AuthorizationCriterias criterias;
	UserContext context;
	Authorization auth;
	
	public UserCertRenew(UserContext context, CertificateRequestUserRecord rec, AuthorizationCriterias criterias) {
		this.context = context;
		this.criterias = criterias;
		this.rec = rec;
		
		auth = context.getAuthorization();
		
		wizard = new Wizard(context.getPageRoot());
		page_begin = createBeginPage();
		page_agreement = createAgreementPage();
		page_password = createPasswordPage();
		page_approve = createApprovePage();
	}
	
	private WizardPage createBeginPage() {
		WizardPage page = wizard.new WizardPage("Begin Renewal") {
			@Override
			public void init() {
				add(new DivRepStaticContent(wizard, "<p>In order to renew this user certificate, you must meet following criterias.</p>"));
				add(new DivRepFormElement(wizard){
					@Override
					public void render(PrintWriter out) {
						criterias.renderHtml(context, out);
					}

					@Override
					protected void onEvent(DivRepEvent e) {
						// TODO Auto-generated method stub
						
					}
				});
			}
			@Override
			protected void onNext() {
				if(!criterias.passAll()) {
					alert("Sorry, you do not meet all the criterias necessary to begin your renewal process.");
				} else {
					wizard.setActive(page_agreement);
				}
			}
		};
		/*
		if(!criterias.passAll()) {
			page.disableNext();
		}
		*/
		return page;
	}
	
	private WizardPage createAgreementPage() {
		
		return wizard.new WizardPage("Agreements") {
			@Override
			public void init() {
				//agreement doc comes from https://twiki.grid.iu.edu/twiki/pub/Operations/DigiCertAgreements/IGTF_Certificate_Subscriber_Agreement_-_Mar_26_2012.doc
				add(new CertificateAUPDE(context.getPageRoot()));
				
				try {
					add(new DivRepFormElement(context.getPageRoot()) {
						CertificateRequestUserModel model = new CertificateRequestUserModel(context);
						ArrayList<VORecord> vos = model.getVOIApprove(auth.getContact().id);
						HtmlFileView fileview = new HtmlFileView(getClass().getResourceAsStream("ra_agreement.html"));
						DivRepCheckBox ra_agree = new DivRepCheckBox(this);
						
						@Override
						public void render(PrintWriter out) {
							out.write("<div id=\""+getNodeID()+"\">");
							//list VOs that this user is RA of
							if(vos.size() > 0) {
								out.write("<div class=\"well\">");
								out.write("<p class=\"muted\">You are currently RA for following VOs: ");
								for(VORecord vo : vos) {
									out.write("<span class=\"label label-info\">"+StringEscapeUtils.escapeHtml(vo.name)+"</span> ");
								}
								out.write("</p>");
								fileview.render(out);
								
								ra_agree.setLabel("I agree");
								ra_agree.addValidator(new MustbeCheckedValidator("You must agree before renewing your certificate."));			
								ra_agree.render(out);
								
								out.write("</div>");//well
							}
							out.write("</div>");
						}
						@Override
						protected void onEvent(DivRepEvent e) {
							// TODO Auto-generated method stub
							
						}	
					});
					
					add(new DivRepFormElement(context.getPageRoot()) {
						GridAdminModel gamodel = new GridAdminModel(context);
						ArrayList<GridAdminRecord> gas = gamodel.getGridAdminsByContactID(auth.getContact().id);
						HtmlFileView fileview = new HtmlFileView(getClass().getResourceAsStream("ga_agreement.html"));
						DivRepCheckBox ga_agree = new DivRepCheckBox(this);
						
						@Override
						public void render(PrintWriter out) {
							out.write("<div id=\""+getNodeID()+"\">");
							if(gas.size() > 0) {
								out.write("<div class=\"well\">");
								out.write("<p class=\"muted\">You are currently GridAdmin for following Domains: ");
								for(GridAdminRecord ga : gas) {
									out.write("<span class=\"label label-info\">"+StringEscapeUtils.escapeHtml(ga.domain)+"</span>&nbsp;");
								}
								out.write("</p>");
								fileview.render(out);
								
								ga_agree.setLabel("I agree");
								ga_agree.addValidator(new MustbeCheckedValidator("You must agree before renewing your certificate."));		
								ga_agree.render(out);
								
								out.write("</div>");
							}	
							out.write("</div>");
						}

						@Override
						protected void onEvent(DivRepEvent e) {
							// TODO Auto-generated method stub
							
						}						
					});	
						
					
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
			@Override
			protected void onNext() {
				wizard.setActive(page_password);
			}
		};
	}
	
	private WizardPage createPasswordPage() {
		
		
		
		
		
		return wizard.new WizardPage("Choose Password") {
			
			DivRepPassword pass;
			DivRepPassword pass_confirm;
			
			@Override
			public void init() {

				pass = new DivRepPassword(context.getPageRoot());
				pass.setLabel("Password");
				pass.setRequired(true);
				pass.addValidator(new PKIPassStrengthValidator());
				pass.addEventListener(new DivRepEventListener() {
					@Override
					public void handleEvent(DivRepEvent event) {
						if(pass_confirm.getValue() != null) {
							pass_confirm.validate();
						}
					}
				});
				pass_confirm = new DivRepPassword(context.getPageRoot());
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
				
				if(context.isSecure()) {
					pass.setRepopulate(true);
					pass_confirm.setRepopulate(true);
				}
				
				add(new DivRepFormElement(context.getPageRoot()) {
					@Override
					public void render(PrintWriter out) {
						out.write("<div id=\""+getNodeID()+"\">");
						out.write("<p>Please choose a password to encrypt your renewed certificate &amp; private key.</p>");
						pass.render(out);
						pass_confirm.render(out);
						out.write("</div>");
					}

					@Override
					protected void onEvent(DivRepEvent e) {
						// TODO Auto-generated method stub
					}
				});
			}
			@Override
			protected void onNext() {
        		context.setComment("User requesting / issueing renewed user certificate");
				CertificateRequestUserModel model = new CertificateRequestUserModel(context);
        		try {
        			//check access again - request status might have changed while processing the form
        			//or.. a malicious user could open 10 different tabs and start renewal processes simultanously..
        			criterias.retestAll();
        			if(criterias.passAll()) {
        				model.renew(rec, pass.getValue());
            			context.message(MessageType.SUCCESS, "Successfully renewed certificate request with ID: " + rec.id);
						wizard.setActive(page_approve);
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
		};
	}
	
	private WizardPage createApprovePage() {
		return wizard.new WizardPage("Approval") {
			@Override
			public void init() {
				add(new DivRepStaticContent(wizard, "<p>Approval</p>"));
			}
			@Override
			protected void onNext() {
			}
		};
	}
	
	@Override
	public void render(PrintWriter out) {
		wizard.render(out);
	}
}
