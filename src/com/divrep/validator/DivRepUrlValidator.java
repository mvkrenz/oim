package com.divrep.validator;

public class DivRepUrlValidator implements DivRepIValidator<String>
{
	static private DivRepUrlValidator singleton = new DivRepUrlValidator();
	static public DivRepUrlValidator getInstance() { return singleton; }
	
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
