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
import com.webif.divrep.DivRepRoot.DivRepPage;
import com.webif.divrep.common.DivRepButton;

//http://lh6.ggpht.com/_uLPl8oMHccE/Si8ZFSCZL1I/AAAAAAAAEsM/2EGwNFX_yLM/s800/P1120510.JPG
public class PuzzleGameServlet extends HttpServlet {
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
		Puzzle puzzle = new Puzzle(pageroot);
		puzzle.render(out);
		
		out.write("</div></body></html>");
	}

	class Puzzle extends DivRep
	{
		//number of pieces to devide
		final int x_devide = 8;
		final int y_devide = 5;
		Piece[] pieces = new Piece[x_devide * y_devide];
		
		//size of the image
		final int xsize = 800;
		final int ysize = 450;
		
		String []image_urls = new String[6];	
		int current_image = 0;
		DivRepButton nextbutton;
		
		public Puzzle(DivRep _parent) {
			//Second argument is the return address of this form after user hit submit (sucessfully) or cancel
			super(_parent);
			
			image_urls[0] = "http://lh6.ggpht.com/_uLPl8oMHccE/Si8ZFSCZL1I/AAAAAAAAEsM/2EGwNFX_yLM/s800/P1120510.JPG";	
			image_urls[1] = "http://lh5.ggpht.com/_uLPl8oMHccE/ShNn6LYxnSI/AAAAAAAAEWo/Yy4dDf_EZO0/s800/P1110668.JPG";
			image_urls[2] = "http://lh4.ggpht.com/_uLPl8oMHccE/ShNoOSJ_e_I/AAAAAAAAEW8/JBWY3w3odeo/s800/P1110700.JPG";
			image_urls[3] = "http://lh5.ggpht.com/_uLPl8oMHccE/Sh8RA9LmnoI/AAAAAAAAEqg/DWj9coJtEw4/s800/P1120372.JPG";
			image_urls[4] = "http://lh5.ggpht.com/_uLPl8oMHccE/ShNqDKAthKI/AAAAAAAAEZE/yBe9_3XFHYE/s800/P1110865.JPG";
			image_urls[5] = "http://lh6.ggpht.com/_uLPl8oMHccE/ShNsvzYv34I/AAAAAAAAEcc/hLztH0tHCPs/s800/P1120116.JPG";
		
			int xpiecesize = xsize / x_devide;
			int ypiecesize = ysize / y_devide;
			
			for(int y = 0; y < y_devide; ++y) {
				for(int x = 0;x < x_devide; ++x) {					
					pieces[y*x_devide+x] = new Piece(this, xpiecesize, ypiecesize, xpiecesize*x,ypiecesize*y);
				}
			}
			
			nextbutton = new DivRepButton(this, "Next Puzzle");
			nextbutton.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					Puzzle.this.nextimage();
					
				}});
		}

		public void nextimage()
		{
			current_image++;
			if(current_image == 6) current_image = 0;
			
			for(int y = 0; y < y_devide; ++y) {
				for(int x = 0;x < x_devide; ++x) {					
					pieces[y*x_devide+x].shown = false;
				}
			}
			
			Puzzle.this.redraw();
		}
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.print("<div id=\""+getNodeID()+"\">");
			out.print("<table style=\"border-collapse:collapse; background-image: url('"+image_urls[current_image]+"');\">");
			
			//display all masks
			for(int y = 0; y < y_devide; ++y) {
				out.print("<tr>");
				for(int x = 0;x < x_devide; ++x) {
					pieces[y*x_devide+x].render(out);
				}
				out.print("</tr>");
			}
			out.print("</table>");
			
			nextbutton.render(out);
			
			out.print("</div>");
		}
		
		class Piece extends DivRep
		{
			Boolean shown = false;
			int xsize, ysize;
			int xoffset, yoffset;
			
			public Piece(DivRep _parent, int _xsize, int _ysize, int _xoffset, int _yoffset) {
				super(_parent);
				xsize = _xsize;
				ysize = _ysize;
				xoffset = _xoffset;
				yoffset = _yoffset;
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				shown = true;
				redraw();
			}

			@Override
			public void render(PrintWriter out) {
				if(!shown) {
					out.write("<td style=\"cursor: pointer; text-align: center; background-color: white; width: "+xsize+"px; height: "+ysize+"px;\" onclick=\"divrep('"+getNodeID()+"');\" id=\""+getNodeID()+"\">");
					out.write("<font size=\"25pt\">?</font>");
					out.write("</td>");
				} else {
					out.write("<td style=\"width: "+xsize+"px; height: "+ysize+"px;\">");
					out.write("&nbsp;</td>");
				
				}
			}
		}
		
	}

}
