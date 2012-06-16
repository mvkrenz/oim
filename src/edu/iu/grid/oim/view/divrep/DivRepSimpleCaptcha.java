package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import nl.captcha.Captcha;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;

public class DivRepSimpleCaptcha extends DivRepFormElement {
    static Logger log = Logger.getLogger(DivRepSimpleCaptcha.class);
    
    private DivRepTextBox answer;
    private HttpSession session;
    
	public DivRepSimpleCaptcha(DivRep parent, HttpSession session) {
		super(parent);
		answer = new DivRepTextBox(this);
		answer.setRequired(true);
		answer.setLabel("Enter above text");
		this.session = session;
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void render(PrintWriter out) {
	
		out.write("<div id=\""+getNodeID()+"\">");
		//out.write("<img src=\"simplecaptcha\"/>");
		out.write("<img src=\"stickycaptcha.png\"/>");
		answer.render(out);
		error.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
	}
	
	public boolean validate()
	{
		//validate WLCG interop selections
		boolean valid = true;
		error.set(null);
		if(answer.validate()) {
			Captcha captcha = (Captcha) session.getAttribute(Captcha.NAME);
			if(!captcha.isCorrect(answer.getValue())) {
				valid = false;
				error.set("Incorrect answer. Please try again.");
				answer.setValue("");
				redraw();
				//NOTE - don't invalidate catpcha here.. if you want to invalidate do following
				//remove captcha from session, and refresh whole page
			}
		} else {
			valid = false;
			//error.set("Invalid answer");
		}
		/*
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
		*/
		setValid(valid);
		error.redraw();
		return valid;
	}


}
