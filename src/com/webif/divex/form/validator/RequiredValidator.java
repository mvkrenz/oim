package com.webif.divex.form.validator;

public class RequiredValidator implements IFormElementValidator<String>
{
	static private RequiredValidator singleton = new RequiredValidator();
	static public RequiredValidator getInstance() { return singleton; }
	
	public Boolean isValid(String value) {
		if(value.trim().length() == 0) return false;
		return true;
	}
	
	public String getMessage()
	{
		return "Cannot be an empty value";
	}
}
