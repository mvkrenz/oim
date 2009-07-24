package com.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.MaskFormatter;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.DivRepPage;
import com.divrep.DivRepRoot;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepForm;
import com.divrep.common.DivRepTextBox;

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
			name.setSampleValue("Soichi Hayashi");
			name.setRequired(true);
			
			tel = new DivRepTextBox(this);
			tel.setLabel("Telephone Number");
			tel.setSampleValue("222-333-4444");
			tel.addEventListener(new TelephoneNumberFormatterEventListener());
		}
		
		//When user clicks submit and if the form passes validations, this function will be called
		protected Boolean doSubmit() {
			//Do sometihng with the value
			alert("Thank you, " + name.getValue());
			
			//return false to stay on the form
			return false;
		}
		
		class TelephoneNumberFormatterEventListener extends DivRepEventListener {
			public void handleEvent(DivRepEvent e) {
				String value = (String)e.value;
		        int length = value.length();
		        
		        //remove non digit
		        StringBuffer buffer = new StringBuffer(length);
		        for(int i = 0; i < length; i++) {
		            char ch = value.charAt(i);
		            if (Character.isDigit(ch)) {
		                buffer.append(ch);
		            }
		        }
		        value = buffer.toString();
		        //truncate at 9 digit (or 10 if starts with 1)
	        	if(value.length() > 0 && value.charAt(0) != '1') {
	        		if(value.length() > 11) {
	        			value = value.substring(1, 10);
	        		}
	        	} else {
	        		if(value.length() > 10) {
	        			value = value.substring(0, 9);
	        		}
		        }
	        	if(value.length() == 10) {
	        		value = "+1(" + value.substring(0,3) + ")" + value.substring(3,6) + "-" + value.substring(6);
	        	} else {
	        		tel.alert("Invalid telephone number");
	        		value = "";
	        	}
		   
				tel.setValue(value);
				tel.redraw();
			}
			
		}
	}
	

}

