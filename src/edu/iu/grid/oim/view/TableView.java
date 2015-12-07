package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.servlet.LogServlet;

public class TableView extends GenericView {
    static Logger log = Logger.getLogger(LogServlet.class);  
    
	static public enum CellStyle { NORMAL, HEADER, BOLD };
	static int next_id = 0;
	int id;
	String cls = "";
	
	public TableView(String cls)
	{
		addClass(cls);
		id = next_id;
		next_id++;
	}
	
	public class Row implements IView
	{
		private String clazz;
		public void setClass(String _clazz) {
			clazz = _clazz;
		}
		/*
		private String tip = null;
		public void setTip(String tip) {
			this.tip = tip;
		}
		*/
		
		public class Cell implements IView
		{
			CellStyle style = CellStyle.NORMAL;
			IView content;
			int span;
			
			public Cell(IView _content) {
				content = _content;
				span = 1;
			}
			public Cell(IView _content, int _span) {
				content = _content;
				span = _span;
			}
			void setStyle(CellStyle _style) {
				style = _style;
			}
			public void render(PrintWriter out)
			{
				switch(style) {
				case NORMAL:
					out.print("<td colspan=\""+span+"\" class=\"record_data\">");
					content.render(out);
					out.print("</td>");
					break;
				case BOLD:
					out.print("<td colspan=\""+span+"\" class=\"record_data_bold\">");
					content.render(out);
					out.print("</td>");
					break;
				case HEADER:
					out.print("<th colspan=\""+span+"\">");
					content.render(out);
					/*
					if(tip != null) {
						out.print("&nbsp;<img style=\"cursor: pointer;\" align=\"top\" class=\"tinytip\" src=\"images/help.png\" title=\""+StringEscapeUtils.escapeHtml(tip)+"\"/>");
					}
					*/
					out.print("</th>");
					break;
				}
			}
		}
		
		private ArrayList<Cell> cells = new ArrayList();
		
		public void addCell(IView content) {
			cells.add(new Cell(content));
		}
		public void addCell(IView content, int span)
		{
			cells.add(new Cell(content, span));
		}
		public void addHeaderCell(IView content) {
			Cell cell = new Cell(content);
			cell.setStyle(TableView.CellStyle.HEADER);
			cells.add(cell);
		}
		public void addBoldCell(IView content) {
			Cell cell = new Cell(content);
			cell.setStyle(TableView.CellStyle.BOLD);
			cells.add(cell);
		}
		
		public void render(PrintWriter out)
		{
			out.print("<tr class=\""+clazz+"\">");
			for(Cell cell : cells) {
				cell.render(out);
			}
			out.print("</tr>");
		}
	}
	
	private ArrayList<Row> rows = new ArrayList();
	public void addClass(String _cls) {
		cls += " "+ _cls;
	}

	public void addRow(Row row)
	{
		rows.add(row);
	}
	
	public Row addRow(IView header, IView view)
	{
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(header);
		row.addCell(view);
		return row;
	}
	
	public void render(PrintWriter out)
	{
		out.print("<table id='table_"+id+"' class='"+cls+"'>");
		for(Row row : rows) {
			row.render(out);
		}
		
		//display toolbar
		if(children.size() > 0) {
			out.print("<tr><td></td><td>");
			for(IView v : children) {
				v.render(out);
			}
			out.print("</td></tr>");
		}
			
		out.print("</table>");	
	}

}
