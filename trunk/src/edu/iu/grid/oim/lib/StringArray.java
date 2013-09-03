package edu.iu.grid.oim.lib;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class StringArray  {
	private String[] strings;
	public String get(int idx) { return strings[idx]; }
	public void set(int idx, String str) { strings[idx] = str; }
	public String[] getAll() { return strings; }
	public StringArray(String xml) {
		//deserialize from xml
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			Document dom = db.parse(is);
			
			//create string array
			Node size_nl = dom.getElementsByTagName("Size").item(0);
			String size_str = size_nl.getTextContent();
			int size = Integer.parseInt(size_str);
			strings = new String[size];
			
			//populate data
			NodeList string_nl = dom.getElementsByTagName("String");
			for(int i = 0;i < string_nl.getLength(); ++i) {
				Node n = string_nl.item(i);
				Node null_mark = n.getAttributes().getNamedItem("null");
				if(null_mark != null) {
					strings[i] = null;
				} else {
					strings[i] = n.getTextContent();
				}
			}
			
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	public StringArray(int size) {
		strings = new String[size];
	}
	public String toXML() {
		StringBuffer out = new StringBuffer();
		out.append("<StringArray>");
		out.append("<Size>"+strings.length+"</Size>");
		for(String s : strings) {
			if(s == null) {
    			out.append("<String null=\"true\"></String>");
			} else {
				out.append("<String>"+StringEscapeUtils.escapeXml(s)+"</String>");
			}
		}
		out.append("</StringArray>");
		return out.toString();
	}
	public int length() { return strings.length; }

}