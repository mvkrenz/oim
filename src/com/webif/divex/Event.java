package com.webif.divex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Event {
	public String action;
	public Object value;
	public Event(String _action, Object _value) {
		action = _action;
		value = _value;
	}
}
