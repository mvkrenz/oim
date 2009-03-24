package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Action;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.lib.Authorization.AuthorizationException;
import edu.iu.grid.oim.model.db.record.CertificateDNRecord;

public class CertificateDNModel extends DBModel {
    static Logger log = Logger.getLogger(CertificateDNModel.class);  
    
    public CertificateDNModel(Connection _con) 
    {
    	super(_con, null);
    }
    
	public CertificateDNRecord findByDN(String dn_string) throws AuthorizationException
	{
		ResultSet rs = null;
		try {
			PreparedStatement pstmt = con.prepareStatement(
	                "SELECT * FROM certificate_dn where dn_string = ?");
	        pstmt.setString(1, dn_string);
	        rs = pstmt.executeQuery();
	        if(rs.next()) {
	        	return new CertificateDNRecord(rs);
	        } else {
	        	return null;
	        }
		} catch(SQLException e) {
			log.error(e.getMessage());
		}
		return null;
	}
}
