package edu.iu.grid.oim.test;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.w3c.dom.Document;

public class DigiCertAccessTest {

	public static void main(String[] args) {
        HttpClient cl = new HttpClient();
        cl.getParams().setParameter("http.useragent", "OIM (OSG Information Management System)");

        PostMethod post = new PostMethod("https://www.digicert.com/enterprise/api/?action=grid_request_email_cert");
		try {
			cl.executeMethod(post);
            String line;
            BufferedReader in = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream()));
            while((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
