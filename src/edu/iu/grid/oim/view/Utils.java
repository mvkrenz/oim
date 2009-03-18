package edu.iu.grid.oim.view;

import org.apache.commons.lang.StringEscapeUtils;

public class Utils {
	//if value is null, show a icon that indicates that the value is null
	static public String strFilter(String str)
	{
		if(str == null) {
			return "<img src='images/null.png' alt='null'/>";
		}
		return StringEscapeUtils.escapeHtml(str);
	}
	static public String boolFilter(Boolean b)
	{
		if(b) {
			return "True";
		} else {
			return "False";
		}
	}
}
