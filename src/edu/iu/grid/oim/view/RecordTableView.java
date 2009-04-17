package edu.iu.grid.oim.view;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.view.TableView;

public class RecordTableView extends TableView {

	public RecordTableView(String cls)
	{
		super(cls);
	}
	public RecordTableView()
	{
		super("record_table");
	}
	public void addHeaderRow(String content)
	{
		Row row = new Row();
		row.setClass("header");
		addRow(row);
		row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(content)), 2);
		
	}
	
	public void addRow(String header, IView content) {
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(new HtmlView(StringEscapeUtils.escapeHtml(header)));
		row.addCell(content);		
	}
	public void addRow(String header, String content)
	{
		content = StringEscapeUtils.escapeHtml(content);
		content = Utils.nullStrFilter(content);
		addRow(header, new HtmlView(content));
	}
	public void addRow(String header, Double value)
	{
		String str = null;
		if(value != null) {
			str = value.toString();
		}
		str = Utils.nullStrFilter(str);
		addRow(header, new HtmlView(str));
	}
	public void addRow(String header, Boolean b)
	{
		if(b) {
			addRow(header, "True");		
		} else {
			addRow(header, "False");
		}
	}
}
