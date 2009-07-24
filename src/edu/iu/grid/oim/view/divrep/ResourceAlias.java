package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ResourceAliasRecord;
import edu.iu.grid.oim.view.divrep.ContactEditor.ContactDE;
import edu.iu.grid.oim.view.divrep.ContactEditor.Rank;

public class ResourceAlias extends DivRepFormElement {

	//ArrayList<AliasEditor> aliases = new ArrayList<AliasEditor>();
	private DivRepButton add_button;

	class AliasEditor extends DivRepFormElement
	{
		private DivRepTextBox text;
		private DivRepButton remove_button;
		private AliasEditor myself;
		
		protected AliasEditor(DivRep parent) {
			super(parent);
			myself = this;
			
			text = new DivRepTextBox(this);
			text.addClass("divrep_inline");
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
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
		protected void onEvent(DivRepEvent e) {
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
		redraw();
	}
	
	public ResourceAlias(DivRep parent) {
		super(parent);
		add_button = new DivRepButton(this, "Add New Alias");
		add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addAlias("");
			}
			
		});
	}

	//Note: caller need to set the resource_id for each records
	public ArrayList<String> getAliases()
	{
		ArrayList<String> records = new ArrayList<String>();
		for(DivRep node : childnodes) {
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

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		for(DivRep node : childnodes) {
			if(node instanceof AliasEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
