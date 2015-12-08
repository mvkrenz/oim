package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.Date;
import java.util.ArrayList;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepDate;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextArea;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.db.record.ProjectPublicationRecord;

public class ProjectPublicationEditor extends DivRepFormElement {
	private DivRepButton add_button;

	class PublicationEditor extends DivRepFormElement
	{
		private DivRepDate date;
		private DivRepTextBox name;
		private DivRepTextArea desc;
		private DivRepButton remove_button;
		private PublicationEditor myself;
		
		protected PublicationEditor(DivRep parent, ProjectPublicationRecord rec) {
			super(parent);
			myself = this;
			
			date = new DivRepDate(this);
			date.setLabel("Publication Date");
			date.setRequired(true);
			if(rec.date != null) {
				date.setValue(rec.date);
			}

			name = new DivRepTextBox(this);
			name.setLabel("Publication Name");
			name.setRequired(true);
			name.setValue(rec.name);
			
			desc = new DivRepTextArea(this);
			desc.setLabel("Publication Detail");
			desc.setRequired(true);
			desc.setValue(rec.desc);

			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removePublication(myself);	
				}
			});
		}

		public ProjectPublicationRecord getRecord() {
			ProjectPublicationRecord rec = new ProjectPublicationRecord();
			rec.date = new Date(date.getValue().getTime());
			rec.name = name.getValue();
			rec.desc = desc.getValue();
			return rec;
		}

		protected void onEvent(DivRepEvent e) {
			// TODO Auto-generated method stub
		}
		
		public void render(PrintWriter out) {
			out.write("<div id=\""+getNodeID()+"\" class=\"well well-small\">");
			
			out.write("<span class=\"right\">");
			remove_button.render(out);
			out.write("</span>");

			for(DivRep child : childnodes) {
				if(child == remove_button) continue;
				
				if(child instanceof DivRepFormElement) {
					DivRepFormElement elem = (DivRepFormElement)child;
					if(!elem.isHidden()) {
						out.print("<div class=\"divrep_form_element\">");
						child.render(out);
						out.print("</div>");
					}
				} else {
					//non form element..
					child.render(out);
				}
			}
			
			out.write("</div>");
		}
	}
	
	public void removePublication(PublicationEditor owner)
	{
		remove(owner);
		modified(true);
		redraw();
	}
	
	public void addPublication(ProjectPublicationRecord rec) { 
		PublicationEditor owner = new PublicationEditor(this, rec);
		redraw();
		//don't set modifed(true) here - addOwner is called by init and doing so will make form to popup confirmation
		//everytime user opens it
	}
	
	public ProjectPublicationEditor(DivRep parent) {
		super(parent);

		add_button = new DivRepButton(this, "Add New Publication");
		add_button.addClass("btn");
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addPublication(new ProjectPublicationRecord());
				modified(true);
			}
		});	
	}
	
	//Note: caller need to set the resource_id for each records
	public ArrayList<ProjectPublicationRecord> getPublications()
	{
		ArrayList<ProjectPublicationRecord> records = new ArrayList<ProjectPublicationRecord>();
		for(DivRep node : childnodes) {
			if(node instanceof PublicationEditor) {
				PublicationEditor publication = (PublicationEditor)node;
				ProjectPublicationRecord rec = publication.getRecord();
				if(rec != null)  {
					records.add(rec);
				}
			}
		}
		return records;
	}

	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
	}
	
	/*
	public boolean validate()
	{
		error.redraw();
		boolean valid = true;

		for(DivRep node : childnodes) {					
			if(node instanceof ProjectPublicationEditor) {
				ProjectPublicationEditor publication = (ProjectPublicationEditor)node;
				if(!publication.validate()) {
					error.set(null);//child element should show error message
					valid = false;
				} else {
					//TODO - do any aggregate validations?
				}
			}
		}
		

		
		setValid(valid);
		return valid;
	}
	*/
	
	public void render(PrintWriter out) {
		out.print("<div id=\""+getNodeID()+"\">");
		out.print("<h3>Publications</h3>");
		error.render(out);
		for(DivRep node : childnodes) {
			if(node instanceof PublicationEditor) {
				node.render(out);
			}
		}
		add_button.render(out);
		out.print("</div>");
	}

}
