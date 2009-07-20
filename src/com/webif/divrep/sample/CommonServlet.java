package com.webif.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.common.DivRepSelectBox;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;
import com.webif.divrep.DivRepRoot.DivRepPage;

public class CommonServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		out.write("<link href=\"css/divrep.samples.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		out.write("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");

		DivRepPage pageroot = DivRepRoot.initPageRoot(request);
		
		out.write("<h1>DivRep Primitive Controls</h1>");
		
		out.write("<h2>Buttons</h2>");
		Buttons buttons = new Buttons(pageroot);
		buttons.render(out);
		
		out.write("<h2>Select Box</h2>");
		out.write("<b>Coffee</b>");
		Select select = new Select(pageroot);
		select.render(out);
	}
	
	class Buttons extends DivRep
	{
		//Button for each styles
	    DivRepButton normal_button;
	    DivRepButton link_button;
	    DivRepButton image_button;

	    public Buttons(DivRep _parent) {
	        super(_parent);

	        //instantiate the buttons
	        normal_button = new DivRepButton(this, "Normal Button");
	        
	        link_button = new DivRepButton(this, "Link Button");
	        link_button.setStyle(DivRepButton.Style.ALINK);
	        
	        image_button = new DivRepButton(this, "css/divrep/osg_logo.png");
	        image_button.setStyle(DivRepButton.Style.IMAGE);

	        //then add event listeners so that I can receive click events
	        normal_button.addEventListener(new DivRepEventListener() {
	        	public void handleEvent(DivRepEvent e) {
	            alert("You clicked a normal button");
	        	}
	        });
	        link_button.addEventListener(new DivRepEventListener() {
	        	public void handleEvent(DivRepEvent e) {
	            alert("You clicked a link button");
	        	}
	        });
	        image_button.addEventListener(new DivRepEventListener() {
	        	public void handleEvent(DivRepEvent e) {
	            alert("You clicked a image button");
	        	}
	        });
	        
	    }

	    public void render(PrintWriter out) {
	        out.write("<div id=\""+getNodeID()+"\">");

	        //I have to render the button
	        normal_button.render(out);
	        out.write("<br/>");
	        link_button.render(out);
	        out.write("<br/>");
	        image_button.render(out);
	        out.write("<br/>");
	        
	        out.write("</div>");
	    }

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class Select extends DivRep
	{
		//Button for each styles
	    DivRepSelectBox select;
	    
	    public Select(DivRep _parent) {
	        super(_parent);

	        //setup select box options
	        TreeMap<Integer, String> options = new TreeMap<Integer, String>();
	        options.put(1, "Americano");
	        options.put(2, "Cappuccino");
	        options.put(3, "Caffe Latte");
	        options.put(4, "Frappe");
	        options.put(5, "Iced");
	        options.put(6, "Mocha");
	        options.put(7, "Oliang");
	        options.put(8, "Turkish");
	        
	        //instantiate the select box
	        select = new DivRepSelectBox(this, options);
	        select.addEventListener(new DivRepEventListener() {
	        	public void handleEvent(DivRepEvent e) {
	        		alert("You have selected the item " + e.value);
	        	}
	        });
	    }

	    public void render(PrintWriter out) {
	        out.write("<div id=\""+getNodeID()+"\">");
	        
	        //render it
	        select.render(out);
	        
	        out.write("</div>");
	    }

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}
	}	
}