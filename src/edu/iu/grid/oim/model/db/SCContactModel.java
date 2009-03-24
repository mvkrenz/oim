package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.VOContactRecord;

public class SCContactModel extends ContactModel {

	public SCContactModel(Connection _con, Authorization _auth) {
		super(_con, _auth, "sc_contact", "sc_id");
		// TODO Auto-generated constructor stub
	}
	
	public HashMap<Integer, ArrayList<Integer>> get(int sc_id) throws AuthorizationException, SQLException
	{
		auth.check(Action.read_sccontact);
		return getRecords(sc_id);
	}
}
