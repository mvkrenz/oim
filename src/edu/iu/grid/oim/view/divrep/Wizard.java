package edu.iu.grid.oim.view.divrep;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang.StringEscapeUtils;

import com.divrep.DivRep;
import com.divrep.DivRepEvent;

import edu.iu.grid.oim.view.IView;

public class Wizard extends DivRep 
{
	public Wizard(DivRep _parent) {
		super(_parent);
	}

	private static final long serialVersionUID = 1L;

	ArrayList<IView> step_views = new ArrayList<IView>();
	HashMap<IView, String> step_labels = new HashMap<IView, String>();
	public void addStep(String label, IView view) {
		step_views.add(view);
		step_labels.put(view, label);
	}
	
	public void render(PrintWriter out) {
		//render tab output
		out.write("<ul>");
		for(IView view : step_views) {
			String label = step_labels.get(view);
			out.write("<li>"+StringEscapeUtils.escapeHtml(label)+"</li>");
		}
		out.write("</ul>");
	}

	@Override
	protected void onEvent(DivRepEvent e) {
		// TODO Auto-generated method stub
		
	}
}
