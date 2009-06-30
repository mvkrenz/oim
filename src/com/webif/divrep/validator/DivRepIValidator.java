package com.webif.divrep.validator;

public interface DivRepIValidator<T> {
	
	//do the validation
	public Boolean isValid(T value);

	//return default error string such as "This must be a valid URL."
	public String getErrorMessage();
}