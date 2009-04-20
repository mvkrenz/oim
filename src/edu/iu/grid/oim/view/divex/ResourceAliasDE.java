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

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.view.divex.ContactEditorDE.ContactDE;
import edu.iu.grid.oim.view.divex.ContactEditorDE.Rank;
import edu.iu.grid.oim.view.divex.ResourceDowntimesDE.DowntimeEditor;

public class ResourceAliasDE extends FormElementDEBase {

	//ArrayList<AliasEditor> aliases = new ArrayList<AliasEditor>();
	private ButtonDE add_button;

	class AliasEditor extends FormElementDEBase
	{
		private TextFormElementDE text;
		private ButtonDE remove_button;
		private AliasEditor myself;
		
		protected AliasEditor(DivEx parent) {
			super(parent);
			myself = this;
			
			text = new TextFormElementDE(this);
			text.addClass("inline");
			
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
		public String getValue() {
			return text.getValue();
		}


		@Override
		protected void onEvent(Event e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"resource_alias\">");
			text.render(out);
			remove_button.render(out);
			out.write("</div>");
		}
		
	}
	
	public void removeAlias(AliasEditor alias)
	{
		remove(alias);
		redraw();
	}
	
	public void addAlias(String alias) { 
		AliasEditor elem = new AliasEditor(this);
		elem.setValue(alias);
		//aliases.add(elem);
		redraw();
	}
	
	public ResourceAliasDE(DivEx parent) {
		super(parent);
		add_button = new ButtonDE(this, "Add New Alias");
		add_button.setStyle(ButtonDE.Style.ALINK);
		add_button.addEventListener(new EventListener() {
			public void handleEvent(Event e) {
				addAlias("");
			}
			
		});
	}

	//Note: caller need to set the resource_id for each records
	public ArrayList<String> getAliases()
	{
		ArrayList<String> records = new ArrayList<String>();
		for(DivEx node : childnodes) {
			if(node instanceof AliasEditor) {
				AliasEditor alias = (AliasEditor)node;
				String str = alias.getValue();
				if(str.length() > 0) {
					records.add(str);
				}
			}
		}
		return records;
	}

	protected void onEvent(Event e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(DivEx node : childnodes) {
			if(node instanceof AliasEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
