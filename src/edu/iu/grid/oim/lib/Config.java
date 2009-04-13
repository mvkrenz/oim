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
	static public String getApplicationBase() {
		return config.appbase;
	}
    private String staticbase;
	static public String getStaticBase() {
		return config.staticbase;
	}	
    
	//why privae? - don't allow client instantiation (use getInstance) 
	private Config() 
	{
		try {
			XPath xpath = XPathFactory.newInstance().newXPath();
			InputStream is = getClass().getResourceAsStream("/config.xml");
			InputSource config_xml = new InputSource(is);	
	
			//load config
			appbase = (String)xpath.evaluate("//ApplicationBase", config_xml);
			is.reset();
			
			staticbase = (String)xpath.evaluate("//StaticBase", config_xml);
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
