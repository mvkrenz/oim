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
	public Row addHeaderRow(String content)
	{
		Row row = new Row();
		row.setClass("header");
		addRow(row);
		row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(content)), 2);
		return row;
	}
	
	public Row addRow(String header, IView content) {
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(new HtmlView(StringEscapeUtils.escapeHtml(header)));
		row.addCell(content);	
		return row;
	}
	
	public Row addBoldRow(String content) {
		Row row = new Row();
		addRow(row);
		row.addBoldCell(new HtmlView(StringEscapeUtils.escapeHtml(content)));
		return row;
	}

	public Row addRow(String content) {
		Row row = new Row();
		addRow(row);
		row.addCell(new HtmlView(StringEscapeUtils.escapeHtml(content)));
		return row;
	}

	public Row addRow(String header, String content)
	{
		content = StringEscapeUtils.escapeHtml(content);
		content = Utils.nullStrFilter(content);
		return addRow(header, new HtmlView(content));
	}
	public Row addRow(String header, Double value)
	{
		String str = null;
		if(value != null) {
			str = value.toString();
		}
		str = Utils.nullStrFilter(str);
		return addRow(header, new HtmlView(str));
	}
	public Row addRow(String header, Boolean b)
	{
		if(b) {
			return addRow(header, "True");		
		} else {
			return addRow(header, "False");
		}
	}
}
