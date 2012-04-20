package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import javax.sql.DataSource;


public class ConfigModel {
	
	public class NoSuchConfigException extends Exception {}
    static Logger log = Logger.getLogger(ConfigModel.class);  
    
    //protected Connection connection;
	//protected Authorization auth;
	
	public class Config {
		String key;
		String def;
		ConfigModel model;
		Config(ConfigModel model, String key, String def) {
			this.model = model;
			this.key = key;
			this.def = def;
		}
		
		//return current config value. returns default is not set yet
		public String get() {
			try {
				return model.get(key);
			} catch (NoSuchConfigException e) {
				return def;
			} catch (SQLException e) {
				log.error("Failed to obtain value for " + key, e);
				return null;
			}
		}
		
		public void set(String value) throws SQLException {
			model.set(key, value);
		}
	}
	
	public Config ResourceFPTemplate = new Config(this, "resource_fp_template", 
		"here is my default fp template");
	public Config VOFPTemplate = new Config(this, "resource_vo_template", 
		"here is my default vo template");
	public Config SCFPTemplate = new Config(this, "resource_sc_template", 
		"here is my default sc template");	
	
    private DataSource _oimds = null;
	protected Connection connectOIM() throws SQLException {
		if(_oimds == null) {
		    try {
		    	
		    	Context initContext = new InitialContext();
		    	Context envContext  = (Context)initContext.lookup("java:/comp/env");
		    	_oimds = (DataSource)envContext.lookup("jdbc/oim");
		    } catch( NamingException ne ) {
		    	throw new RuntimeException( "Unable to aquire data source", ne );
		    }
		}
		
		return _oimds.getConnection();
	}
	
	private String get(String key) throws SQLException, NoSuchConfigException {
		Connection connection = connectOIM();
		PreparedStatement stmt = connection.prepareStatement("SELECT `value` FROM config WHERE `key` = ?");
		stmt.setString(1, key);
		String value = null;
		ResultSet rs = stmt.executeQuery();
		if(rs != null) {
	    	if(rs.next()) {
	    		value = rs.getString(1);
			} else {
		    	throw new NoSuchConfigException();
		    }
	    } else {
	    	log.error("ConfigModel::get didn't return result set");
	    }
	    stmt.close();
	    connection.close();
	    return value;
	}
	
	private void set(String key, String value) throws SQLException {
		Connection connection = connectOIM();
		int affected;
		try {
			//does the value exist?
			get(key);
			
			//update the value
			PreparedStatement stmt = connection.prepareStatement("UPDATE config SET `value` = ? WHERE `key` = ?");
			stmt.setString(1, value);
			stmt.setString(2, key);
			affected = stmt.executeUpdate();
		} catch (NoSuchConfigException e) {
			//never been set before.. insert
			PreparedStatement stmt = connection.prepareStatement("INSERT INTO config (`key`, `value`) VALUES (?,?)");
			stmt.setString(1, key);
			stmt.setString(2, value);	
			affected = stmt.executeUpdate();
		} 
		if(affected != 1) {
			log.error("Failed to set " + key + " with value " + value);
		}
		connection.close();
	}
	

}

