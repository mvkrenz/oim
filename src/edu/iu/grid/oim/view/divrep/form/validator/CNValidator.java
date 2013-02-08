package edu.iu.grid.oim.view.divrep.form.validator;

import org.bouncycastle.asn1.x500.X500Name;

import com.divrep.validator.DivRepIValidator;

public class CNValidator implements DivRepIValidator<String>
{
	String message;
	
	public CNValidator() {
	}
		
	@Override
	public Boolean isValid(String value) {
		//I am not sure how effective this is..
		try {
			X500Name name = new X500Name("CN="+value);
		} catch(Exception e) {
			return false;
		}
		
		if(value.contains("/")) {
			//we can't use / in apache format.. which is the format stored in our DB
			return false;
		}
		
		return true;
	}

	@Override
	public String getErrorMessage() {
		return "Failed to validate CN";
	}
}
