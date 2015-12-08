package edu.iu.grid.oim.model.db;

import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.UserContext;
import edu.iu.grid.oim.model.db.record.WLCGSiteRecord;

public class WLCGSiteModel extends SmallTableModelBase<WLCGSiteRecord> {
    static Logger log = Logger.getLogger(WLCGSiteModel.class); 

	public WLCGSiteModel(UserContext context) {
		super(context, "wlcg_site");
		
		//empty everytime model is requested - since this table is udpated outside of OIM
		emptyCache(); 
		log.debug("emptying cache for wlcg_site");
	}
    public String getName()
    {
    	return "WLCG Site";
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
	
    WLCGSiteRecord createRecord() throws SQLException
	{
		return new WLCGSiteRecord();
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
    /*
	public WLCGSiteRecord getByPrimaryKey(String key) throws SQLException {
		WLCGSiteRecord rec = null;
		Connection con = connectOIM();
		PreparedStatement pstmt = con.prepareStatement("SELECT * FROM "+table_name+" WHERE primary_key = ?");
		pstmt.setString(1, key);
    	ResultSet rs = pstmt.executeQuery();
    	if(rs.next()) {
    		rec = createRecord();
    		rec.set(rs);
		}
	    pstmt.close();
	    con.close();
		return rec;
	}
	*/
	public WLCGSiteRecord get(String key) throws SQLException {
		WLCGSiteRecord keyrec = new WLCGSiteRecord();
		keyrec.primary_key = key;
		return get(keyrec);
	}
}
