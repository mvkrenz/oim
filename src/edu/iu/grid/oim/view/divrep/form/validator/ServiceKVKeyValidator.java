package edu.iu.grid.oim.view.divrep.form.validator;

import com.divrep.validator.DivRepIValidator;

public class ServiceKVKeyValidator implements DivRepIValidator<String>
{
	String message;
	
	public ServiceKVKeyValidator() {
	}
	
	public Boolean isValid(String value) {
		message = null;
		if(!value.matches("[_0-9a-zA-Z\\.]*")) {
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
