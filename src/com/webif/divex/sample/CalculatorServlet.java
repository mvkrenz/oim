package com.webif.divex.sample;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.DivExRoot;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.StaticDE;
import com.webif.divex.DivExRoot.DivExPage;
import com.webif.divex.form.TextFormElementDE;

public class CalculatorServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<script type=\"text/javascript\" src=\"divex.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		
		out.write("<style>");
		out.write(".calc_node { border: 1px solid black; padding: 5px; margin: 5px; background-color: white;}");
		out.write(".divex_processing {color: gray;}");
		out.write(".logic {background-color: #ccf; padding: 5px; margin: 5px;  border: 1px solid black;}");
		out.write("</style>");

		DivExPage pageroot = DivExRoot.initPageRoot(request);
		CalculatorDE calc = new CalculatorDE(pageroot);
		calc.render(out);
	}

	class CalculatorDE extends DivEx
	{

		class Result extends DivEx
		{

			public Result(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			@Override
			protected void onEvent(Event e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				try {
					Double ret = root.calculate();
					out.write("= " + ret);
				} catch (Exception e) {
					out.write(e.getMessage());
				}
				out.write("</div>");
			}
		
		}
		
		abstract class CalcNode extends DivEx
		{
			public CalcNode(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			protected void onEvent(Event e) {
			}
		
			abstract public Double calculate() throws Exception;
		}
		
		class Constant extends CalcNode
		{
			TextFormElementDE constant = new TextFormElementDE(this);
			public Constant(DivEx _parent) {
				super(_parent);
				constant.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						result.redraw();
						
					}});
				constant.setWidth(100);
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				constant.render(out);
				out.write("</div>");
			}

			protected void onEvent(Event e) {
				super.onEvent(e);
			}
			public Double calculate() throws Exception { return Double.parseDouble(constant.getValue()); }
		}
		
		class StemCellNode extends CalcNode
		{
			CalcNode instance;

			ButtonDE clearbutton;
			
			ButtonDE becomepluslogic;
			ButtonDE becomeminuslogic;
			ButtonDE becomemultiplylogic;
			ButtonDE becomedividelogic;
			ButtonDE becomeconstant;
			
			public StemCellNode(DivEx _parent) {
				super(_parent);
				
				instance = null;
				
				//selector components
				becomepluslogic = new ButtonDE(this, "+");
				becomepluslogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicAddition(StemCellNode.this);
						StemCellNode.this.redraw();
					}});
				
				becomeminuslogic = new ButtonDE(this, "-");
				becomeminuslogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicSubtraction(StemCellNode.this);
						StemCellNode.this.redraw();
					}});
				
				becomemultiplylogic = new ButtonDE(this, "x");
				becomemultiplylogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicMultiplication(StemCellNode.this);
						StemCellNode.this.redraw();
					}});
				
				becomedividelogic = new ButtonDE(this, "/");
				becomedividelogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicDivision(StemCellNode.this);
						StemCellNode.this.redraw();
					}});
				
				becomeconstant = new ButtonDE(this, "123");
				becomeconstant.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new Constant(StemCellNode.this);
						StemCellNode.this.redraw();
					}});
				
				clearbutton = new ButtonDE(this, "Remove");
				clearbutton.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = null;
						StemCellNode.this.redraw();
						result.redraw();
					}});
			}
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				if(instance == null) {
					becomepluslogic.render(out);
					becomeminuslogic.render(out);
					becomemultiplylogic.render(out);
					becomedividelogic.render(out);
					becomeconstant.render(out);
	
				} else {
					out.write("<table><tr><td>");
					instance.render(out);
					out.write("</td><td>");
					clearbutton.render(out);
					out.write("</td></tr></table>");
				}	
				out.write("</div>");
			}
			public Double calculate() throws Exception { 
				if(instance == null) {
					throw new Exception("Please enter your calculation"); 
				} else {
					return instance.calculate();
				}	
			}
		}
		
		class LogicAddition extends CalcNode
		{
			CalcNode left = new StemCellNode(this);
			CalcNode right = new StemCellNode(this);
			
			public LogicAddition(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}
			public void render(PrintWriter out) {
				out.write("<div class=\"logic\" id=\""+getNodeID()+"\">");
				left.render(out);
				out.write("+");
				right.render(out);
				out.write("</div>");
			}
			public Double calculate() throws Exception { 
				return left.calculate() + right.calculate();
			}
		}
		
		class LogicSubtraction extends CalcNode
		{
			CalcNode left = new StemCellNode(this);
			CalcNode right = new StemCellNode(this);
			
			public LogicSubtraction(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}
			public void render(PrintWriter out) {
				out.write("<div class=\"logic\" id=\""+getNodeID()+"\">");
				left.render(out);
				out.write("-");
				right.render(out);
				out.write("</div>");
			}
			public Double calculate() throws Exception { 
				return left.calculate() - right.calculate();
			}
		}
		
		class LogicMultiplication extends CalcNode
		{
			CalcNode left = new StemCellNode(this);
			CalcNode right = new StemCellNode(this);
			
			public LogicMultiplication(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}
			public void render(PrintWriter out) {
				out.write("<div class=\"logic\" id=\""+getNodeID()+"\">");
				left.render(out);
				out.write("*");
				right.render(out);
				out.write("</div>");
			}
			public Double calculate() throws Exception { 
				return left.calculate() * right.calculate();
			}
		}
		
		class LogicDivision extends CalcNode
		{
			CalcNode left = new StemCellNode(this);
			CalcNode right = new StemCellNode(this);
			
			public LogicDivision(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}
			public void render(PrintWriter out) {
				out.write("<div class=\"logic\" id=\""+getNodeID()+"\">");
				left.render(out);
				out.write("/");
				right.render(out);
				out.write("</div>");
			}
			public Double calculate() throws Exception { 
				return left.calculate() / right.calculate();
			}
		}
		
		CalcNode root;
		Result result;
		public CalculatorDE(DivEx _parent) {
			super(_parent);

			root = new StemCellNode(this);
			result = new Result(this);
		}	
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<h1>Visual Calculator</h1>");
			root.render(out);
			result.render(out);
			out.write("</div>");
		}
		
	}
}
