package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

public class TableView extends GenericView {
	static public enum CellStyle { NORMAL, HEADER };
	String cls = "";
	
	public class Row implements IView
	{
		public class Cell implements IView
		{
			CellStyle style = CellStyle.NORMAL;
			IView content;
			
			Cell(IView _content) {
				content = _content;
			}
			void setStyle(CellStyle _style) {
				style = _style;
			}
			public void render(PrintWriter out)
			{
				switch(style) {
				case NORMAL:
					out.print("<td class=\"record_data\">");
					content.render(out);
					out.print("</td>");
					break;
				case HEADER:
					out.print("<th>");
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
		public void addHeaderCell(IView content) {
			Cell cell = new Cell(content);
			cell.setStyle(TableView.CellStyle.HEADER);
			cells.add(cell);
		}
		
		public void render(PrintWriter out)
		{
			out.print("<tr>");
			for(Cell cell : cells) {
				cell.render(out);
			}
			out.print("</tr>");
		}
	}
	
	private ArrayList<Row> rows = new ArrayList();
	public void setClass(String _cls) {
		cls = _cls;
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
