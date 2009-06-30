package com.webif.divrep.validator;

public class DivRepIntegerValidator implements DivRepIValidator<String>
{
	static private DivRepIntegerValidator singleton = new DivRepIntegerValidator();
	static public DivRepIntegerValidator getInstance() { return singleton; }

	public Boolean isValid(String value) {
		try {
			int d = Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public String getErrorMessage()
	{
		return "Please specify a integer value (1, 4, 10, etc..)";
	}
}
