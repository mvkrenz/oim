package edu.iu.grid.oim.view;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.view.TableView;

public class RecordTableView extends TableView {
	
	public RecordTableView()
	{
		setClass("record_table");
	}
	
	public void addRow(String header, View content) {
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(new HtmlView(StringEscapeUtils.escapeHtml(header)));
		row.addCell(content);		
	}
	public void addRow(String header, String content)
	{
		addRow(header, new HtmlView(StringEscapeUtils.escapeHtml(content)));
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
