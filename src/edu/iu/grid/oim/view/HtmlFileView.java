package edu.iu.grid.oim.view;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import edu.iu.grid.oim.lib.ResourceReader;

public class HtmlFileView implements IView 
{
	StringBuilder content;
	
	public HtmlFileView(String resource_name, HashMap<String, String> params) {
		InputStream is = this.getClass().getResourceAsStream(resource_name);
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
