package com.webif.divrep.form.validator;

public class UrlValidator implements IFormElementValidator<String>
{
	static private UrlValidator singleton = new UrlValidator();
	static public UrlValidator getInstance() { return singleton; }
	
	static String[] schemes = {"http","https"};
	static private org.apache.commons.validator.UrlValidator urlvalidator = new org.apache.commons.validator.UrlValidator(schemes);

	public Boolean isValid(String value) {
		return (urlvalidator.isValid(value));
	}
	
	public String getErrorMessage()
	{
		return "Please specify a valid http or https URL. For example: http://www.grid.iu.edu/systems";
	}
}
