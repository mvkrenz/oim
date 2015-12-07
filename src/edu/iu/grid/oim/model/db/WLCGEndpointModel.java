package edu.iu.grid.oim.model.db;

import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.RecordBase;
import edu.iu.grid.oim.model.db.record.WLCGEndpointRecord;

public class WLCGEndpointModel extends SmallTableModelBase<WLCGEndpointRecord> {
    static Logger log = Logger.getLogger(WLCGEndpointModel.class); 

	public WLCGEndpointModel(UserContext context) {
		super(context, "wlcg_endpoint");

		//empty everytime model is requested - since this table is udpated outside of OIM
		emptyCache(); 
		log.debug("emptying cache for wlcg_endpoint");
	}
    public String getName()
    {
    	return "WLCG Endpoints";
    }
    /*
	public String getHumanValue(String field_name, String value) throws NumberFormatException, SQLException
	{
		return value;
	}
	public Boolean hasLogAccess(XPath xpath, Document doc) throws XPathExpressionException
	{
		//Integer id = Integer.parseInt((String)xpath.evaluate("//Keys/Key[Name='id']/Value", doc, XPathConstants.STRING));
		if(auth.allows("admin")) {
			return true;
		}
		return false;
	}
	*/
	
    WLCGEndpointRecord createRecord() throws SQLException
	{
		return new WLCGEndpointRecord();
	}
    /*
	public ArrayList<WLCGSiteRecord> getAll() throws SQLException
	{
		ArrayList<WLCGSiteRecord> list = new ArrayList<WLCGSiteRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name)) {
	    	rs = stmt.getResultSet();
	    	while(rs.next()) {
	    		WLCGSiteRecord rec = createRecord();
	    		rec.set(rs);
	    		list.add(rec);
			}
	    }	
	    stmt.close();
	    conn.close();
		return list;
	}
	*/
	public WLCGEndpointRecord get(String key) throws SQLException {
		WLCGEndpointRecord keyrec = new WLCGEndpointRecord();
		keyrec.primary_key = key;
		return get(keyrec);
	}
	public ArrayList<WLCGEndpointRecord> getByServiceID(int id) throws SQLException
	{ 
		ArrayList<WLCGEndpointRecord> list = new ArrayList<WLCGEndpointRecord>();
		for(RecordBase rec : getCache()) {
			WLCGEndpointRecord vcrec = (WLCGEndpointRecord)rec;
			if(vcrec.service_id != null && vcrec.service_id.equals(id)) list.add(vcrec);
		}
		return list;
	}	
    /*
	public WLCGEndpointRecord getByPrimaryKey(String id) throws SQLException {
		WLCGEndpointRecord rec = null;
		Connection con = connectOIM();
		PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "+table_name+" WHERE primary_key = ''");
		pstmt.setString(1, id);
    	ResultSet rs = pstmt.executeQuery();
    	if(rs.next()) {
    		rec = createRecord();
		}
	    pstmt.close();
	    con.close();
		return rec;
	}
	public ArrayList<WLCGEndpointRecord> getByServiceID(Integer id) throws SQLException {
		ArrayList<WLCGEndpointRecord> list = new ArrayList<WLCGEndpointRecord>();
		ResultSet rs = null;
		Connection conn = connectOIM();
		Statement stmt = conn.createStatement();
	    if (stmt.execute("SELECT * FROM "+table_name + " WHERE service_id = "+id)) {
	    	rs = stmt.getResultSet();
	    	while(rs.next()) {
	    		WLCGEndpointRecord rec = createRecord();
	    		rec.set(rs);
	    		list.add(rec);
			}
	    }	
	    stmt.close();
	    conn.close();
		return list;
	}
	*/
}
