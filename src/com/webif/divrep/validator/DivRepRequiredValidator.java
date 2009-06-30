package com.webif.divrep.validator;

public class DivRepRequiredValidator implements DivRepIValidator<String>
{
	static private DivRepRequiredValidator singleton = new DivRepRequiredValidator();
	static public DivRepRequiredValidator getInstance() { return singleton; }
	
	public Boolean isValid(String value) {
		if(value.trim().length() == 0) return false;
		return true;
	}
	
	public String getErrorMessage()
	{
		return "Cannot be an empty value";
	}
}
