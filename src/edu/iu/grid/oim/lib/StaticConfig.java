package edu.iu.grid.oim.lib;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

//we now have *db* configuration (ConfigModel) now.. use it instead
public class StaticConfig {
	public static Properties conf = new Properties();
	
	static public String getVersion() { return "3.0"; } //version should be hardcoded.
	
	static Logger log = Logger.getLogger(StaticConfig.class);  
    
	//just use getProperty() directly.. exists for backward compatibility and to reduce error
	static public String getApplicationBase() { return conf.getProperty("application.base"); }
	static public String getApplicationName() { return conf.getProperty("application.name"); }
	static public Boolean isDebug() { return conf.getProperty("debug").equals("true"); }	
	static public String getGMapAPIKey() { return conf.getProperty("gmapapikey"); }
	static public int getConfirmationExpiration() { return Integer.parseInt(conf.getProperty("confirmation.expiration")); }
	static public int getDowntimeEditableEndDays() { return Integer.parseInt(conf.getProperty("downtime.editable.endday")); }
	static public String getFootprintsUri() { return conf.getProperty("footprints.uri"); }
	static public String getFootprintsUrl() { return conf.getProperty("footprints.url"); }
	static public String getFootprintsUsername() { return conf.getProperty("footprints.username"); }
	static public String getFootprintsPassword() { return conf.getProperty("footprints.password"); }
	static public Integer getFootprintsProjectID() { return Integer.parseInt(conf.getProperty("footprints.projectid")); }
	static public String getDOECN() { return conf.getProperty("footprints.doecn"); }
	static public String getSSLTrustStorePath() { return conf.getProperty("ssl.truststore"); }
    
	//why private? - don't allow client instantiation (use static getters)
	static 
	{
		try {
			InputStream is = StaticConfig.class.getResourceAsStream("/oim.conf");
			conf.load(is);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//static private StaticConfig config = new StaticConfig();
}
