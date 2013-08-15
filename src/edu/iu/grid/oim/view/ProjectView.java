package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;

public class ProjectView implements IView {

	private ArrayList<ProjectRecord> projects;
	private ContactModel contactmodel;
	
	public ProjectView(ArrayList<ProjectRecord> projects, ContactModel contactmodel) {
		this.projects = projects;
		this.contactmodel = contactmodel;
	}
	
	@Override
	public void render(PrintWriter out) {
		out.write("<h2>Projects</h2>");
		if(projects.size() == 0) {
			out.write("<p class=\"muted\">No projects registered</p>");
		} else {
			out.write("<table class=\"table table-hover\">");
			
			out.write("<thead><tr><th>Name</th><th>PI</th></tr></thead>");
			for(ProjectRecord project : projects) {
				String url = "projectedit?id="+project.id;
				out.write("<tr onclick=\"document.location='"+url+"';\">");
				out.write("<td>"+StringEscapeUtils.escapeHtml(project.name)+"</td>");

				/*
				//load field of science
				try {
					FieldOfScienceRecord fos = fosmodel.get(project.fos_id);
					out.write("<td>"+StringEscapeUtils.escapeHtml(fos.name)+"</td>");
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				
				//load pi
				try {
					ContactRecord pi = contactmodel.get(project.pi_contact_id);
					out.write("<td>"+StringEscapeUtils.escapeHtml(pi.name)+"</td>");
				} catch (SQLException e) {
					out.write("(failed to load contact)");
				}				
				
				//out.write("<td>"+StringEscapeUtils.escapeHtml(project.desc)+"</td>");
				out.write("</tr>");
			}
			out.write("</table>");
			//out.write("<script>$('.tooltip2').tooltip();</script>");
		}
	}

}
