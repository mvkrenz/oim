package edu.iu.grid.oim.view;

import edu.iu.grid.oim.view.TableView;

public class RecordTableView extends TableView {
	
	public RecordTableView()
	{
		setClass("record_table");
	}
	
	public void addRow(String header, String value)
	{
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(header);
		row.addCell(value);
	}
	
	public void addRow(String header, Boolean value)
	{
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(header);
		row.addCell(value);
	}
	public void addHtmlRow(String header, String html)
	{
		Row row = new Row();
		addRow(row);
		row.addHeaderCell(header);
		row.addHtmlCell(html);
	}
}
