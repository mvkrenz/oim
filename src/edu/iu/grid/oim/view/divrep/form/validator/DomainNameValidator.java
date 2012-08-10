package edu.iu.grid.oim.view.divrep.form.validator;

import com.divrep.validator.DivRepIValidator;

public class DomainNameValidator implements DivRepIValidator<String>
{
	String message;
	
	public DomainNameValidator() {
	}
	
	public Boolean isValid(String value) {
		message = null;
		if(!value.matches("[a-zA-Z\\.]*")) {
			message = "Contains invalid characters";
			return false;
		}
		return true;
	}
	
	public String getErrorMessage()
	{
		return message;
	}
}
