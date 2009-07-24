package com.divrep.validator;

public class DivRepDoubleValidator implements DivRepIValidator<String>
{
	static private DivRepDoubleValidator singleton = new DivRepDoubleValidator();
	static public DivRepDoubleValidator getInstance() { return singleton; }

	public Boolean isValid(String value) {
		try {
			Double d = Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public String getErrorMessage()
	{
		return "Please specify a floating point value (2.343, 5, etc..)";
	}
}
