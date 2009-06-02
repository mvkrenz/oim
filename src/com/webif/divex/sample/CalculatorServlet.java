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
import com.webif.divex.form.TextFormElementDE;

public class CalculatorServlet extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{	
		PrintWriter out = response.getWriter();
		out.write("<script type=\"text/javascript\" src=\"divex.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		out.write("<link rel=\"stylesheet\" href=\"divex.css\" type=\"text/css\">");
		
		out.write("<style>");
		out.write(".calc_node { border: 1px solid black; padding: 5px; margin: 5px; background-color: white;}");
		out.write(".divex_processing {color: gray;}");
		out.write(".selector {float: right; background-color: #ccc;}");
		out.write(".selected {background-color: #ccf;}");
		out.write("</style>");
		
		CalculatorDE calc = new CalculatorDE(DivExRoot.getInstance(request.getSession()));
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
				out.write("<div class=\"selector\" id=\""+getNodeID()+"\">");
				if(selected == null) {
					out.write("(Nothing is Selected)");
				} else {
					selected.renderSelect(out);
				}
				out.write("</div>");
			}	
			
			public void select(CalcNode node) {
				//if same node is selected, do nothing
				if(node == selected) {
					return;
				}
				//deselect old one
				if(selected != null) {
					selected.selected = false;
					selected.redraw();
				}
				
				//select new one
				selected = node;
				if(node != null) {
					node.selected = true;
					node.redraw();
				}
				redraw();
			}
		}
		
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
					out.write("result: " + ret);
				} catch (Exception e) {
					out.write(e.getMessage());
				}
				out.write("</div>");
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
			final public void render(PrintWriter out) {
				String classes = "";
				if(selected) {
					classes += " selected";
				}
				out.write("<div id=\""+getNodeID()+"\" class=\"calc_node"+classes+"\" onclick=\"divex('"+getNodeID()+"', event, null);\">");
				renderContent(out);
				out.write("</div>");
			}	
			abstract protected void renderContent(PrintWriter out);
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
			}

			protected void renderContent(PrintWriter out) {
				constant.render(out);
			}

			protected void renderSelect(PrintWriter out) {
				// TODO Auto-generated method stub
			}	
			protected void onEvent(Event e) {
				super.onEvent(e);
				//redraw() looses the focus -- re-select the constant..
				/*
				if(e.action.equals("click")) {
					constant.focus();
				}
				*/
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
				becomepluslogic = new ButtonDE(this, "Addition");
				becomepluslogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicAddition(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.select(null);
					}});
				
				becomeminuslogic = new ButtonDE(this, "Minus");
				becomeminuslogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicSubtraction(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.select(null);
					}});
				
				becomemultiplylogic = new ButtonDE(this, "Multiply");
				becomemultiplylogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicMultiplication(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.select(null);
					}});
				
				becomedividelogic = new ButtonDE(this, "Devide");
				becomedividelogic.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new LogicDivision(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.select(null);
					}});
				
				becomeconstant = new ButtonDE(this, "123");
				becomeconstant.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = new Constant(StemCellNode.this);
						StemCellNode.this.redraw();
						selector.select(null);
					}});
				
				clearbutton = new ButtonDE(this, "Clear");
				clearbutton.addEventListener(new EventListener() {
					public void handleEvent(Event e) {
						instance = null;
						StemCellNode.this.redraw();
						selector.select(null);
					}});
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
				if(instance == null) {
					out.write("???");
				} else {
					instance.render(out);
				}	
			}
			public Double calculate() throws Exception { 
				if(instance == null) {
					throw new Exception("Can't calculate due to stemcell value"); 
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("+");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("-");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("*");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
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
			protected void renderContent(PrintWriter out) {
				left.render(out);
				out.write("/");
				right.render(out);
			}
			protected void renderSelect(PrintWriter out) {
				out.write("Not yet implemented");
			}
			public Double calculate() throws Exception { 
				return left.calculate() / right.calculate();
			}
		}
		
		Selector selector;
		CalcNode root;
		Result result;
		public CalculatorDE(DivEx _parent) {
			super(_parent);
			
			selector = new Selector(this);
			root = new StemCellNode(this);
			result = new Result(this);
		}	
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
		}
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			selector.render(out);
			root.render(out);
			result.render(out);
			out.write("</div>");
		}
		
	}
}
