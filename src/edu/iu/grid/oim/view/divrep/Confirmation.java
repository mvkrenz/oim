package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepButton.Style;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.record.ConfirmableRecord;

public class Confirmation extends DivRepFormElement
{
	private Timestamp timestamp;
	private DivRepButton update;
	private DateFormat dformat;
	ConfirmableRecord rec;
	
	public Timestamp getTimestamp()
	{
		return timestamp;
	}
	
	public Confirmation(DivRep parent, ConfirmableRecord _rec, Authorization auth) {
		super(parent);
		rec = _rec.clone();//I need to clone so that user updating the form won't update the cache
		
		dformat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
		TimeZone timezone = auth.getTimeZone();
		timestamp = rec.confirmed;
		
		update = new DivRepButton(this, "Update Confirmation Date");
		update.setStyle(Style.BUTTON);
		update.addClass("pull-top");
		update.addClass("btn");
		update.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				Calendar cal = Calendar.getInstance();
				timestamp.setTime(cal.getTimeInMillis());
				Confirmation.this.setFormModified();
				redraw();
			}});
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) {
		out.print("<div class=\"divrep_form_element\" id=\""+getNodeID()+"\">");	
		
		out.write("<p>The date when the information on this page was last reviewed</p>");
		out.write("<input style=\"width: 200px\" type=\"text\" value=\""+dformat.format(timestamp)+"\" disabled > ");//<b>"+dformat.format(timestamp) + " " + "</b></p>");
		update.render(out);
		
		if(rec.isConfirmationExpired()) {
			out.write("<p class=\"divrep_round divrep_elementerror\">Date expired. Please review the information and update the confirmation date.</p>");
		}
		
		out.print("</div>");
	}	
}