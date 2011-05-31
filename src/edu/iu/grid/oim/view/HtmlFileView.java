package edu.iu.grid.oim.view;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import edu.iu.grid.oim.lib.ResourceReader;

public class HtmlFileView implements IView 
{
	StringBuilder content;
	
	
	public HtmlFileView(String template, HashMap<String, String> params) {
		content = new StringBuilder(template);
		
		//replace templates
		for(String key : params.keySet()) {
			String value = params.get(key);
			int pos = content.indexOf(key);			
			while(pos != -1) {
				content.replace(pos, key.length()+pos, value);
				pos = content.indexOf(key, pos);	
			}
		}
	}
	
	public HtmlFileView(InputStream is, HashMap<String, String> params) {
		content = ResourceReader.loadContent(is);
		
		//replace templates
		for(String key : params.keySet()) {
			String value = params.get(key);
			int pos = content.indexOf(key);			
			while(pos != -1) {
				content.replace(pos, key.length()+pos, value);
				pos = content.indexOf(key, pos);	
			}
		}
	}
	
	public String toString() {
		return content.toString();
	}

	public void render(PrintWriter out) {
		out.print(content);
		
	}
	
}
