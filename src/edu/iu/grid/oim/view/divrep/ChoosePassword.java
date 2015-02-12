package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepPassword;
import com.divrep.validator.DivRepIValidator;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.view.divrep.form.validator.PKIPassStrengthValidator;

public class ChoosePassword extends DivRepFormElement<String> {
	private static final long serialVersionUID = 3068168774583831231L;
	private UserContext context;
	private DivRepPassword passphrase; //for guest
	private DivRepPassword passphrase_confirm; //for guest
	
	public ChoosePassword(DivRep parent, UserContext context) {
		super(parent);
		this.context = context;
		
		passphrase = new DivRepPassword(this);
		passphrase.setLabel("Password");
		passphrase.addValidator(new PKIPassStrengthValidator());
		passphrase.setRequired(true);
		passphrase.addEventListener(new DivRepEventListener() {
			@Override
			public void handleEvent(DivRepEvent e) {
				if(passphrase_confirm.getValue() != null) {
					passphrase_confirm.validate();
				}
			}
		});
		passphrase_confirm = new DivRepPassword(this);
		passphrase_confirm.setLabel("Re-enter password");
		passphrase_confirm.addValidator(new DivRepIValidator<String>() {
			String message;
			@Override
			public Boolean isValid(String value) {
				if(value.equals(passphrase.getValue())) return true;
				message = "Passphrase does not match";
				return false;
			}

			@Override
			public String getErrorMessage() {
				return message;
			}
		});
		passphrase_confirm.setRequired(true);
		
		if(context.isSecure()) {
			passphrase.setRepopulate(true);
			passphrase_confirm.setRepopulate(true);
		}
	}

	@Override
	public void render(PrintWriter out) {
		out.write("<div id=\"\">");
		Authorization auth = context.getAuthorization();
		out.write("<p class=\"help-block\">Please choose a password to issue your certificate and encrypt your private key.</p>");
		if(!auth.isUser()) {
			out.write("<p class=\"help-block alert alert-error\"><b>IMPORTANT</b>: If you forget this password, you will not be able to issue your certificate and import it to your browser after it is issued.</p>");
		}
		passphrase.render(out);
		passphrase_confirm.render(out);
		error.render(out);
		out.write("</div>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public String getValue() {
		return passphrase.getValue();
	}

	
}
