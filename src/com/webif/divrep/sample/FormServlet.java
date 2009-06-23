package com.webif.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divrep.common.FormBase;
import com.webif.divrep.common.Text;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepRoot.DivRepPage;

public class FormServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<html><head>");
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		out.write("<link href=\"css/divrep.sample.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("</head><body><div id=\"content\">");
		
		DivRepPage pageroot = DivRepRoot.initPageRoot(request);
		out.write("<h1>Enter Your Information</h1>");
		
		Form form = new Form(pageroot);
		form.render(out);
		
		out.write("</div></body></html>");
	}

	class Form extends FormBase
	{
		Text name;
		Text tel;
		
		public Form(DivRep _parent) {
			//second argument is the return address of this form after user hit submit (sucessfully) or cancel
			super(_parent, "http://iu.edu");
			
			name = new Text(this);
			name.setLabel("Full Name");
			name.setSampleValue("John Doe");
			name.setRequired(true);
			
			tel = new Text(this);
			tel.setLabel("Telephone Number");
			tel.setSampleValue("812-123-1234");
		}
		protected Boolean doSubmit() {
			//do sometihng with the value
			
			alert("Thank you, " + name.getValue());
			
			//return false if something goes wrong at submission.
			//The page will stay on the form.
			return false;
		}
		
	}
}
