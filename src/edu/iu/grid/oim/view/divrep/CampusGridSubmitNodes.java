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

public class CampusGridSubmitNodes extends DivRepFormElement {
	private DivRepButton add_button;
	LinkedHashMap<Integer, String> submitnodes;

	class HostEditor extends DivRepFormElement<Integer>
	{
		private DivRepSelectBox host;
		private DivRepButton remove_button;
		private HostEditor myself;
		
		protected HostEditor(DivRep parent) {
			super(parent);
			myself = this;
			
			host = new DivRepSelectBox(this);
			host.setValues(submitnodes);
			host.addClass("divrep_inline");
			host.setRequired(true);
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addClass("pull-right");
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeNode(myself);	
				}
			});
		}

		public void setValue(Integer id) {
			host.setValue(id);
		}
		public Integer getValue() {
			return host.getValue();
		}
		@Override 
		public boolean validate()
		{
			return host.validate();
		}
		
		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"campusgrid_hosts\">");
			remove_button.render(out);
			host.render(out);
			out.write("</div>");
		}
		
	}
	
	public void removeNode(HostEditor host)
	{
		remove(host);
		redraw();
	}
	
	public void addNode(Integer host_id) { 
		HostEditor elem = new HostEditor(this);
		elem.setValue(host_id);
		redraw();
	}
	
	public CampusGridSubmitNodes(DivRep parent, LinkedHashMap<Integer, String> submitnodes) {
		super(parent);
		this.submitnodes = submitnodes;
		
		add_button = new DivRepButton(this, "Add Submit Node");
		//add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addClass("btn");
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addNode(null); //TODO - should I pick a valid entity?
			}
			
		});
	}

	//Note: caller need to set the resource_id for each records
	public ArrayList<Integer> getSubmithosts()
	{
		ArrayList<Integer> records = new ArrayList<Integer>();
		for(DivRep node : childnodes) {
			if(node instanceof HostEditor) {
				HostEditor host = (HostEditor)node;
				Integer id = host.getValue();
				if(id != null) {
					records.add(id);
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
			out.write("<p class=\"muted\">None Selected</p>");
		}

		add_button.render(out);
		
		out.print("</div>");
	}

}
