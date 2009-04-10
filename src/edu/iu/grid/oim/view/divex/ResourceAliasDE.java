package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.webif.divex.ButtonDE;
import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.EventListener;
import com.webif.divex.form.FormElementDEBase;
import com.webif.divex.form.TextFormElementDE;

import edu.iu.grid.oim.view.divex.ContactEditorDE.ContactDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;

public class ResourceAliasDE extends FormElementDEBase {

	ArrayList<AliasEditor> aliases = new ArrayList<AliasEditor>();

	class AliasEditor extends FormElementDEBase
	{
		private TextFormElementDE text;
		private ButtonDE remove_button;
		private AliasEditor myself;
		
		protected AliasEditor(DivEx parent) {
			super(parent);
			myself = this;
			
			text = new TextFormElementDE(this);
			
			remove_button = new ButtonDE(this, "images/delete.png");
			remove_button.setStyle(ButtonDE.Style.IMAGE);
			remove_button.addEventListener(new EventListener() {
				public void handleEvent(Event e) {
					removeAlias(myself);	
				}
			});
		}

		public void setValue(String value) {
			text.setValue(value);
		}
		
		@Override
		public void validate() {
			// TODO Auto-generated method stub
			
		}

		@Override
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			text.render(out);
			remove_button.render(out);
		}
		
	}
	
	public void removeAlias(AliasEditor alias)
	{
		aliases.remove(alias);
		redraw();
	}
	
	public void addAlias(String alias) { 
		AliasEditor elem = new AliasEditor(this);
		elem.setValue(alias);
		aliases.add(elem); 
	}
	
	public ResourceAliasDE(DivEx parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void validate() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(AliasEditor alias : aliases) {
			alias.render(out);
		}
		out.print("</div>");

	}

}
