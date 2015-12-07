package edu.iu.grid.oim.view.divrep.form.validator;

import java.util.Collection;
import java.util.HashSet;

import com.divrep.validator.DivRepIValidator;

public class IncaseUniqueValidator implements DivRepIValidator<String>
{
	//no singleton - user must provide the list of prohibited values

	private static final long serialVersionUID = 1L;
	private Collection<String> prohibited;
	public IncaseUniqueValidator(Collection<String> _prohibited)
	{
		//convert all items to upper case.
		prohibited = new HashSet<String>(_prohibited.size());
		for(String item : _prohibited) {
			prohibited.add(item.toUpperCase());
		}
		prohibited = _prohibited;
	}
	
	public String getErrorMessage()
	{
		return "This value is already used (case insensitive). Please enter different value.";
	}
	
	@Override
	public Boolean isValid(String value) {
		String up_value = value.toUpperCase();
		return !(prohibited.contains(up_value));
	}
}
