package edu.iu.grid.oim.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOReportConsolidator {
	public VOReportNameRecord name;
	public ArrayList<VOReportNameFqanRecord> fqans;
	public ArrayList<VOReportContactRecord> vorep_contacts;
	public ArrayList<ContactRecord> contacts;
	
//	public VOReportConsolidator(VOReportNameRecord _name, 
//				ArrayList<VOReportNameFqanRecord> _fqans,
//				ArrayList<ContactRecord> _contacts) 
//	{
//		name     = _name;
//		fqans    = _fqans;
//		contacts = _contacts;
//	}
	
	public VOReportConsolidator(VOReportNameRecord _name, 
			ArrayList<VOReportNameFqanRecord> _fqans,
			ArrayList<VOReportContactRecord> _contacts) 
	{
		name           = _name;
		fqans          = _fqans;
		vorep_contacts = _contacts;
	}
}
