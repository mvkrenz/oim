package com.webif.divex;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Event {
	public HttpServletRequest request;
	public HttpServletResponse response;
	public Event(HttpServletRequest _request, HttpServletResponse _response) {
		request = _request;
		response = _response;
	}
	public String getAction()
	{
		return request.getParameter("action");
	}
	public String getValue()
	{
		return request.getParameter("value");
	}
	public String getParameter(String key)
	{
		return request.getParameter(key);
	}
}
