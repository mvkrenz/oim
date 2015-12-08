package edu.iu.grid.oim.view.divrep.form.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bouncycastle.asn1.x500.X500Name;
import com.divrep.validator.DivRepIValidator;

public class CNValidator implements DivRepIValidator<String>
{
	public enum Type { USER, HOST };
	private Type type;
	String message;
	
	public CNValidator(Type type) {
		this.type = type;
	}
		
	@Override
	public Boolean isValid(String value) {
		//ideally, we should have single regex expression, but regex is impossible to get it right for originally human beings
		switch(type) {
		case USER:
			if(value.contains("/")) {
				//we can't use / in apache format.. which is the format stored in our DB 
				//TODO why are we allowing this for host certificate then?
				message = "Please do not use /(slash).";
				return false;
			}
			break;
		case HOST:
			//split into service name and hostname
			int pos = value.indexOf("/");
			pos++; //if no service name, pos will be -1, and increment it will set it to 0
			String hostname = value.substring(pos);
			if(hostname.length() == 0) {
				message = "Hostname is not specified. ";
				return false;
			}
			if(hostname.charAt(0) == '.') {
				message = "Hostname must not being with period. ";
				return false;
			}
			break;
		default:
			message = "Unknown certificate type. ";
			return false;
		}
		
		if(!value.matches("^\\p{ASCII}*$")) {
			message = "Contains non-ascii characters.";
			return false;
		}
		
		Pattern whitespace = Pattern.compile("\\p{Space}\\p{Space}");
		Matcher matcher = whitespace.matcher(value);
		if(matcher.find())	{
			message = "Contains more than one space.";
			return false;
		}
		
		//TODO I am not sure how effective this is..
		try {
			new X500Name("CN="+value);
		} catch(Exception e) {
			message = "Couldn't parse as X500 Name. ";
			return false;
		}
			
		return true;
	}

	@Override
	public String getErrorMessage() {
		return message;
	}
}
