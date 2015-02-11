package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepButton.Style;
import com.divrep.common.DivRepTextArea;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ConfigModel;
import edu.iu.grid.oim.model.db.ConfigModel.Config;
import edu.iu.grid.oim.model.db.record.ConfirmableRecord;

public class EditableContent extends DivRepFormElement
{
    static Logger log = Logger.getLogger(EditableContent.class);  
    
	private DivRepButton edit;
	private DivRepTextArea html;
	ConfirmableRecord rec;
	private UserContext context;
	private boolean editing = false;
	
	public EditableContent(DivRep parent, UserContext context, final Config content) {
		super(parent);
		this.context = context;
		
		final ConfigModel config = new ConfigModel(context);
		html = new DivRepTextArea(this);
		html.setValue(content.getString());
		//html.setWidth(800); //sometimes overflows
		html.setHeight(200);
		html.addClass("content-editor");
		
		edit = new DivRepButton(this, "Update Content");
		edit.setStyle(Style.BUTTON);
		edit.addClass("pull-right");
		edit.addClass("btn");
		edit.addClass("btn-mini");
		edit.addEventListener(new DivRepEventListener() {
			@Override
			public void handleEvent(DivRepEvent arg0) {
				editing = !editing;
				if(editing) {
					edit.setTitle("Save");
				} else {
					try {
						content.set(html.getValue());
					} catch (SQLException e) {
						log.error("Failed to store html content for key "+content.getKey());
					}
					
					edit.setTitle("Update Content");
				}
				EditableContent.this.redraw();
			}});
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");	
		edit.render(out);
		if(editing) {
			html.render(out);
		} else {
			out.print(html.getValue());
		}
		out.print("</div>");
	}	
}