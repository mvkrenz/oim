package edu.iu.grid.oim.view.divrep.form.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.divrep.validator.DivRepIValidator;

public class DomainNameValidator implements DivRepIValidator<String>
{
	private static final long serialVersionUID = 2512974166533246187L;

	// http://www.mkyong.com/regular-expressions/domain-name-regular-expression-example/
	private static final String PATTERN = "^[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
	private static final Pattern domainName = Pattern.compile(PATTERN);
	private Matcher matcher;

	private String message;
	
	public DomainNameValidator() {
	}
	
	public Boolean isValid(String value) {
		message = null;
		matcher = domainName.matcher(value);
		if (!matcher.find()) {
			message = "Contains invalid characters";
			return false;
		}
		return true;
	}
	
	public String getErrorMessage()
	{
		return message;
	}
}
