package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import org.apache.log4j.Logger;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import net.tanesha.recaptcha.ReCaptchaResponse;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepFormElement;

import edu.iu.grid.oim.view.divrep.form.UserCertificateRequestForm;

public class DivRepReCaptcha extends DivRepFormElement {
    static Logger log = Logger.getLogger(DivRepReCaptcha.class);
    
	ReCaptcha captcha;
	String challenge;
	String answer;
	String remote_addr;
	String public_key;
	
	public DivRepReCaptcha(DivRep parent, String public_key, String private_key, String remote_addr) {
		super(parent);
		captcha = ReCaptchaFactory.newReCaptcha(public_key, private_key, false);
		this.remote_addr = remote_addr;
		this.public_key = public_key;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void render(PrintWriter out) {
	
		out.write("<div id=\""+getNodeID()+"\">");
		String html = captcha.createRecaptchaHtml(null, null);
		log.debug(html);
		out.write(html);
		//capture change event
		out.write("<script>");
		out.write("$(function() { ");
		out.write("console.log($('#recaptcha_response_field'));");
		out.write("$('#"+getNodeID()+" input[type=text]').change(");
		out.write("		function(event) {divrep('"+getNodeID()+"', event, $(this).val()+':'+ $('#"+getNodeID()+" input[type=hidden]').val(), 'change'); }");
		out.write(");");
		out.write("});</script>");
		error.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		String[] tokens = e.value.split(":");
		answer = tokens[0];
		challenge = tokens[1];
	}
	
	public boolean validate()
	{
		//validate WLCG interop selections
		error.redraw();
		boolean valid = true;
		
		/*
        ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
        reCaptcha.setPrivateKey(private_key);
        */
		if(challenge == null || answer == null) {
			valid = false;
        	error.set("Please answer the captcha.");
		} else {
	        ReCaptchaResponse reCaptchaResponse = captcha.checkAnswer(remote_addr, challenge, answer);
	        if (!reCaptchaResponse.isValid()) {
	        	error.set("Your recaptcha doesn't match.");
	        	valid = false;
	        }
		}
		setValid(valid);
		return valid;
	}


}
