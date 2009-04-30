package edu.iu.grid.oim.lib;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class Config {
    static Logger log = Logger.getLogger(Config.class);  
    
    private String appbase;
	static public String getApplicationBase() { return config.appbase; }
    
	private String staticbase;
	static public String getStaticBase() { return config.staticbase; }

	private Boolean debug;
	static public Boolean isDebug() { return config.debug; }	
	
	//Footprints
	private String footprints_uri;
	static public String getFootprintsUri() { return config.footprints_uri; }
	private String footprints_url;
	static public String getFootprintsUrl() { return config.footprints_url; }
	private String footprints_username;
	static public String getFootprintsUsername() { return config.footprints_username; }
	private String footprints_password;
	static public String getFootprintsPassword() { return config.footprints_password; }
	
	private String ssl_truststore;
	static public String getSSLTrustStorePath() { return config.ssl_truststore; }
    
	//why private? - don't allow client instantiation (use static getters)
	private Config() 
	{
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			InputStream is = getClass().getResourceAsStream("/config.xml");
			InputSource config_xml = new InputSource(is);	

			appbase = (String)xpath.evaluate("//ApplicationBase", config_xml);
			is.reset();
			
			staticbase = (String)xpath.evaluate("//StaticBase", config_xml);
			is.reset();
			
			debug = ((String)xpath.evaluate("//Debug", config_xml)).compareTo("true") == 0;
			is.reset();

			footprints_uri = (String)xpath.evaluate("//Footprints/URI", config_xml);
			is.reset();
			
			footprints_url = (String)xpath.evaluate("//Footprints/URL", config_xml);
			is.reset();
			
			footprints_username = (String)xpath.evaluate("//Footprints/Username", config_xml);;
			is.reset();

			footprints_password = (String)xpath.evaluate("//Footprints/Password", config_xml);;
			is.reset();
			
			ssl_truststore = (String)xpath.evaluate("//SSLTruststorePath", config_xml);;
			is.reset();

		} catch (XPathExpressionException e) {
			log.error(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//singleton
	static private Config config = new Config();

}
