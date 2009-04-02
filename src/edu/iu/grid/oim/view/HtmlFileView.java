package edu.iu.grid.oim.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

public class HtmlFileView extends View {

	private String content;
	
	public HtmlFileView(String resource_name) {
		InputStream is = this.getClass().getResourceAsStream(resource_name);
		content = loadContent(is);
	}
	public HtmlFileView(InputStream is)
	{
		content = loadContent(is);
	}
	public void render(PrintWriter out) {
		out.print(content);
	}
	
	public String loadContent(InputStream is)
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
	}
	
	/* something like this might be a better solution
	 private final byte[] getFileAsBytes(final File file) throws IOException {
	final BufferedInputStream bis = new BufferedInputStream( 
		new FileInputStream(file));
	final byte [] bytes = new byte[(int) file.length()];
	bis.read(bytes);
	bis.close();
	return bytes;
}

	 * */
}
