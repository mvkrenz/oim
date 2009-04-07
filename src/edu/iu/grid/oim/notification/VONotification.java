package edu.iu.grid.oim.notification;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;

import javax.xml.xpath.*;

import org.w3c.dom.Document;

import com.webif.divex.DivEx;
import com.webif.divex.form.SelectFormElementDE;
import edu.iu.grid.oim.lib.Authorization;
import edu.iu.grid.oim.model.db.ContactModel;
import edu.iu.grid.oim.model.db.VOModel;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VORecord;
import edu.iu.grid.oim.view.GenericView;
import edu.iu.grid.oim.view.HtmlView;
import edu.iu.grid.oim.view.IView;
import edu.iu.grid.oim.view.RecordTableView;

public class VONotification extends NotificationBase {

	private SelectFormElementDE voselector;
	private Integer vo_id;
	private Integer contact_id;
	
	void init(XPath xpath, Document doc) throws XPathExpressionException {
		vo_id = Integer.parseInt(xpath.evaluate("//Notification/VOID", doc));
		contact_id = Integer.parseInt(xpath.evaluate("//Notification/ContactID", doc));
	}
	
	public String getTitle() {
		return "VO Notification";
	}

	public RecordTableView createReadView(DivEx root, Authorization auth)
	{
		RecordTableView view = new RecordTableView();
		try {		
			VOModel vomodel = new VOModel(auth);
			VORecord vo = vomodel.get(vo_id);
			view.addRow("Notify any changes on", new HtmlView(vo.name));
			
			ContactModel cmodel = new ContactModel(auth);
			ContactRecord contact = cmodel.get(contact_id);
			view.addRow("Send notification to", new HtmlView(contact.name));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return view;
	}
	public IView createEditView(DivEx root, Authorization auth)
	{
		GenericView view = new GenericView();
		view.add(new HtmlView("Notify any changes for following VO"));
		
		//construct voselector
		view.add(new HtmlView("Send notification to"));
		VOModel model = new VOModel(auth);
		HashMap<Integer, String> volist = new HashMap();
		try {
			for(VORecord rec : model.getAll()) {
				volist.put(rec.id, rec.name);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		voselector = new SelectFormElementDE(root, volist);
		view.add(voselector);
		
		return view;
	}

}
