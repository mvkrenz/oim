package edu.iu.grid.oim.view.divrep.form.validator;

import com.divrep.validator.DivRepIValidator;

public class DivRepPassStrengthValidator implements DivRepIValidator<String>
{
	String message;
	
	public DivRepPassStrengthValidator() {
	}
	
	public Boolean isValid(String value) {
		int len = value.trim().length();
		
		if(len < 8) {
			message = "Too short. Minimum of 8 characters required. Currently it is "+len+" characters";
			return false;
		}
		//TODO.. what else?
		
		return true;
	}
	
	public String getErrorMessage()
	{
		return message;
	}
}
