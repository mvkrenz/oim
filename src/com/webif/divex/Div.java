package com.webif.divex;
import java.util.HashMap;

public abstract class Div {
	static private HashMap<String, Div> nodes = new HashMap<String, Div>();	
	static private int next_nodeid = 0;

	private String nodeid;
	private Boolean needupdate = false;
	
	public static String bootcode()
	{
		String boot = "<script type=\"text/javascript\">"+
		"function divex_click(node) {"+
		"$.getScript('divex?nodeid='+node.id+'&action=click');"+
		"}"+
		"</script>";
		return boot;
	}

	public static String outputUpdatecode()
	{
		String code = "";
		for(Div d : Div.nodes.values()) {
			if(d.needupdate) {
				code += "$(\"#"+d.nodeid+"\").load(\"divex?action=load&nodeid="+d.nodeid+"\");";
				d.needupdate = false;
			}
		}	
		return code;
	}
	public Div() {
		nodeid = "divex_"+next_nodeid;
		++next_nodeid;
		Div.nodes.put(nodeid, this);
	}
	
	final public String render() {
		String html = "";
		html += "<div class='divex' id='"+nodeid+"' onclick='divex_click(this);'>";
		html += toHtml();
		html += "</div>";
		return html;
	}
	
	abstract protected String toHtml();
	
	protected void onClick(Event e) {
	}
	
	public void update() {
		needupdate = true;
	}


	public static String encodeHTML(String s)
	{
	    StringBuffer out = new StringBuffer();
	    for(int i=0; i<s.length(); i++)
	    {
	        char c = s.charAt(i);
	        if(c > 127 || c=='"' || c=='\'' || c=='<' || c=='>')
	        {
	           out.append("&#"+(int)c+";");
	        }
	        else
	        {
	            out.append(c);
	        }
	    }
	    return out.toString();
	}
	
	public static Div findNode(String nodeid) {
		Div node = nodes.get(nodeid);
		return node;
	}
}
