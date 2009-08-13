package edu.iu.grid.oim.lib;

import javax.servlet.ServletException;

public class AuthorizationException extends ServletException 
{
	AuthorizationException(String msg) {
		super(msg);
	}
}
