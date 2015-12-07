package edu.iu.grid.oim.view.divrep;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;

import com.divrep.DivRep;

public abstract class HostGroupListEditor extends SelectionEditorBase {

	private static final long serialVersionUID = 1L;
	
	public HostGroupListEditor(DivRep parent) {
		super(parent);
	}

	@Override
	abstract protected ItemInfo getDetailByID(Integer id);

	@Override
	protected Collection<ItemInfo> searchByQuery(String query) throws SQLException {
		ItemInfo best_guess = null;
		int best_guess_distance = 10000;
		HashMap<Integer, ItemInfo> recs = new HashMap<Integer, ItemInfo>();
		
		//search for each available records
		for(ItemInfo info : getAvailableRecords()) {
			if(recs.size() > 20) break;
			if(info.name != null) {
				String name = itrim(info.name.toLowerCase());
				if(name.contains(query.toLowerCase())) {
					recs.put(info.id, info);
					continue;
				}
				
				//calculate levenshtein distance per token
				for(String token : info.name.split(" ")) {
					int distance = StringUtils.getLevenshteinDistance(token, query);
					if(best_guess_distance > distance) {
						best_guess = info;
						best_guess_distance = distance;
					}
				}
			}
			if(info.detail != null) {
				String name = info.detail.toLowerCase();
				if(name.contains(query.toLowerCase())) {
					recs.put(info.id, info);
					continue;
				}
			}
		}
		
		//if no match was found, pick the closest match
		if(recs.size() == 0 && best_guess != null) {
			recs.put(best_guess.id, best_guess);	
		}

		//remove items that are already selected 
		for(ItemDE sel : selected) {
			recs.remove(sel.info.id);
		}
		
		/*
		//finally, construct json
		String out = "[";
		boolean first = true;
		for(ItemInfo info: recs.values()) {
			if(first) {
				first = false;
			} else {
				out += ",";
			}
			String detail = info.detail;
			if(detail == null) {
				detail = info.rec.fqdn;
			}
			out += "{\"id\":"+info.rec.id+", \"name\":\""+itrim(info.rec.name)+"\", \"email\":\""+detail+"\"}\n";
		}
		out += "]";
		*/
		return recs.values();
	}
	
	//takes comma delimited ids
	public void addSelected(ArrayList<Integer> _ids) {
		if(_ids == null) return;
		
		//String[] ids = _ids.split(",");
		for(Integer id : _ids) {
			ItemInfo item = getDetailByID(id);
			addSelected(item);
		}
	}
	
	public ArrayList<Integer> getSelectedIds() {
		ArrayList<Integer> ids = new ArrayList<Integer>();
		for(ItemDE sel : getSelected()) {
			ids.add(sel.info.id);
		}
		return ids;
	}

	Collection<ItemInfo> available;
	public void setAvailableValues(LinkedHashMap<Integer, String> groups_keyvalues) {
		available = new ArrayList<ItemInfo>();
		for(Integer id : groups_keyvalues.keySet()) {
			String name = groups_keyvalues.get(id);
			ItemInfo info = new ItemInfo();
			info.id = id;
			info.name = name;
			available.add(info);
		}	
	}
	protected Collection<ItemInfo> getAvailableRecords() {
		return available;
	}
}
