package com.webif.divrep.sample;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.webif.divrep.common.DivRepButton;
import com.webif.divrep.DivRep;
import com.webif.divrep.DivRepRoot;
import com.webif.divrep.DivRepEvent;
import com.webif.divrep.DivRepEventListener;

public class TradingGameServlet extends HttpServlet {
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();
		out.write("<script type=\"text/javascript\" src=\"divrep.js\"></script>");
		out.write("<script type=\"text/javascript\" src=\"http://jqueryjs.googlecode.com/files/jquery-1.3.2.min.js\"></script>");
		out.write("<link href=\"css/divrep.css\" rel=\"stylesheet\" type=\"text/css\"/>");
		
		out.write("<style>");
		out.write(".selector {float: right; background-color: #ccc;}");
		out.write(".selected {background-color: #ccf;}");
		out.write("</style>");
		
		TradingGame calc = new TradingGame(DivRepRoot.initPageRoot(request));
		calc.render(out);
	}

	class TradingGame extends DivRep
	{
		abstract class Town
		{
			//returns name of the town
			abstract public String getName();
			
			abstract public void render(PrintWriter out);
			protected void renderLocation(PrintWriter out, final Town newtown, final Double day_cost)
			{
				DivRepButton button = new DivRepButton(main, "Go to "+newtown.getName()+" (Cost: "+day_cost+" day)");
				button.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						town = newtown;
						day += day_cost;
						redrawall();
					}}
				);
				button.render(out);
			}
			protected void renderTrader(PrintWriter out, final String item, final Double cost)
			{
				out.write("<p>");
				out.write(item+" (Cost: $"+cost+")");
				
				DivRepButton buy = new DivRepButton(main, "Buy 1");
				buy.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						if(money < cost) {
							alert("You don't have enough money!");
						} else {
							trade(item, 1, cost);
						}
					}}
				);
				buy.render(out);
				
				DivRepButton sale = new DivRepButton(main, "Sale 1");
				sale.addEventListener(new DivRepEventListener() {
					public void handleEvent(DivRepEvent e) {
						Integer count = bag.get(item);
						if(count == null || count < 1) {	
							alert("You don't have this item to sell");
						} else {
							trade(item, -1, cost);
						}							
						
					}}
				);
				sale.render(out);
				out.write("</p>");
			}
		}
		
		class BTown extends Town
		{
			public String getName() { return "BTown"; }

			public void render(PrintWriter out) {

				out.write("<h3>You can go to</h3>");
				renderLocation(out, new MartinsVille(), 0.1);
				
				out.write("<h3>Farmer's Market</h3>");
				renderTrader(out, "Wine", 10D);
				renderTrader(out, "Vegitables", 3D);
				renderTrader(out, "Plants", 5D);
				
				out.write("<h3>IU Bookstore</h3>");			
				renderTrader(out, "DivRepTextBox Book", 100D);
				renderTrader(out, "Book", 15D);
				
				out.write("<h3>Downtown</h3>");
				renderTrader(out, "Bicycle", 300D);
				renderTrader(out, "T-shirts", 10D);
			}
		}
		
		class MartinsVille extends Town
		{
			public String getName() { return "MartinsVille"; }
			public void render(PrintWriter out) {
		
				out.write("<h3>You can go to</h3>");
				renderLocation(out, new BTown(), 0.1);
				renderLocation(out, new IndiTown(), 0.1);
				
				out.write("<h3>Downtown</h3>");
				renderTrader(out, "Wine", 12D);
				
				out.write("<h3>Wallmart</h3>");
				renderTrader(out, "T-shirts", 7.5D);
			}
		}
		
		class IndiTown extends Town
		{
			public String getName() { return "Indi"; }

			public void render(PrintWriter out) {

				out.write("<h3>You can go to</h3>");
				renderLocation(out, new MartinsVille(), 0.1);
				
				out.write("<h3>Downtown</h3>");
				renderTrader(out, "Wine", 15D);
				renderTrader(out, "Vegitables", 5D);
								
				out.write("<h3>Downtown</h3>");
				renderTrader(out, "DivRepTextBox Book", 70D);
				renderTrader(out, "Book", 12D);
				renderTrader(out, "Marble Brick", 50D);
			}
		}
		
		class StatusWindow extends DivRep
		{
			public StatusWindow(DivRep _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			@Override
			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
			}

			@Override
			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<h2>Status</h2>");
				out.write("<p>Money: " + money + "</p>");
				out.write("<p>Day: " + day + "</p>");
				
				out.write("<h3>Bag</h3>");
				for(String name : bag.keySet()) {
					Integer count = bag.get(name);
					out.write("<p>" + name + " : " + count + "</p>");
				}
				out.write("</div>");	
			}
		}
		
		class MainWindow extends DivRep
		{

			public MainWindow(DivRep _parent) {
				super(_parent);
				// TODO Auto-generated constructor stub
			}

			protected void onEvent(DivRepEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void render(PrintWriter out) {
				out.write("<div id=\""+getNodeID()+"\">");
				out.write("<h2>You are at "+town.getName()+"</h2>");
				town.render(out);
				out.write("</div>");
			}
		}
	
		//UI components
		MainWindow main;		
		StatusWindow status;
		
		//status
		int money = 300;
		float day = 1;
		Town town = new BTown();
		TreeMap<String/*item*/, Integer/*count*/> bag = new TreeMap<String, Integer>();
		
		public TradingGame(DivRep _parent) {
			super(_parent);
			status = new StatusWindow(this);
			main = new MainWindow(this);
		}

		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			out.write("<h1>Trade Game</h1>");
			out.write("<table width=\"100%\"><tr>");
			
			out.write("<td>");
			main.render(out);
			out.write("</td>");
			
			out.write("<td>");
			status.render(out);
			out.write("</td>");
			
			out.write("</div>");	
		}
		
		public void redrawall()
		{
			redraw();
		}
		
		public void trade(String item, int count, double cost)
		{
			money -= cost * count;
			
			Integer current_count = bag.get(item);
			if(current_count == null) {
				bag.put(item, count);
			} else {
				bag.put(item, current_count + count);
			}
			status.redraw();
		}
	}
}
