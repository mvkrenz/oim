package edu.iu.grid.oim.view.divrep.form;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.util.encoders.Base64;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.common.DivRepToggler;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.MessageType;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.GridAdminModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.GridAdminRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

import edu.iu.grid.oim.view.divrep.DivRepSimpleCaptcha;

public class CertificateRequestHostForm extends DivRepForm
{
    static Logger log = Logger.getLogger(CertificateRequestHostForm.class);
	private UserContext context;
	private Authorization auth;
	
	//requester contact (for guest)
	private DivRepTextBox fullname;
	private DivRepTextBox email;
	private DivRepTextBox phone;
	
	private DivRepTextArea request_comment;
	
	private DivRepTextArea csr;
	private DivRepSelectBox vo;
	
	private DivRepCheckBox agreement;

	
	public CertificateRequestHostForm(final UserContext context, String origin_url) {
		
		super(context.getPageRoot(), origin_url);
		this.context = context;
		auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
	
		new DivRepStaticContent(this, "<h2>Host Certificate Request</h2>");
		
		if(!auth.isUser()) {
			new DivRepStaticContent(this, "<div class=\"alert\">This is a public host certificate request form. If you are already an OIM user, please login first.</div>");
			new DivRepStaticContent(this, "<h3>Contact Information</h3>");
			new DivRepStaticContent(this, "<p class=\"help-block\">Following information will be used to contact you during the approval process.</p>");
					
			fullname = new DivRepTextBox(this);
			fullname.setLabel("Full Name");
			fullname.setRequired(true);
			if(contact != null) {
				fullname.setValue(contact.name);
			}
			
			email = new DivRepTextBox(this);
			email.setLabel("Email");
			email.setRequired(true);
			if(contact != null) {
				email.setValue(contact.primary_email);
			}
			
			phone = new DivRepTextBox(this);
			phone.setLabel("Phone");
			phone.setRequired(true);
			if(contact != null) {
				phone.setValue(contact.primary_phone);
			}
			
			new DivRepStaticContent(this, "<h3>Captcha</h3>");
			new DivRepSimpleCaptcha(this, context.getSession());
		}
	
		new DivRepStaticContent(this, "<h3>CSR (Certificate Signing Request)</h3>");

		DivRepToggler csr_help = new DivRepToggler(this) {
			@Override
			public DivRep createContent() {
				return new DivRepStaticContent(this, 
						"<div class=\"well\"><p>You can create your CSR on your target hosts using tools such as openssl and copy & paste generated CSR below. </p>"+
						"<p><code>umask 077; openssl req -new -newkey rsa:2048 -nodes -keyout hostkey.pem -subj \"/CN=osg-ce.example.edu\"</code></p>"+
						"<p>If you want to request a service certificate, you need to escape backslash for service name inside CN like following.</p>" +
						"<p><code>umask 077; openssl req -new -newkey rsa:2048 -nodes -keyout hostkey.pem -subj \"/CN=rsv\\/osg-ce.example.edu\"</code></p>"+
						"<p>DN will be overriden by the certificate signer except CN.</p>" +
						"</div>");
			}};
		csr_help.setShowHtml("<u class=\"pull-right\">How can I generate CSR?</u>");
		csr_help.setHideHtml("");
		
	
		csr = new DivRepTextArea(this);
		//csr.setLabel("CSR");
		csr.setRequired(true);
		csr.setSampleValue("-----BEGIN CERTIFICATE REQUEST-----\n"+
"MIIC5DCCAcwCAQAwgZ4xCzAJBgNVBAYTAlVTMRAwDgYDVQQIEwdJbmRpYW5hMRQw\n"+
"EgYDVQQHEwtCbG9vbWluZ3RvbjEbMBkGA1UEChMSSW5kaWFuYSBVbml2ZXJzaXR5\n"+
"MQ0wCwYDVQQLEwRVSVRTMRswGQYDVQQDExJzb2ljaGkuZ3JpZC5pdS5lZHUxHjAc\n"+
"BgkqhkiG9w0BCQEWD2hheWFzaGlzQGl1LmVkdTCCASIwDQYJKoZIhvcNAQEBBQAD\n"+
"ggEPADCCAQoCggEBAM6TXTvVBUl2Rw1cLaJuF0zqOXxHmtizF/BRE16RxPS88AyA\n"+
"YgnMg5Aa+emqJXaMfeh2zXifoi0yPKsRJwztLrxSU8IXlzcUZ0mBEK+gzfK7GtFV\n"+
"5sRL4ecdYR1R9XVlj2iL0FpLknBJHQb9I7+WQ6rC9yhwKoH7Sm5EaNWo2ty4YVca\n"+
"rNw7pptizRVAUW972+jvcCNJWZyNJJtyKJOR0zkulYyXPohW5ovcT0hyCs9XTYNN\n"+
"g/O02fI1sEzEyOfoBNoHy06UH0L0xw9AkxwUmlzyZr+NB2OuhCEjm/QUefMgh+c8\n"+
"PFxbcW69M0lGR4A20ZJsd+2hui1Cz1wWfSIqqBMCAwEAAaAAMA0GCSqGSIb3DQEB\n"+
"BQUAA4IBAQC26RTFELEb0NhkmIarz/t1IciqbuYN1WIwfRpn5yIR7WiSquA6BwR1\n"+
"Ie6mpOlJNzj5PflQU1FJNeMxWUc7vFsUsupv9y1Pv0MpMXX9iSYPYjym3IA0I2D/\n"+
"CIdVVwpOpjTJJhCI/r5LGiZIKWxv4prjMc47ctWm8rPu1TmH3fEX8ZeQ2ZNU/VMJ\n"+
"gymaT3CFIWanYbJnkWCFigzBkrh7aaE1zDWrDKV3EQs3N+i5NzFhM6pg6Ix/5lLs\n"+
"8skR/aR4v2OMI/8JawkWkgn9WqCY6dIm+1af9zikTlPRehbt4VzYsLJijOCPXkUV\n"+
"Nb4jr2oKlBc4Vqo4OjfpakA4n6yseH0F\n"+
"-----END CERTIFICATE REQUEST-----");
		csr.setWidth(600);
		csr.addValidator(new DivRepIValidator<String>(){
			String error_message;
			@Override
			public String getErrorMessage() { return error_message;}

			@Override
			public Boolean isValid(String dirty_csr) {
				vo.setHidden(true);
				vo.setRequired(false);
				vo.redraw();
				
				//parse CSR and check to make sure it's valid
				try {
					
					CertificateRequestHostModel certmodel = new CertificateRequestHostModel(context);
					String csr_string = stripCSRString(dirty_csr);
					PKCS10CertificationRequest pkcs10 = certmodel.parseCSR(csr_string);
					String cn = certmodel.pullCNFromCSR(pkcs10);
					GridAdminModel gamodel = new GridAdminModel(context);
					String domain = gamodel.getDomainByFQDN(cn);
					if(domain == null) {
						error_message = "Failed to find any GridAdmin who can approve certificates for CN: " + cn;
						return false;
					}
					HashMap<VORecord, ArrayList<GridAdminRecord>> groups = gamodel.getByDomainGroupedByVO(domain);					
					HashMap<Integer, String> keyvalues = new HashMap<Integer, String>();
					for(VORecord vo : groups.keySet()) {
						keyvalues.put(vo.id, vo.name);
					}
					vo.setValues(keyvalues);
					vo.setHidden(false);
					if(keyvalues.size() > 1) {
						//required if there are more than one value
						//only 1 - then user can choose to select null
						vo.setRequired(true);
					}
					vo.setLabel("Approver VO for "+domain);
					
					return true;
				} catch (IOException e) {
					error_message = e.getMessage();
				} catch (CertificateRequestException e) {
					error_message = "Failed to pull CN from pkcs10." + e.getMessage();
				} catch (SQLException e) {
					error_message = "Failed to lookup GridAdmin domain" + e.getMessage();
				}
				return false;
			}
		});		
		//hidden until user enter CSR
		vo = new DivRepSelectBox(this);
		vo.setHidden(true);
		vo.setRequired(false);
		vo.addClass("indent");
		
		request_comment = new DivRepTextArea(this);
		request_comment.setLabel("Comments");
		request_comment.setSampleValue("Please enter any comments, or request you'd like to make for GridAdmin");
		request_comment.setWidth(600);
		
		new DivRepStaticContent(this, "<h2>OSG Policy Agreement</h2>");
		//agreement doc comes from https://twiki.grid.iu.edu/twiki/pub/Operations/DigiCertAgreements/IGTF_Certificate_Subscriber_Agreement_-_Mar_26_2012.doc
		InputStream aup_stream =getClass().getResourceAsStream("osg.certificate.agreement.html");
		StringBuilder aup = ResourceReader.loadContent(aup_stream);
		new DivRepStaticContent(this, aup.toString());
		agreement = new DivRepCheckBox(this);
		agreement.setLabel("I AGREE");
		agreement.setRequired(true);
		agreement.addValidator(new DivRepIValidator<Boolean>(){

			@Override
			public Boolean isValid(Boolean value) {
				return value;
			}

			@Override
			public String getErrorMessage() {
				return "You must agree to these policies";
			}
		});
	
	}

