package edu.iu.grid.oim.view;

import java.io.InputStream;
import java.io.PrintWriter;

import edu.iu.grid.oim.lib.StaticConfig;
import edu.iu.grid.oim.lib.FileReader;
import edu.iu.grid.oim.model.Context;

public class HtmlFileView implements IView 
{

	private String content;
	private Context context;
	
	public HtmlFileView(Context _context, String resource_name) {
		context = _context;
		InputStream is = this.getClass().getResourceAsStream(resource_name);
		loadContent(is);
	}
	public void loadContent(InputStream is)
	{
		content = FileReader.loadContent(is);
		content = content.replaceAll("__STATICBASE__", StaticConfig.getStaticBase());
		content = content.replaceAll("__APPNAME__", StaticConfig.getApplicationName());
		content = content.replaceAll("__VERSION__", StaticConfig.getVersion());
		if(context.getAuthorization().isGuest()) {
			content = content.replaceAll("__DN__", "Guest");
		} else {
			content = content.replaceAll("__DN__", context.getAuthorization().getUserDN());
		}
	}
	public void render(PrintWriter out) {
		out.print(content);
	}
	
}
