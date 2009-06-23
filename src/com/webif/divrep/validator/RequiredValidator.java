package com.webif.divrep.validator;

public class RequiredValidator implements IFormElementValidator<String>
{
	static private RequiredValidator singleton = new RequiredValidator();
	static public RequiredValidator getInstance() { return singleton; }
	
	public Boolean isValid(String value) {
		if(value.trim().length() == 0) return false;
		return true;
	}
	
	public String getErrorMessage()
	{
		return "Cannot be an empty value";
	}
}
