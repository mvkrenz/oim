package edu.iu.grid.oim.view.divrep.form.validator;

import java.util.ArrayList;
import java.util.List;

import com.divrep.validator.DivRepIValidator;

import edu.vt.middleware.password.AlphabeticalSequenceRule;
import edu.vt.middleware.password.CharacterCharacteristicsRule;
import edu.vt.middleware.password.DigitCharacterRule;
import edu.vt.middleware.password.LengthRule;
import edu.vt.middleware.password.LowercaseCharacterRule;
import edu.vt.middleware.password.NonAlphanumericCharacterRule;
import edu.vt.middleware.password.NumericalSequenceRule;
import edu.vt.middleware.password.Password;
import edu.vt.middleware.password.PasswordData;
import edu.vt.middleware.password.PasswordValidator;
import edu.vt.middleware.password.QwertySequenceRule;
import edu.vt.middleware.password.RepeatCharacterRegexRule;
import edu.vt.middleware.password.Rule;
import edu.vt.middleware.password.RuleResult;
import edu.vt.middleware.password.UppercaseCharacterRule;
//import edu.vt.middleware.password.WhitespaceRule;

public class PKIPassStrengthValidator implements DivRepIValidator<String>
{
	private String message;
	private PasswordValidator validator;
	
	public PKIPassStrengthValidator() {
		
		LengthRule lengthRule = new LengthRule();//, 16);
		lengthRule.setMinimumLength(12);

		// don't allow whitespace
		//WhitespaceRule whitespaceRule = new WhitespaceRule();

		/*
		// control allowed characters
		CharacterCharacteristicsRule charRule = new CharacterCharacteristicsRule();
		// require at least 1 digit in passwords
		charRule.getRules().add(new DigitCharacterRule(1));
		// require at least 1 non-alphanumeric char
		charRule.getRules().add(new NonAlphanumericCharacterRule(1));
		// require at least 1 upper case char
		charRule.getRules().add(new UppercaseCharacterRule(1));
		// require at least 1 lower case char
		charRule.getRules().add(new LowercaseCharacterRule(1));
		// require at least 3 of the previous rules be met
		charRule.setNumberOfCharacteristics(3);
		*/
		
		// don't allow alphabetical sequences
		AlphabeticalSequenceRule alphaSeqRule = new AlphabeticalSequenceRule();

		// don't allow numerical sequences of length 3
		NumericalSequenceRule numSeqRule = new NumericalSequenceRule(3, true); //true to wrap sequences when searching for mathtces

		// don't allow qwerty sequences
		QwertySequenceRule qwertySeqRule = new QwertySequenceRule();

		// don't allow 5 repeat characters
		RepeatCharacterRegexRule repeatRule = new RepeatCharacterRegexRule(5);

		// group all rules together in a List
		List<Rule> ruleList = new ArrayList<Rule>();
		ruleList.add(lengthRule);
		//ruleList.add(whitespaceRule);
		//ruleList.add(charRule);
		ruleList.add(alphaSeqRule);
		ruleList.add(numSeqRule);
		ruleList.add(qwertySeqRule);
		ruleList.add(repeatRule);
		
		validator = new PasswordValidator(ruleList);
	}
	
	public Boolean isValid(String value) {
		message = "";

		PasswordData passwordData = new PasswordData(new Password(value));
		RuleResult result = validator.validate(passwordData);
		if (result.isValid()) {
			return true;
		} else {
			message += "Weak password: ";
			for (String msg : validator.getMessages(result)) {
				message += msg;
			}
			return false;
		}
	}
	
	public String getErrorMessage()
	{
		return message;
	}
}
