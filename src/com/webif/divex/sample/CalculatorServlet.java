package com.webif.divex.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.TextAreaFormElementDE;

public class CalculatorServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<script type=\"text/javascript\" src=\"divex.js\"></script>");
		out.write("<link rel=\"stylesheet\" href=\"divex.css\" type=\"text/css\">");
		
		CalculatorDE calc = new CalculatorDE(DivExRoot.getInstance(request));
		calc.render(out);
	}
	
	class CalculatorLog
	{
		
	}
	
	
	class CalculatorDE extends DivEx
	{
		ArrayList<CalculatorLog> log = new ArrayList();
		TextAreaFormElementDE input;
		ButtonDE enter;
		public CalculatorDE(DivEx _parent) {
			super(_parent);
			input = new TextAreaFormElementDE(this);
			
			enter = new ButtonDE(this, "+");
			enter.addEventListener(new EventListener() {
				public void handleEvent(Event e) { enter();}});
		}

		private void enter() {
			
		}
		
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			input.render(out);
		}
		
	}
}
