package edu.iu.grid.oim.model.db;

import java.sql.SQLException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.CertificateLogRecord;
import edu.iu.grid.oim.model.db.record.CertificateRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;

public class CertificateModel extends ModelBase<CertificateRecord> {
	private Context contect;
    public CertificateModel(Context _context) {
		super(_context, "certificate");
		context = _context;
	}

	static Logger log = Logger.getLogger(CertificateModel.class);

	@Override
	public Boolean hasLogAccess(XPath xpath, Document doc)
			throws XPathExpressionException {
		// TODO Auto-generated method stub
		return null;
	}  
	
	public boolean request(CertificateRecord rec) {
		//store certificate request
		try {
			Integer id = insert(rec);
			updateLog(id, "REQUEST", "");
		} catch (SQLException e) {
			log.error("Failed to insert certificate record", e);
			return false;
		}
	
		return true;
	}
	
	protected void updateLog(Integer certificate_id, String activity, String detail) throws SQLException
	{
		CertificateLogRecord logrec = new CertificateLogRecord();
		logrec.certificate_id = certificate_id;
		ContactRecord crec = context.getAuthorization().getContact();
		if(crec != null) {
			logrec.contact_id = crec.id;
		}
		logrec.remote_addr = context.getRemoteAddr();
		logrec.activity = activity;
		logrec.detail = detail;
		CertificateLogModel model = new CertificateLogModel(context);
		model.insert(logrec);		
	}
}
