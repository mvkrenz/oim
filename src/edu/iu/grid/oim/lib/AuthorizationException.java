package edu.iu.grid.oim.lib;

import javax.servlet.ServletException;

public class AuthorizationException extends ServletException 
{
	public AuthorizationException(String msg) {
		super(msg);
	}
}
