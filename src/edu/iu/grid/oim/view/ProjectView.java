package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.model.db.record.ProjectRecord;

public class ProjectView implements IView {

	private ArrayList<ProjectRecord> projects;
	public ProjectView(ArrayList<ProjectRecord> projects) {
		this.projects = projects;
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<h2>Projects</h2>");
		if(projects.size() == 0) {
			out.write("<p class=\"muted\">No projects registered</p>");
		} else {
			out.write("<table class=\"table\">");
			
			out.write("<thead><tr><th>Name</th><th>Description</th></tr></thead>");
			for(ProjectRecord project : projects) {
				String url = "projectedit?id="+project.id;
				out.write("<tr onclick=\"document.location='"+url+"';\">");
				out.write("<td>"+StringEscapeUtils.escapeHtml(project.name)+"</td>");
				out.write("<td>"+StringEscapeUtils.escapeHtml(project.desc)+"</td>");
				out.write("</tr>");
			}
			out.write("</table>");
		}
	}

}
