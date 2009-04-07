package edu.iu.grid.oim.notification;

import com.google.gdata.client.*;
import com.google.gdata.client.blogger.*;
import com.google.gdata.data.*;
import com.google.gdata.data.blogger.BlogEntry;
import com.google.gdata.data.blogger.BlogFeed;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Set;

//post the public log information to somewhere user can access to
public class PublicNotification {
	public  static BlogEntry publish(String title, String content, ArrayList<String> categories) throws IOException, ServiceException
	{
		String BlogID = "5150742677623931956"; //OIM Update (obtained from the URL on blogger.com)
		
		//connect to our blogger service
		URL postUrl = new URL("https://www.blogger.com/feeds/"+BlogID+"/posts/default");
		GoogleService myService = new GoogleService("blogger", "OSGGOC-OIM2");
		myService.setUserCredentials("goc@opensciencegrid.org", "goc345#pw");
		new BlogFeed().declareExtensions(myService.getExtensionProfile());
		Feed myFeed = myService.getFeed(postUrl, Feed.class);

		//construct our post
		BlogEntry myEntry = new BlogEntry();
		myEntry.setTitle(new PlainTextConstruct(title));
		myEntry.setContent(new PlainTextConstruct(content));
		for(String cat : categories) {
			myEntry.getCategories().add(new Category("http://www.blogger.com/atom/ns#", cat));
		}
		
		//do insert
		BlogEntry insertedEntry = myService.insert(postUrl, myEntry);
		
		return insertedEntry;
	}
}
