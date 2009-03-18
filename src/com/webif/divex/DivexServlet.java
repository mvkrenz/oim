package com.webif.divex;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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
		//request.setCharacterEncoding("UTF-8");
		
		PrintWriter writer = response.getWriter();
		
		//prevent ie6 cacheing of loaded content
		response.setHeader("Cache-Control","no-cache, must-revalidate");
		
		//get this sessions's divex root
		DivExRoot root = DivExRoot.getInstance(request);
		
		//find the target node and process action
		String action = request.getParameter("action");
		String nodeid = request.getParameter("nodeid");	
		DivEx div = root.findNode(nodeid);
		if(div == null) {
			//ooops.. maybe we lost something here?
			response.setContentType("text/javascript");
			writer.print("alert('Lost session. Reloading page...'); window.location.reload();");			
		} else {
			if(action != null) {
				
				System.out.println(action + " on " + nodeid);
				
				if(action.compareTo("click") == 0) {
					response.setContentType("text/javascript");
					String value = request.getParameter("value");
					synchronized(root) {
						div.click(value);
						writer.print(root.outputUpdatecode());
					}
				} else if(action.compareTo("change") == 0) {
					response.setContentType("text/javascript");
					String value = request.getParameter("value");
					synchronized(root) {
						div.change(value);
						writer.print(root.outputUpdatecode());
					}
				} else if(action.compareTo("load") == 0) {
					//we don't synchronize load action - since it should be read-only
					response.setContentType("text/html");
					writer.print(div.renderInside());
				}
			}	
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
