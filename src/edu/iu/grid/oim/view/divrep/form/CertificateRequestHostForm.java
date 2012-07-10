package edu.iu.grid.oim.view.divrep.form;

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepPassword;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.HashHelper;
import edu.iu.grid.oim.lib.ResourceReader;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.CertificateRequestHostModel;
import edu.iu.grid.oim.model.db.CertificateRequestUserModel;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.DNModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.CertificateRequestHostRecord;
import edu.iu.grid.oim.model.db.record.CertificateRequestUserRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.model.exceptions.CertificateRequestException;

import edu.iu.grid.oim.view.divrep.DivRepSimpleCaptcha;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class CertificateRequestHostForm extends DivRepForm
{
    static Logger log = Logger.getLogger(CertificateRequestHostForm.class);
	private UserContext context;
	private Authorization auth;
	
	//requester contact (for guest)
	private DivRepTextBox fullname;
	private DivRepTextBox email;
	private DivRepTextBox phone;
	
	private DivRepTextArea csr;
	//private DivRepTextBox fqdn;
	private DivRepCheckBox agreement;
	
	public CertificateRequestHostForm(final UserContext context, String origin_url) {
		
		super(context.getPageRoot(), origin_url);
		this.context = context;
		auth = context.getAuthorization();
		ContactRecord contact = auth.getContact();
	

		if(!auth.isUser()) {
			new DivRepStaticContent(this, "<div class=\"alert\">This is a public certificate request form. If you are already an OIM user, please login first.</div>");
			new DivRepStaticContent(this, "<h2>Contact Information</h2>");
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
	
			new DivRepStaticContent(this, "<h2>Captcha</h2>");
			new DivRepSimpleCaptcha(this, context.getSession());
		}
		
		new DivRepStaticContent(this, "<h2>Host Certificate Request</h2>");
		/*	
		fqdn = new DivRepTextBox(this);
		fqdn.setLabel("FQDN");
		fqdn.setSampleValue("soichi.grid.iu.edu");
		fqdn.setRequired(true);
		*/
		csr = new DivRepTextArea(this);
		csr.setLabel("CSR");
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
		new DivRepStaticContent(this, "<p>* Create your CSR on your target hosts using tools such as openssl and copy & paste generated CSR above. <br><pre>umask 077; openssl req -new -newkey rsa:2048 -nodes -keyout hostkey.pem -subj \"/CN=osg-ce.example.edu\"</pre> DN will be overriden by the certificate signer except CN.</p>");
		
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

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Boolean doSubmit() {
		Boolean ret = true;

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
		
		//remove comment lines (---)
		String clean_csr = "";
		for(String line : csr.getValue().split("\n")) {
			if(line.startsWith("----")) continue;
			clean_csr += line;
		}
		String []csrs = new String[1];
		csrs[0] = clean_csr;
		
		//TODO - in the near future, we need to pass dn override (or is this just for user cert?)
		
		//do certificate request
		try {
			CertificateRequestHostModel certmodel = new CertificateRequestHostModel(context);
			CertificateRequestHostRecord rec;
			if(auth.isUser()) {
				rec = certmodel.requestAsUser(csrs, auth.getContact());
			} else {
				rec = certmodel.requestAsGuest(csrs, requester_name, requester_email, requester_phone);
			}
			if(rec != null) {
				redirect("certificatehost?id="+rec.id); //TODO - does this work? I haven't tested it
			}
		} catch (CertificateRequestException e) {
			log.error("Failed to submit request..", e);
			alert(e.getMessage());
			ret = false;	
		} catch (Exception e) {
			log.error("Failed to submit request (unhandled exception)", e);
			alert(e.toString());
			ret = false;
		}
	
		//context.storeDivRepSession();
		return ret;
	}
}