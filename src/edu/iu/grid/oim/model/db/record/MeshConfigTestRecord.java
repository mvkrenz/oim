package edu.iu.grid.oim.model.db.record;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MeshConfigTestRecord extends RecordBase {

	@Key public Integer id;
	
	public Integer mesh_config_id;
	public String name;
	public Boolean disable;
	
	public Integer service_id; //owamp, bwctl, etc.
	public String type;  //enum - MESH/DISJOINT/STAR
	
	//use get/setGroupXIds() instead of accessing directly
	protected  String groupa_ids; //comma delimited list of host groups
	protected String groupb_ids; //comma delimited list of host groups
	public Integer param_id;
	
	//load from existing record
	public MeshConfigTestRecord(ResultSet rs) throws SQLException { super(rs); }
	//for creating new record
	public MeshConfigTestRecord() {}
	
	public ArrayList<Integer> getGroupAIds() {
		return getGroupIds(groupa_ids);
	}
	public ArrayList<Integer> getGroupBIds() {
		return getGroupIds(groupb_ids);
	}
	public ArrayList<Integer> getGroupIds(String _ids) {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		if(_ids != null) {
			for(String id : _ids.split(",")) {
				ids.add(Integer.parseInt(id));
			}
		}
		return ids;
	}
	
	public void setGroupAIds(ArrayList<Integer> _ids) {
		groupa_ids = convertToStringIds(_ids);
	}
	public void setGroupBIds(ArrayList<Integer> _ids) {
		groupb_ids = convertToStringIds(_ids);
	}
	public String convertToStringIds(ArrayList<Integer> _ids) {
		StringBuffer buf = new StringBuffer();
		for(Integer id : _ids) {
			buf.append(id.toString());
			buf.append(",");
		}
		return buf.toString();
	}
}
