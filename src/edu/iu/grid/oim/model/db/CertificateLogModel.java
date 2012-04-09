package edu.iu.grid.oim.model.db;

import java.sql.SQLException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import edu.iu.grid.oim.model.Context;
import edu.iu.grid.oim.model.db.record.CertificateRecord;

public class CertificateLogModel extends ModelBase<CertificateRecord> {
	private Context contect;
    public CertificateLogModel(Context _context) {
		super(_context, "certificate_log");
		context = _context;
	}

	static Logger log = Logger.getLogger(CertificateLogModel.class);

	@Override
	public Boolean hasLogAccess(XPath xpath, Document doc)
			throws XPathExpressionException {
		// TODO Auto-generated method stub
		return null;
	}  
	
}
