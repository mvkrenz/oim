package com.webif.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepRoot.DivRepPage;
import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.common.DivRepStaticContent;
import com.webif.divrep.common.DivRepToggler;

public class TreeServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<html><head>");
		
		//Load DivRep Stuff
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		out.write("<link href=\"css/divrep.sample.css\" rel=\"stylesheet\" type=\"text/css\"/>");

		//Load jQuery
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		out.write("</head><body><div id=\"content\">");		
		out.write("<h1>Who's Hiding!?</h1>");
		
		//Create DivRep form
		DivRepPage pageroot = DivRepRoot.initPageRoot(request);
		Tree tree = new Tree(pageroot);
		tree.render(out);
		
		out.write("</div></body></html>");
	}
	
	class Tree extends DivRep
	{
		DivRepToggler root;
		
		public Tree(DivRep _parent) {
			super(_parent);
			Child child = new Child(this);
			root = new DivRepToggler(this, child);
			
			DivRepButton show_button = root.getShowButton();
			show_button.setStyle(DivRepButton.Style.IMAGE);
			show_button.setTitle("images/folder-close.gif");

			DivRepButton hide_button = root.getHideButton();
			hide_button.setStyle(DivRepButton.Style.IMAGE);
			hide_button.setTitle("images/folder-open.gif");
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			root.render(out);
			out.write("</div>");
		}
	}
	
	class Child extends DivRep
	{
		DivRepToggler root;
		
		public Child(DivRep _parent) {
			super(_parent);
			Child child = new Child(this);
			root = new DivRepToggler(this, child);
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"indent\">");
			out.write("Child conten");
			out.write("</div>");
		}
		
	}
}

	