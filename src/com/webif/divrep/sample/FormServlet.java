package com.webif.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.common.DivRepForm;
import com.webif.divrep.common.DivRepTextBox;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepRoot.DivRepPage;

public class FormServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<html><head>");
		
		//Load DivRep Stuff
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		out.write("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/divrep.samples.css\" rel=\"stylesheet\" type=\"text/css\"/>");

		//Load jQuery
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		
		out.write("</head><body><div id=\"content\">");		
		out.write("<h1>Sample Form</h1>");
		
		//Create DivRep form
		DivRepPage pageroot = DivRepRoot.initPageRoot(request);
		Form form = new Form(pageroot);
		form.render(out);
		
		out.write("</div></body></html>");
	}

	//Define our DivRep form
	class Form extends DivRepForm
	{
		DivRepTextBox name;
		DivRepTextBox tel;
		//SomeDivRep extra;
		
		public Form(DivRep _parent) {
			//Second argument is the return address of this form after user hit submit (sucessfully) or cancel
			super(_parent, "http://www.iu.edu");
			
			name = new DivRepTextBox(this);
			name.setLabel("Full Name");
			name.setRequired(true);
			
			tel = new DivRepTextBox(this);
			tel.setLabel("Telephone Number");
			
			//extra = new SomeDivRep(this);
		}
		
		//When user clicks submit and if the form passes validations, this function will be called
		protected Boolean doSubmit() {
			//Do sometihng with the value
			alert("Thank you, " + name.getValue());
			
			//return false to stay on the form
			return false;
		}
	}
}
