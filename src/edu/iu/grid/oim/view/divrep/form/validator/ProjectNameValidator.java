package edu.iu.grid.oim.view.divrep.form.validator;

import com.divrep.validator.DivRepIValidator;

public class ProjectNameValidator implements DivRepIValidator<String>
{
	String message;
	
	public ProjectNameValidator() {
	}
	
	public Boolean isValid(String value) {
		message = null;
		if(!value.matches("[-0-9a-zA-Z]*")) {
			message = "Please use alphabet, number, or -(dash).";
			return false;
		}
		return true;
	}
	
	public String getErrorMessage()
	{
		return message;
	}
}
