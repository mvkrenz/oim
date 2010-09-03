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
		TimeZone timezone;
		try {
			timezone = TimeZone.getTimeZone(auth.getContact().timezone);
			dformat.setTimeZone(timezone);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		timestamp = rec.confirmed;
		
		update = new DivRepButton(this, "Update");
		update.setStyle(Style.BUTTON);
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
	
		if(rec.isConfirmationExpired()) {
			out.write("<p class=\"divrep_round divrep_elementerror\">");
		} else {
			out.write("<p>");
		}
		
		out.write("The date when the information on this page was last reviewed and confirmed<br/>");
		out.write("<input style=\"width: 200px\" type=\"text\" value=\""+dformat.format(timestamp)+"\" disabled > ");//<b>"+dformat.format(timestamp) + " " + "</b></p>");
		update.render(out);
		out.print("</div>");
	}	
}