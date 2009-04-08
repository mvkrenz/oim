package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.ContactRankModel;
import edu.iu.grid.oim.model.db.ContactTypeModel;
import edu.iu.grid.oim.model.db.SCModel;
import edu.iu.grid.oim.model.db.VOModel;

public class SCContactRecord extends RecordBase {
	@Key public Integer contact_id;
	@Key public Integer sc_id;
	@Key public Integer contact_type_id;
	@Key public Integer contact_rank_id;
	
	//load from existing record
	public SCContactRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public SCContactRecord() {}
	
	public String getTitle() {
		return "SC Contact Record";
	}
	
	public ArrayList<String> getLables() {
		ArrayList<String> labels = new ArrayList();
		labels.add("sc_contact");
		labels.add("sc_"+sc_id.toString());
		return labels;
	}
	public String toString(Object field, Authorization auth)
	{
		if(field == null) return null;
		try {
			if(field == contact_id) {
				ContactModel model = new ContactModel(auth);
				ContactRecord rec = model.get(contact_id);
				return rec.name;
			} else if(field == sc_id) {
				SCModel model = new SCModel(auth);
				SCRecord rec = model.get(sc_id);
				return rec.name;
			} else if(field == contact_type_id) {
				ContactTypeModel model = new ContactTypeModel(auth);
				ContactTypeRecord rec = model.get(contact_type_id);
				return rec.name;
			} else if(field == contact_rank_id) {
				ContactRankModel model = new ContactRankModel(auth);
				ContactRankRecord rec = model.get(contact_rank_id);
				return rec.name;				
			}
		} catch(SQLException e) {
			//forget it then..
		}
		return field.toString();
	}
}
