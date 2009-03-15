package com.webif.divex.form.validator;

public interface IFormElementValidator<T> {
	
	//do the validation
	public Boolean isValid(T value);

	//return default error string such as "This must be a valid URL."
	public String getMessage();
}
