package edu.iu.grid.oim.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileReader {
	static public String loadContent(InputStream is)
	{
		InputStreamReader reader = new InputStreamReader(is);
		StringBuilder sb = new StringBuilder();
        try {
			int c;
			while ((c = is.read()) != -1) {
				sb.append((char)c);
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