	private String stripCSRString(String csr_string) {
		//remove comment lines (---)
		String clean_csr = "";
		for(String line : csr_string.split("\n")) {
			if(line.startsWith("----")) continue;
			clean_csr += line;
		}
		return clean_csr;
	}
	
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected Boolean doSubmit() {

		String requester_name;
		String requester_email;
		String requester_phone;
		if(auth.isUser()) {
			ContactRecord user = auth.getContact();
			requester_name = user.name;
			requester_email = user.primary_email;
			requester_phone = user.primary_phone;
		} else {
			requester_name = fullname.getValue();
			requester_email = email.getValue();
			requester_phone = phone.getValue();
		}
	
		/* - we now have validator in form
		//use parseCSR to clean up.
		CertificateRequestHostModel certmodel = new CertificateRequestHostModel(context);
		try {
			PKCS10CertificationRequest pkcs10 = certmodel.parseCSR(csr.getValue());
			String cn = certmodel.pullCNFromCSR(pkcs10);
		} catch (IOException e1) {
			log.error("Faile to parse CSR at submit. this shouldn't happen");
			return false;
		}
		*/
		
		//create array of 1 - since we only allow 1 host cert per request on this ui
		String []csrs = new String[1];
		csrs[0] = stripCSRString(csr.getValue());
		
		//TODO - allow user to pass list of email addresses
		String []request_ccs = null;
		
		//TODO - in the near future, we need to pass dn override (or is this just for user cert?)
		
		//do certificate request
		CertificateRequestHostModel certmodel = new CertificateRequestHostModel(context);
		try {
			CertificateRequestHostRecord rec;
			if(auth.isUser()) {
				rec = certmodel.requestAsUser(csrs, auth.getContact(), request_comment.getValue(), request_ccs, vo.getValue());
			} else {
				rec = certmodel.requestAsGuest(csrs, requester_name, requester_email, requester_phone, request_comment.getValue(), request_ccs, vo.getValue());
			}
			if(rec != null) {
				redirect("certificatehost?id="+rec.id); //TODO - does this work? I haven't tested it
			}
			context.message(MessageType.SUCCESS, "Successfully requested new host certificate. Your GridAdmin will contact you to process your request.");
			return true;
		} catch (CertificateRequestException e) {
			log.error("Failed to submit request..", e);
			alert(e.getMessage());
			return false;	
		} catch (Exception e) {
			log.error("Failed to submit request (unhandled exception)", e);
			alert(e.toString());
			return false;
		}
	}
}