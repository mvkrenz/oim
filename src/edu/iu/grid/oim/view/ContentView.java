package edu.iu.grid.oim.view;

import java.io.PrintWriter;
import java.util.ArrayList;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.UserContext.Message;

public class ContentView extends GenericView {
	IView bread_crumb;
	UserContext context;

	public ContentView(UserContext context) {
		this.context = context;
	}
	
	public void setBreadCrumb(IView _crumb)
	{
		bread_crumb = _crumb;
	}
	
	public void render(PrintWriter out)
	{
		out.println("<div id=\"content\">");
		
		renderMessages(out);
		if(bread_crumb != null) {
			bread_crumb.render(out);
		}
		
		super.render(out);
		
		out.println("</div>");
	}
	
	public void renderMessages(PrintWriter out) {
		ArrayList<Message> messages = context.flushMessages();
		if(messages != null) {
			for(Message message : messages) {
				switch(message.type) {
				case WARNING:
					out.write("<div class=\"alert alert-warning\">");
					break;
				case INFO:
					out.write("<div class=\"alert alert-info\">");
					break;
				case SUCCESS:
					out.write("<div class=\"alert alert-success\">");
					break;
				case ERROR:
					out.write("<div class=\"alert alert-error\">");
					break;
				}
				out.write("<a class=\"close\" data-dismiss=\"alert\" href=\"#\">&times;</a>");
				//out.write("<h4 class=\"alert-heading\">Warning!</h4>");
				out.write(StringEscapeUtils.escapeHtml(message.text));
				out.write("</div>");
			}
		}
	}

}
