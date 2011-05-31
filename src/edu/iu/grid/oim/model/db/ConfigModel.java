package edu.iu.grid.oim.model.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.Context;

public class ConfigModel {
	
	/*
	public class ConfigException extends Exception {
		public ConfigException(String msg) {
			super(msg);
		}
	}
	*/
	public class NoSuchConfigException extends Exception {}
	
    static Logger log = Logger.getLogger(ConfigModel.class);  
    
    protected Context context;
	protected Authorization auth;
	
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
	
	public ConfigModel(Context context)
	{
		this.context = context;
		auth = context.getAuthorization();
	}

	private String get(String key) throws SQLException, NoSuchConfigException {
		Connection conn = context.connectOIM();
		PreparedStatement stmt = conn.prepareStatement("SELECT `value` FROM config WHERE `key` = ?");
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
	    return value;
	}
	
	private void set(String key, String value) throws SQLException {
		Connection conn = context.connectOIM();
		int affected;
		try {
			//does the value exist?
			get(key);
			
			//update the value
			PreparedStatement stmt = conn.prepareStatement("UPDATE config SET `value` = ? WHERE `key` = ?");
			stmt.setString(1, value);
			stmt.setString(2, key);
			affected = stmt.executeUpdate();
		} catch (NoSuchConfigException e) {
			//never been set before.. insert
			PreparedStatement stmt = conn.prepareStatement("INSERT INTO config (`key`, `value`) VALUES (?,?)");
			stmt.setString(1, key);
			stmt.setString(2, value);	
			affected = stmt.executeUpdate();
		} 
		if(affected != 1) {
			log.error("Failed to set " + key + " with value " + value);
		}
	}
	

}

