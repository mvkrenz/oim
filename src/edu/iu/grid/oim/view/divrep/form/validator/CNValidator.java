package edu.iu.grid.oim.view.divrep.form.validator;

import org.bouncycastle.asn1.x500.X500Name;

import com.divrep.validator.DivRepIValidator;

public class CNValidator implements DivRepIValidator<String>
{

	private static final long serialVersionUID = 3609459828510820900L;
	String message;
	
	public CNValidator() {
	}
		
	@Override
	public Boolean isValid(String value) {
		
		// MT 22-APR-2014
		// Addresses https://jira.opensciencegrid.org/browse/OIM-82:
		// Names like .iu.edu are invalid
		final char PERIOD = '.'; 
		
		if (PERIOD == value.charAt(0))	{
			message = "Contains invalid character . at the beginning";
			return false;
		}

		if(value.contains("/")) {
			//we can't use / in apache format.. which is the format stored in our DB
			message = "Please do not use /(slash)";
			return false;
		}
		
		//if(!value.matches("[-0-9a-zA-Z\' ]*")) {
		if(!value.matches("^\\p{ASCII}*$")) {
			message = "Contains non-ascii characters";
			return false;
		}
		
		//I am not sure how effective this is..
		try {
			@SuppressWarnings("unused")
			X500Name name = new X500Name("CN="+value);
		} catch(Exception e) {
			message = "Couldn't parse as X500 Name";
			return false;
		}

		return true;
	}

	@Override
	public String getErrorMessage() {
		return message;
	}
}
