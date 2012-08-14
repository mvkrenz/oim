package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;
import com.divrep.DivRepEventListener;
import com.divrep.common.DivRepButton;
import com.divrep.common.DivRepCheckBox;
import com.divrep.common.DivRepFormElement;
import com.divrep.common.DivRepTextBox;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.FieldOfScienceModel;
import edu.iu.grid.oim.model.db.VOFieldOfScienceModel;
import edu.iu.grid.oim.model.db.record.FieldOfScienceRecord;

public class FieldOfScience extends DivRepFormElement
{
    static Logger log = Logger.getLogger(FieldOfScience.class); 
    
	DivRepButton add_fs;
	DivRepTextBox new_fs; 
	private HashMap<Integer, DivRepCheckBox> field_of_science;
	public HashMap<Integer, DivRepCheckBox> getSciences() {
		return field_of_science;
	}
	private UserContext context;
	
	public FieldOfScience(DivRep _parent, final UserContext context, final ArrayList<Integer> selected) throws SQLException {
		super(_parent);
		this.context = context;
		
		populateList(selected);
		
		new_fs = new DivRepTextBox(this);
		new_fs.setLabel("Or, you can add a new field of science");
		new_fs.addClass("inline-block");
		new_fs.setWidth(230);
		
		add_fs = new DivRepButton(this, "Add");
		add_fs.setStyle(DivRepButton.Style.BUTTON);
		add_fs.addClass("inline-block");
		add_fs.addClass("btn");
		add_fs.addClass("margin-bottom9");
		add_fs.addEventListener(new DivRepEventListener() {
			public void handleEvent(DivRepEvent e) {
				String name = new_fs.getValue();
				if(name == null || name.trim().length() == 0) {
					alert("Please enter field of science to add");
					return;
				}
				name = name.trim();
				for(DivRepCheckBox elem : field_of_science.values()) {
					if(name.equals(elem.getLabel())) {
						alert("'" + name + "' already exists in the list");
						return;
					}
				}

				try {
					//add new field of science						
					FieldOfScienceModel fsmodel = new FieldOfScienceModel(context);	
					FieldOfScienceRecord newrec = new FieldOfScienceRecord();
					newrec.name = name;
					fsmodel.insert(newrec);

					//repopulate the list
					populateList(selected);
					FieldOfScience.this.redraw();
					
					//select newly created fs
					DivRepCheckBox elem = findFieldOfScience(name);
					elem.setValue(true);
					
					new_fs.setValue(null);
				} catch (SQLException e1) {
					log.error(e1);
				}
			}}
		);
	}
	private void populateList(ArrayList<Integer> selected) throws SQLException
	{
		FieldOfScienceModel fsmodel = new FieldOfScienceModel(context);
		field_of_science = new HashMap();
		for(FieldOfScienceRecord fsrec : fsmodel.getAll()) {
			DivRepCheckBox elem = new DivRepCheckBox(this);
			field_of_science.put(fsrec.id, elem);
			elem.setLabel(fsrec.name);
		}
		
		if(selected != null) {
			/*
			//select currently selected field of science
			VOFieldOfScienceModel vofsmodel = new VOFieldOfScienceModel(context);
			for(VOFieldOfScienceRecord fsrec : vofsmodel.getByVOID(rec.id)) {
				DivRepCheckBox check = field_of_science.get(fsrec.field_of_science_id);
				check.setValue(true);
			}
			*/
			for(Integer fid : selected) {
				DivRepCheckBox check = field_of_science.get(fid);
				check.setValue(true);
			}
		}
	}
	
	private DivRepCheckBox findFieldOfScience(String name)
	{
		for(DivRepCheckBox elem : field_of_science.values()) {
			if(elem.getLabel().equals(name)) {
				return elem;
			}
		}
		return null;
	}
	
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void render(PrintWriter out) {
		out.write("<div id=\""+getNodeID()+"\">");
		
		out.write("<h3>Field of Science</h3>");

		out.write("<p>Select Field Of Science(s) applicable to this VO</p>");
		if(isRequired()) {
			//out.print(" * Required");
			out.print("* Required");
		}
		
		out.write("<table class=\"layout\"><tr><td width=\"33%\">");
		//sort the field_of_science by name and render
		TreeSet<DivRepCheckBox> sorted = new TreeSet<DivRepCheckBox>(new Comparator<DivRepCheckBox>() {
			public int compare(DivRepCheckBox o1, DivRepCheckBox o2) {
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		int items_per_column = field_of_science.size() / 3 + 1; //+1 is for rounding
		sorted.addAll(field_of_science.values());
		int count = 0;
		for(DivRepCheckBox elem : sorted) {
			elem.render(out);
			++count;
			if(count != 0 && count % items_per_column == 0) {
				out.write("</td><td width=\"33%\">");
			}
		}
		out.write("</td></tr></table>");

		
		new_fs.render(out);
		add_fs.render(out);

		error.render(out);
		
		out.write("</div>");
	}	
	
	@Override
	public boolean validate()
	{
		if(isRequired()) {
			//make sure at least one element is selected
			for(DivRepCheckBox check : field_of_science.values()) {
				if(check.getValue() == true) {
					return true;
				}
			}
			error.set("Please select at least one field of science.");
			return false;
		}
		return true;
	}
	
}