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
import com.webif.divex.form.TextFormElementDE;

public class CalculatorServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<script type=\"text/javascript\" src=\"divex.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		out.write("<link rel=\"stylesheet\" href=\"divex.css\" type=\"text/css\">");
		
		out.write("<style>");
		out.write(".calc_node { border: 1px solid black; padding: 5px; margin: 3px;}");
		out.write(".divex_processing {color: gray;}");
		out.write("</style>");
		
		CalculatorDE calc = new CalculatorDE(DivExRoot.getInstance(request));
		calc.render(out);
	}

	class CalculatorDE extends DivEx
	{
		class Selector extends DivEx
		{
			CalcNode selected = null;

			public Selector(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			protected void onEvent(Event e) {
				// TODO Auto-generated method stub
				
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				if(selected == null) {
					out.write("(Nothing is Selected)");
				} else {
					selected.renderSelect(out);
				}
				out.write("</div>");
			}	
			
			public void select(CalcNode node) {
				//deselect old one
				if(node != null) {
					node.selected = false;
					node.redraw();
				}
				
				//select new one
				selected = node;
				node.selected = true;
				node.redraw();
				
				redraw();
			}
		}
		
		abstract class CalcNode extends DivEx
		{
			Boolean selected = false;
			public CalcNode(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			protected void onEvent(Event e) {
				selector.select(this);
			}
			
			abstract protected void renderSelect(PrintWriter out);
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" class=\"calc_node\" onclick=\"divex('"+getNodeID()+"', event, null);\">");
				renderContent(out);
				out.write("</div>");
			}	
			abstract protected void renderContent(PrintWriter out);
		}
		
		class Constant extends CalcNode
		{
			TextFormElementDE constant = new TextFormElementDE(this);
			public Constant(DivEx _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			protected void renderContent(PrintWriter out) {
				out.write("Constant: ");
				constant.render(out);
			}

			protected void renderSelect(PrintWriter out) {
				// TODO Auto-generated method stub
				
			}			
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
				becomepluslogic = new ButtonDE(this, "Addition");
				becomepluslogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicAddition(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.redraw();
					}});
				
				becomeminuslogic = new ButtonDE(this, "Minus");
				becomeminuslogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicSubtraction(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.redraw();
					}});
				
				becomemultiplylogic = new ButtonDE(this, "Multiply");
				becomemultiplylogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicMultiplication(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.redraw();
					}});
				
				becomedividelogic = new ButtonDE(this, "Devide");
				becomedividelogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicDivision(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.redraw();
					}});
				
				becomeconstant = new ButtonDE(this, "123");
				becomeconstant.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new Constant(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.redraw();
					}});
				
				clearbutton = new ButtonDE(this, "Clear");
				clearbutton.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = null;
						StemCellNode.this.redraw();
						selector.redraw();
					}});
			}
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\" onclick=\"divex('"+getNodeID()+"', event, null);\">");
				if(instance == null) {
					out.write("???");
				} else {
					instance.render(out);
				}
				out.write("</div>");
			}	
			protected void renderSelect(PrintWriter out) {
				if(instance == null) {
					becomepluslogic.render(out);
					becomeminuslogic.render(out);
					becomemultiplylogic.render(out);
					becomedividelogic.render(out);
					becomeconstant.render(out);
				} else {
					clearbutton.render(out);
				}
			}
			protected void renderContent(PrintWriter out) {
				//render() is overriden to not call this for StemCellNode		
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("+");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("-");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("*");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("/");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
			}
		}
		
		Selector selector;
		CalcNode root;
		public CalculatorDE(DivEx _parent) {
			super(_parent);
			
			selector = new Selector(this);
			root = new StemCellNode(this);
		
		}	
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			root.render(out);
			selector.render(out);
			out.write("</div>");
		}
		
	}
}
