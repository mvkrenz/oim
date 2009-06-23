package com.webif.divrep.validator;

public class IntegerValidator implements IFormElementValidator<String>
{
	static private IntegerValidator singleton = new IntegerValidator();
	static public IntegerValidator getInstance() { return singleton; }

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
