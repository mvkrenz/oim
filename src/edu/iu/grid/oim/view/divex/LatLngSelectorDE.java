package edu.iu.grid.oim.view.divex;

import java.io.PrintWriter;
import java.util.Random;

import org.apache.commons.lang.StringEscapeUtils;

import com.webif.divex.DivEx;
import com.webif.divex.Event;
import com.webif.divex.form.FormElementDEBase;

import edu.iu.grid.oim.lib.Config;

public class LatLngSelectorDE extends FormElementDEBase<LatLngSelectorDE.LatLng> {
	public class LatLng
	{
		public LatLng(Double lat, Double lng) {
			latitude = lat;
			longitude = lng;
		}
		public Double latitude;
		public Double longitude;
	}

	public LatLngSelectorDE(DivEx parent) {
		super(parent);
	}

	protected void onEvent(Event e) {
		String newval = (String)e.value;
		String coords[] = newval.split(",");
		value.latitude = Double.parseDouble(coords[0]);
		value.longitude = Double.parseDouble(coords[1]);
	}

	public void render(PrintWriter out) {
		out.write("<div ");
		renderClass(out);
		out.write("id=\""+getNodeID()+"\">");
		if(!hidden) {
			if(label != null) {
				out.print("<label>"+StringEscapeUtils.escapeHtml(label)+"</label><br/>");
			}
			String random = Integer.toString(Math.abs(new Random().nextInt()));
			out.write("<div id=\"map_canvas_"+getNodeID()+random+"\" style=\"width: 500px; height: 300px\"></div>"); 
		
			out.write("<script type=\"text/javascript\">");
		    out.write("if (GBrowserIsCompatible()) {");
		    //out.write("alert('setting up map');");
		    out.write("    var map = new GMap2(document.getElementById(\"map_canvas_"+getNodeID()+random+"\"));");
		    if(value.latitude == null || value.longitude == null) {
		    	out.write("    map.setCenter(new GLatLng(0, 0), 1);");		    	
		    } else {
		    	out.write("    map.setCenter(new GLatLng("+value.latitude+", "+value.longitude+"), 16);");
		    }
		    out.write("    map.setUIToDefault();");
		    out.write("    map.setMapType(G_HYBRID_MAP);");
		    //out.write("    map.enableGoogleBar();");
		    out.write("    var prompt = new GScreenOverlay(\""+Config.getApplicationBase()+"/images/target.png\",new GScreenPoint(0.5, 0.5, 'fraction', 'fraction'), new GScreenPoint(-50, -50), new GScreenSize(100, 100, 'pixel', 'pixel'));");
		    out.write("    map.addOverlay(prompt);");
		    out.write("    GEvent.addListener(map, 'moveend', function() {divex('"+getNodeID()+"', null, map.getCenter().toUrlValue());});");
		    out.write("}");
		    out.write("</script>");
		
			if(isRequired()) {
				out.write(" * Required");
			}
			error.render(out);	
		}
		out.write("</div>");
	}

}
