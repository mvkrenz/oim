package com.webif.divex;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//let run this in single thread
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
		//prevent ie6 cacheing of loaded content
		response.setHeader("Cache-Control","no-cache, must-revalidate");
		
		//find the target node and process action
		String nodeid = request.getParameter("nodeid");	
		
		//get this session's divex root and find the target node
		DivExRoot root = DivExRoot.getInstance(request);
		synchronized(root) {
			DivEx div = root.findNode(nodeid);
			if(div == null) {
				//ooops.. maybe we lost something here?
				response.setContentType("text/javascript");
				PrintWriter writer = response.getWriter();
				writer.print("alert('Lost session. Reloading page...'); window.location.reload();");			
			} else {
				div.doGet(root, request, response);
			}
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
