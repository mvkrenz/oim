package com.webif.divex.form.validator;

public class DoubleValidator implements IFormElementValidator<String>
{
	static private DoubleValidator singleton = new DoubleValidator();
	static public DoubleValidator getInstance() { return singleton; }

	public Boolean isValid(String value) {
		try {
			Double d = Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public String getMessage()
	{
		return "Please specify a floating point value (2.343, 5, etc..)";
	}
}
