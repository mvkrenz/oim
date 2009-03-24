package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class ContactModel extends DBModel {
	
	private String tablename;
	private String fkey;

	//tablename - such as "sc_contact";
	//fkey - such as "sc_id" which maps the foreign key
	public ContactModel(Connection _con, Authorization _auth, String _tablename, String _fkey) {
		super(_con, _auth);
		tablename = _tablename;
		fkey = _fkey;
	}
	
	public HashMap<Integer, ArrayList<Integer>> getRecords(int id) throws SQLException
	{
		PreparedStatement stmt = null;

		String sql = "SELECT * FROM "+tablename+" WHERE "+fkey+" = ? order by rank_id";
		stmt = con.prepareStatement(sql); 
		stmt.setInt(1, id);
	
		ResultSet rs = stmt.executeQuery();

		HashMap<Integer, ArrayList<Integer>> list = new HashMap();
		while(rs.next()) {
			int person_id = rs.getInt("person_id");
			int fid = rs.getInt(fkey);
			int type_id = rs.getInt("type_id");
			int rank_id = rs.getInt("rank_id"); //ignore rank_id - we just sort the query by rank_id (forget the discontinuous rank_id)
			ArrayList<Integer> a = null;
			if(!list.containsKey(type_id)) {
				a = new ArrayList<Integer>();
				list.put(type_id, a);
			} else {
				a = list.get(type_id);
			}
			a.add(person_id);
		}
		return list;
	}
}
