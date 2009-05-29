package edu.iu.grid.oim.view;

import java.io.InputStream;
import java.io.PrintWriter;

import edu.iu.grid.oim.lib.Config;
import edu.iu.grid.oim.lib.FileReader;

public class HtmlFileView implements IView {

	private String content;
	
	public HtmlFileView(String resource_name) {
		InputStream is = this.getClass().getResourceAsStream(resource_name);
		loadContent(is);
		
	}
	public void loadContent(InputStream is)
	{
		content = FileReader.loadContent(is);
		content = content.replaceAll("__STATICBASE__", Config.getStaticBase());
		content = content.replaceAll("__APPNAME__", Config.getApplicationName());
		content = content.replaceAll("__VERSION__", Config.getVersion());
	}
	public void render(PrintWriter out) {
		out.print(content);
	}
	
}
