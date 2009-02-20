package edu.iu.grid.oim.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class HtmlFileView implements View {

	private String resource_name;
	
	public HtmlFileView(String _resource_name) {
		resource_name = _resource_name;
	}

	public String toHTML() {
		InputStream is = this.getClass().getResourceAsStream(resource_name);
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
}
