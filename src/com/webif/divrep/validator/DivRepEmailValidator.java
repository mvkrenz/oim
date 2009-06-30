package com.webif.divrep.validator;

public class DivRepEmailValidator implements DivRepIValidator<String>
{
	static private DivRepEmailValidator singleton = new DivRepEmailValidator();
	static public DivRepEmailValidator getInstance() { return singleton; }
	
	public Boolean isValid(String value) {
		return org.apache.commons.validator.EmailValidator.getInstance().isValid(value);
	}
	
	public String getErrorMessage()
	{
		return "Please specify a valid email address.";
	}
}
