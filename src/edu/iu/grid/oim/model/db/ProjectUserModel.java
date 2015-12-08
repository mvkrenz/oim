package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import edu.iu.grid.oim.model.UserContext;

import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.ProjectUserRecord;
import edu.iu.grid.oim.model.db.record.ProjectRecord;
import edu.iu.grid.oim.model.db.record.RecordBase;

public class ProjectUserModel extends SmallTableModelBase<ProjectUserRecord> {
    static Logger log = Logger.getLogger(ProjectUserModel.class); 

	public ProjectUserModel(UserContext context) {
		super(context, "project_user");
	}
	ProjectUserRecord createRecord() throws SQLException
	{
		return new ProjectUserRecord();
	}
	public Collection<ProjectUserRecord> getAll() throws SQLException
	{
		ArrayList<ProjectUserRecord> list = new ArrayList<ProjectUserRecord>();
		for(RecordBase it : getCache()) {
			list.add((ProjectUserRecord)it);
		}
		return list;
	}
	public Collection<ProjectUserRecord> getAllByProjectId(int id) throws SQLException
	{
		ArrayList<ProjectUserRecord> list = new ArrayList<ProjectUserRecord>();
		for(ProjectUserRecord it : getAll()) {
			if(it.project_id.equals(id)) {
				list.add(it);
			}
		}
		return list;		
	}
	
	public String getName()
    {
    	return "Project / Users";
    }

	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='project_id']/Value", doc, XPathConstants.STRING));
		ResourceModel model = new ResourceModel(context);
		return model.canEdit(id);
	}
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		if(field_name.equals("project_id")) {
			ProjectModel model = new ProjectModel(context);
			ProjectRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		} else if(field_name.equals("contact_id")) {
			ContactModel model = new ContactModel(context);
			ContactRecord rec = model.get(Integer.parseInt(value));
			return value + " (" + rec.name + ")";
		}
		return value;
	}
}

    