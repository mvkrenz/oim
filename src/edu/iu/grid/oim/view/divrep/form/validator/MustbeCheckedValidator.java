package edu.iu.grid.oim.view.divrep.form.validator;

import com.divrep.validator.DivRepIValidator;

public class MustbeCheckedValidator implements DivRepIValidator<Boolean>
{
	String message;
	
	public MustbeCheckedValidator(String message) {
		this.message = message;
	}
		
	@Override
	public Boolean isValid(Boolean value) {
		return value;
	}

	@Override
	public String getErrorMessage() {
		return message;
	}
}
