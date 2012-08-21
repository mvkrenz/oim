package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.iu.grid.oim.model.db.record.LogRecord;

public class LogView implements IView {

	private ArrayList<LogRecord> logs;
	public LogView(ArrayList<LogRecord> logs) {
		this.logs = logs;
	}
	@Override
	public void render(PrintWriter out) {
		out.write("<h2>Update Log</h2>");
		if(logs.size() == 0) {
			out.write("<p class=\"muted\">No logs aviable</p>");
		} else {
			out.write("<table class=\"table\">");
			
			out.write("<thead><tr><th>Date</th><th>Comment</th></tr></thead>");
			boolean first = true;
			for(LogRecord log : logs) {
				String url = "log?type=3&id="+log.id;
				if(first) {
					out.write("<tr class=\"latest\" onclick=\"window.open('"+url+"', '_newtab');\">");
					first = false;
				} else {
					out.write("<tr onclick=\"document.location='"+url+"';\">");
				}
				String time = new SimpleDateFormat("MM/dd/yyyy").format(log.timestamp);
				out.write("<td>"+time+"</td>");
				
				String comment = "";

				comment += log.type+" / ";
				comment += log.model.substring(25, log.model.length()-5);	
				if(log.comment != null) {
					comment += "<br><span class=\"muted\">"+log.comment+"</span>";
				}
				out.write("<td>"+comment+"</td>");
				out.write("</tr>");
			}
			out.write("</table>");
		}
	}

}
