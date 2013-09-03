package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepStaticContent;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.ContactRank;
import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ProjectUserRecord;

public class ProjectUserEditor extends DivRepFormElement {
    static Logger log = Logger.getLogger(ProjectUserEditor.class);  

    UserContext context;
	private DivRepButton add_button;

	class UserEditor extends DivRepFormElement
	{
		private DivRepTextBox submit_uid;
		private ContactEditor contact;
		
		private DivRepButton remove_button;
		private UserEditor myself;
		
		protected UserEditor(DivRep parent, ProjectUserRecord rec, UserContext context) {
			super(parent);
			myself = this;
			
			submit_uid = new DivRepTextBox(this);
			submit_uid.setLabel("Submit Host User ID");
			submit_uid.setRequired(true);
			submit_uid.setValue(rec.submit_uid);
			submit_uid.setSampleValue("hayashis");
			
			ContactModel cmodel = new ContactModel(context);
			contact = new ContactEditor(this, cmodel, false, false);
			contact.setLabel("OIM Contact");
			contact.setShowRank(false);
			contact.setMinContacts(ContactRank.Primary, 1); //required
			if(rec.contact_id != null) {
				try {
					ContactRecord c = cmodel.get(rec.contact_id);
					contact.addSelected(c, ContactRank.Primary);
				} catch (SQLException e) {
					log.error(e);
				}
			}
		
			remove_button = new DivRepButton(this, "images/delete.png");
			remove_button.setStyle(DivRepButton.Style.IMAGE);
			remove_button.addEventListener(new DivRepEventListener() {
				public void handleEvent(DivRepEvent e) {
					removeUser(myself);	
				}
			});
		}

		public ProjectUserRecord getRecord() {
			ProjectUserRecord rec = new ProjectUserRecord();
			rec.submit_uid = submit_uid.getValue();
			ArrayList<ContactRecord> recs = contact.getContactRecordsByRank(ContactRank.Primary);
			if(recs.size() == 1) {
				ContactRecord c = recs.get(0);
				rec.contact_id = c.id;
			} else {
				log.error("faile do to get one and only one contact from contact editor");
			}
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
	
	public void removeUser(UserEditor owner)
	{
		remove(owner);
		modified(true);
		redraw();
	}
	
	public void addUser(ProjectUserRecord rec) { 
		UserEditor editor = new UserEditor(this, rec, context);
		redraw();
		//don't set modifed(true) here - addOwner is called by init and doing so will make form to popup confirmation
		//everytime user opens it
	}
	
	public ProjectUserEditor(DivRep parent, UserContext context) {
		super(parent);
		this.context = context;

		add_button = new DivRepButton(this, "Add New Submit Host User");
		add_button.addClass("btn");
		add_button.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				addUser(new ProjectUserRecord());
				modified(true);
			}
		});	
	}
	
	//Note: caller need to set the resource_id for each records
	public ArrayList<ProjectUserRecord> getUsers()
	{
		ArrayList<ProjectUserRecord> records = new ArrayList<ProjectUserRecord>();
		for(DivRep node : childnodes) {
			if(node instanceof UserEditor) {
				UserEditor user = (UserEditor)node;
				ProjectUserRecord rec = user.getRecord();
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
		out.print("<h3>Submit Host Users</h3>");

		for(DivRep node : childnodes) {
			if(node instanceof UserEditor) {
				node.render(out);
			}
		}
		out.write("<p class=\"help-block\">* If you can't find a contact, please register first at <a href=\"contactedit\" target=\"_blank\">New Contact</a>. You don't have to refersh this page after adding a new contact.</p>");

		error.render(out);
		add_button.render(out);
		out.print("</div>");
	}

}
