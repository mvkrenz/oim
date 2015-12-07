package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepSelectBox;
import com.divrep.common.DivRepTextBox;

public class CampusGridSubmitNodes extends DivRepFormElement {
	private DivRepButton add_button;

	class HostEditor extends DivRepFormElement<String>
	{
		private DivRepTextBox fqdn;
		private DivRepButton remove_button;
		private HostEditor myself;
		
		protected HostEditor(DivRep parent) {
			super(parent);
			myself = this;
			
			fqdn = new DivRepTextBox(this);
			fqdn.addClass("divrep_inline");
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			//remove_button.addClass("pull-right");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeNode(myself);	
				}
			});
		}

		public void setValue(String f) {
			fqdn.setValue(f);
		}
		public String getValue() {
			return fqdn.getValue();
		}
		@Override 
		public boolean validate()
		{
			return fqdn.validate();
		}
		
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\">");
			fqdn.render(out);
			remove_button.render(out);
			out.write("</div>");
		}
		
	}
	
	public void removeNode(HostEditor host)
	{
		remove(host);
		redraw();
	}
	
	public void addNode(String fqdn) { 
		HostEditor elem = new HostEditor(this);
		elem.setValue(fqdn);
		redraw();
	}
	
	public CampusGridSubmitNodes(DivRep parent) {
		super(parent);
		
		add_button = new DivRepButton(this, "Add FQDN");
		//add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addClass("btn");
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addNode(null); //TODO - should I pick a valid entity?
			}
			
		});
	}

	//Note: caller need to set the resource_id for each records
	public ArrayList<String> getSubmithosts()
	{
		ArrayList<String> records = new ArrayList<String>();
		for(DivRep node : childnodes) {
			if(node instanceof HostEditor) {
				HostEditor host = (HostEditor)node;
				if(host.getValue() != null) {
					records.add(host.getValue());
				}
			}
		}
		return records;
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub

	}

	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\" class=\"well\">");
		int c = 0;
		for(DivRep node : childnodes) {
			if(node instanceof HostEditor) {
				node.render(out);
				c++;
			}
		}
		if(c == 0) {
			//out.write("<p class=\"muted\">None Selected</p>");
		}

		add_button.render(out);
		
		out.print("</div>");
	}

}
