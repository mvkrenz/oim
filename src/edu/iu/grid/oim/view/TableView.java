package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

public class TableView extends GenericView {
	static public enum CellStyle { NORMAL, HEADER };
	String cls = "";
	
	public TableView(String cls)
	{
		addClass(cls);
	}
	
	public class Row implements IView
	{
		private String clazz;
		public void setClass(String _clazz) {
			clazz = _clazz;
		}
		public class Cell implements IView
		{
			CellStyle style = CellStyle.NORMAL;
			IView content;
			int span;
			
			Cell(IView _content) {
				content = _content;
				span = 1;
			}
			Cell(IView _content, int _span) {
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
				case HEADER:
					out.print("<th colspan=\""+span+"\">");
					content.render(out);
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
	
	public void addRow(IView header, IView view)
	{
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(header);
		row.addCell(view);
	}
	
	public void render(PrintWriter out)
	{
		out.print("<table class='"+cls+"'>");
		for(Row row : rows) {
			row.render(out);
		}
		
		//display toolbar
		out.print("<tr><td></td><td>");
		for(IView v : children) {
			v.render(out);
		}
		out.print("</td></tr>");
		
		out.print("</table>");		
	}

}
