package com.webif.divex;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class DivexServlet
 */
public class DivexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DivexServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		PrintWriter writer = response.getWriter();
		
		//prevent ie6 cacheing of loaded content
		response.setHeader("Cache-Control","no-cache, must-revalidate");
		
		//get our application
    	HttpSession session = request.getSession();
    	Div app = (Div)session.getAttribute("divex");
		
		//find the target node and process action
		String action = request.getParameter("action");
		String nodeid = request.getParameter("nodeid");	
		Div div = Div.findNode(nodeid);
		Event e = new Event();
		if(div != null && action != null) {
			if(action.compareTo("click") == 0) {
				div.onClick(e);
				response.setContentType("text/javascript");
				writer.print(Div.outputUpdatecode());		
			} else if(action.compareTo("load") == 0) {
				response.setContentType("text/html");
				writer.print(div.toHtml());
			} 
		}	
    	
    	session.setAttribute("divex", app);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
