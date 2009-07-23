package com.webif.divrep;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//let run this in single thread
public class DivRepServlet extends HttpServlet { 
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
	
    public DivRepServlet() {
        super();
        // TODO Auto-generated constructor stub
    }
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		/*
		//simulate network latency for localhost
		if(request.getLocalName().equals("localhost")) {
			try {
				Thread.sleep(600);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		*/
		response.setHeader("Cache-Control","no-cache, must-revalidate");
		
		//find the target node and process action
		String nodeid = request.getParameter("nodeid");	
		
		//get this session's divrep root and find the target node
		DivRepRoot root = DivRepRoot.getInstance(request.getSession());
		//synchronized(root) {
			DivRep div = root.findNode(nodeid);
			if(div == null) {
				//ooops.. maybe we lost something here?
				response.setContentType("text/javascript");
				PrintWriter writer = response.getWriter();
				writer.print("alert('Lost session. Reloading page...'); window.location.reload();");			
			} else {
				//dipatch divrep event handler
				div.doGet(request, response);
			}
		//}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
