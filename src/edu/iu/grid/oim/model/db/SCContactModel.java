package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.ResourceRecord;
import edu.iu.grid.oim.model.db.record.SCContactRecord;

public class SCContactModel extends SmallTableModelBase<SCContactRecord> {
    static Logger log = Logger.getLogger(SCContactModel.class); 

	public SCContactModel(Authorization _auth) {
		super(_auth, "sc_contact");
	}
	SCContactRecord createRecord(ResultSet rs) throws SQLException
	{
		return new SCContactRecord(rs);
	}
}
