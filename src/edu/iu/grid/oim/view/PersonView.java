package edu.iu.grid.oim.view;

import java.io.PrintWriter;

import org.apache.commons.lang.StringEscapeUtils;

import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.db.record.ContactRecord;

public class PersonView implements IView {
	private ContactRecord person;
	private ContactRank rank;
	
	public PersonView(ContactRecord person, ContactRank rank) {
		this.person = person;
		this.rank = rank;
	}
	
	public String render() {
		StringBuffer out = new StringBuffer();
		out.append("<div class='contact_rank contact_"+rank+"'>");
		out.append(StringEscapeUtils.escapeHtml(person.name.trim()));
		if(person.disable) {
			out.append(" <span class=\"label label-important\">Disabled</span>");
		}
		out.append("</div>");	
		return out.toString();
	}

	@Override
	public void render(PrintWriter out) {
		out.write(render());
	}
}
