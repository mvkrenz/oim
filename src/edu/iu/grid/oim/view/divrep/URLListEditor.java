package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;

public class URLListEditor extends DivRepFormElement {

	private DivRepButton add_button;
	private String url_samle = "ce.grid.iu.edu";
	
	public void setSampleURL(String sample) {
		this.url_samle = sample;
	}
	
	class URLEditor extends DivRepFormElement
	{
		private DivRepTextBox url;
		private DivRepButton remove_button;
		private URLEditor myself;
		
		
		protected URLEditor(DivRep parent) {
			super(parent);
			myself = this;
			
			url = new DivRepTextBox(this);
			url.setSampleValue(url_samle);
			url.addClass("divrep_inline");
			
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeUrl(myself);	
				}
			});
		}

		public void setValue(String value) {
			url.setValue(value);
		}
		public String getValue() {
			return url.getValue();
		}


		@Override
		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"resource_alias\">");
			url.render(out);
			remove_button.render(out);
			out.write("</div>");
		}
		
	}
	
	public void removeUrl(URLEditor url)
	{
		remove(url);
		redraw();
	}
	
	public void addUrl(String url) { 
		URLEditor elem = new URLEditor(this);
		elem.setValue(url);
		redraw();
	}
	
	public URLListEditor(DivRep parent) {
		super(parent);
		add_button = new DivRepButton(this, "Add New URL");
		//add_button.setStyle(DivRepButton.Style.ALINK);
		add_button.addClass("btn");
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addUrl("");
			}
			
		});
	}

	//Note: caller need to set the resource_id for each records
	public ArrayList<String> getURLs()
	{
		ArrayList<String> records = new ArrayList<String>();
		for(DivRep node : childnodes) {
			if(node instanceof URLEditor) {
				URLEditor url = (URLEditor)node;
				String str = url.getValue();
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
		out.print("<div id=\""+getNodeID()+"\" class=\"well\">");
		for(DivRep node : childnodes) {
			if(node instanceof URLEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
