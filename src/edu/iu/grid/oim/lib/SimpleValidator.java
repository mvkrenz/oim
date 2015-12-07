package edu.iu.grid.oim.lib;

public class SimpleValidator {
	static public boolean notempty(String value)
	{
		return minsize(value.trim(), 1);
	}
	static public boolean minsize(String value, int minsize) {
		return(value.length() < minsize);
	}
}
