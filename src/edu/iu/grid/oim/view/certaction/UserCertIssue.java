package edu.iu.grid.oim.view.certaction;

import java.io.PrintWriter;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepStaticContent;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.AuthorizationCriterias;
import edu.iu.grid.oim.model.CertificateRequestStatus;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.divrep.AuthorizationCriteriasView;
import edu.iu.grid.oim.view.divrep.EditableContent;
import edu.iu.grid.oim.view.divrep.Wizard;
import edu.iu.grid.oim.view.divrep.Wizard.WizardPage;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class UserCertIssue implements IView {
    static Logger log = Logger.getLogger(UserCertIssue.class);
	
	Wizard wizard;
	WizardPage page_password;
	WizardPage page_issue;
	WizardPage page_download;
	
	CertificateRequestUserRecord rec;
	AuthorizationCriterias criterias;
	UserContext context;
	Authorization auth;
	
	public UserCertIssue(UserContext context, CertificateRequestUserRecord rec, AuthorizationCriterias criterias) {
		this.context = context;
		this.criterias = criterias;
		this.rec = rec;
		
		auth = context.getAuthorization();
		
		wizard = new Wizard(context.getPageRoot());
		
		page_password = createPasswordPage();
		wizard.addPage(page_password);
		
		page_issue = createIssuePage();
		wizard.addPage(page_issue);
		
		page_download = createDownloadPage();
		wizard.addPage(page_download);
	}

	private WizardPage createPasswordPage() {		
		return wizard.new WizardPage("Choose Password") {
			
			DivRepPassword pass;
			DivRepPassword pass_confirm;
			
			@Override
			public void init() {
				
				//password
				if(rec.requester_passphrase == null) {
					new DivRepStaticContent(this, "<p>Please choose a password to encrypt your new certificate &amp; private key.</p>");
				} else {
					new DivRepStaticContent(this, "<p>Please enter the password you chose during a request submission to retrieve your certificate &amp; encrypt your private key. If you don't remember, please read <a target=\"_blank\" href=\"https://confluence.grid.iu.edu/display/CENTRAL/Forgot+retrieval+password\">this doc.</a></p>");
				}
				
				pass = new DivRepPassword(this);
				pass.setLabel("Password");
				pass.setRequired(true);

				pass_confirm = new DivRepPassword(this);
				pass_confirm.setLabel("Re-enter password");
				//pass_confirm.setRequired(true);
				pass_confirm.setHidden(true);
				
				if(context.isSecure()) {
					pass.setRepopulate(true);
					pass_confirm.setRepopulate(true);
				}

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
					//pane.add(pass_confirm);
				} else {
					//guest - password check
					
					pass.addValidator(new DivRepIValidator<String>(){
						@Override
						public Boolean isValid(String value) {
							CertificateRequestUserModel model = new CertificateRequestUserModel(context);
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
							
			}
			@Override
			protected void onNext() {
        		context.setComment("User issueing a new user certificate");
				
        		try {
        			//check access again - request status might have changed while processing the form
        			//or.. a malicious user could open 10 different tabs and start renewal processes simultanously..
        			criterias.retestAll();
        			if(criterias.passAll()) {
        				//not so OO-ish.
         				doProcess(context, pass.getValue());
						wizard.setActive(page_issue);
        			} else {
        				alert("Request status has changed. Please reload.");
        			}
        		} catch (CertificateRequestException ex) {
        			String message = "Failed to issue/renew certificate: " + ex.getMessage();
        			if(ex.getCause() != null) {
        				message += "\n\n" + ex.getCause().getMessage();
        			}
            		alert(message);
            	}
			}
		};
	}

	protected void doProcess(UserContext context, String pass) throws CertificateRequestException {
		CertificateRequestUserModel model = new CertificateRequestUserModel(context);
		//model.renew(rec, pass);
		model.startissue(rec, pass);
	}
		

	private WizardPage createIssuePage() {
		return wizard.new WizardPage("Issue Certificate") {
			@Override
			public void init() {
				
				new DivRepFormElement(this) {
					CertificateRequestUserModel model = new CertificateRequestUserModel(context);
					CertificateRequestUserRecord current_rec = rec;
					@Override
					public void render(PrintWriter out) {
						out.write("<div id=\""+getNodeID()+"\">");
						
						//pull the latest request status
						try {
							current_rec = model.get(rec.id);
							//log.debug(current_rec.status);
						} catch (SQLException e1) {
							log.error("Failed to load certificate record", e1);
						}
						
						if(current_rec.status.equals(CertificateRequestStatus.ISSUING)) {
							out.write("<h3><img src=\"images/loading.gif\"> Issuing Certificate</h3>");
							out.write("<p>We are contacting our certificate authority to issue your certificate.</p>");
							out.write("<p>Please <strong>do not</strong> close this window until your certificate is issued.</p>");
							//reload status in 3 sec
							out.write("<script>setTimeout(\"divrep('"+getNodeID()+"', null, null, 'reload');\", 3000);</script>");
						} else if(current_rec.status.equals(CertificateRequestStatus.ISSUED)) {
							//somehow, I need to have divrep wrapped in timeout, or it won't work..
							out.write("<script>setTimeout(\"divrep('"+getNodeID()+"', null, null, 'download');\", 0);</script>");
						} else {
							out.write("<p class=\"alert alert-danger\">Something went wrong while issuing your certificate.\n");
							out.write("OSG support staff will investigate and contact you shortly. If you have any questions, please feel free to contact us at <a href=\"mailto:goc@opensciencegrid.org\">goc@opensciencegrid.org</a></p>");

						}
							
						out.write("</div>");
					}

					@Override
					protected void onEvent(DivRepEvent e) {
						log.debug(e.action);
						switch(e.action) {
						case "download": wizard.setActive(page_download); break;
						}
						redraw();
					}
				};
				//setSubmitLabel("Issuing..");
				//disableNext();
				hideNext();
			}
			@Override
			protected void onNext() {
			}
		};
	}
	
	private WizardPage createDownloadPage() {
		final String url = "certificatedownload?id="+rec.id+"&type=user&download=pkcs12";
		return wizard.new WizardPage("Download Certificate") {

			@Override
			public void init() {
				new DivRepStaticContent(this, "<h3>Your certificate has been issued</h3>");
				new DivRepStaticContent(this, "<p>Your browser should start downloading your certificate. If not, please click the download button below to start downloading your certificate.</p>");
				new DivRepStaticContent(this, "<iframe width=\"1\" height=\"1\" frameborder=\"0\" src=\""+url+"\"></iframe>");
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
				
				ConfigModel config = new ConfigModel(context);
				Config help_content = config.new Config(config, "howto_import_pkcs12", "");
				if(auth.allows("admin") || auth.allows("admin_ra")) {
					new EditableContent(context.getPageRoot(), context, help_content);
				} else {
					new DivRepStaticContent(context.getPageRoot(), help_content.getString());
				}
				
				setSubmitLabel("Download Certificate (PKCS12)");
			}
			@Override
			protected void onNext() {
				js("var a = document.createElement('a');a.href='"+url+"';a.target='_blank';document.body.appendChild(a);a.click();");
			}
		};
	}
	
	@Override
	public void render(PrintWriter out) {
		if(criterias.passAll()) {
			wizard.render(out);
		} else {
			out.write("<p>In order to issue this user certificate, the following criteria must be met.</p>");
			AuthorizationCriteriasView authview = new AuthorizationCriteriasView(context.getPageRoot(), context, criterias);
			authview.render(out);
		}
	}
}
