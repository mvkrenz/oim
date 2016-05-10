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
		return addHeaderRow(new HtmlView(content));
	}
	public Row addHeaderRow(IView content)
	{
		Row row = new Row();
		row.setClass("header");
		addRow(row);
		row.addCell(content, 2);
		return row;
	}
	
	public Row addRow(String header, String content) {
		if(content == null) {
			content = Utils.nullStrFilter(null);
		} else {
			content = StringEscapeUtils.escapeHtml(content);
		}
		return addRow(new HtmlView(header), new HtmlView(content));
	}
	public Row addRow(String header, IView content) {
		return addRow(new HtmlView(header), content);
	}
	public Row addRow(IView header, String content)
	{
		if(content == null) {
			content = Utils.nullStrFilter(null);
		} else {
			content = StringEscapeUtils.escapeHtml(content);
		}
		return addRow(header, new HtmlView(content));
	}

	public Row addBoldRow(String header) {
		return addBoldRow(new HtmlView(header));
	}
	public Row addBoldRow(IView header) {
		Row row = new Row();
		addRow(row);
		row.addBoldCell(header);
		return row;
	}

	public Row addRow(String content) {
		return addRow(new HtmlView(content));
	}
	public Row addRow(IView content) {
		Row row = new Row();
		addRow(row);
		row.addCell(content);
		return row;
	}
	
	public Row addRow(String header, Double value) {
		return addRow(new HtmlView(header), value);
	}
	public Row addRow(IView header, Double value)
	{
		String str;
		if(value == null) {
			str = Utils.nullStrFilter(null);
		} else {
			str = value.toString();
		}
		return addRow(header, new HtmlView(str));
	}
	
	public Row addRow(String header, Boolean b) {
		return addRow(new HtmlView(header), b);
	}
	public Row addRow(IView header, Boolean b)
	{
		String str;
		if(b == null) {
			str = Utils.nullStrFilter(null);
		} else if(b) {
			str = "True";		
		} else {
			str = "False";
		}
		return addRow(header, str);
	}

}
