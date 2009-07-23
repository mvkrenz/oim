package com.webif.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepPage;

public class HelloWorldServlet extends HttpServlet {

	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException 
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
		out.write("<h1>Hello World</h1>");
		
		//Initialize pageroot
		DivRepPage pageroot = DivRepRoot.initPageRoot(request);
		final HelloWorld hello = new HelloWorld(pageroot);
		/*
		hello.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				hello.alert("Clicked via Event Listener!");
			}});
		*/
		hello.render(out);
		
		out.write("</div></body></html>");
	}

	class HelloWorld extends DivRep
	{
			int counter = 0;
	        public HelloWorld(DivRep _parent) {
	                super(_parent);
	                // TODO Auto-generated constructor stub
	        }

	        public void render(PrintWriter out) {
			       out.write("<div id=\""+getNodeID()+"\" onclick=\"divrep(this.id);\">");
			       out.write("<h2>You have clicked me " + counter + " times</h2>");               
			       out.write("</div>"); 
	        }

			protected void onEvent(DivRepEvent e) {
				counter++;
				redraw();
			}      
	}

}
