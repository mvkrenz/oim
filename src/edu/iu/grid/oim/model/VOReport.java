package edu.iu.grid.oim.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.iu.grid.oim.model.db.record.VOReportNameRecord;
import edu.iu.grid.oim.model.db.record.VOReportNameFqanRecord;
import edu.iu.grid.oim.model.db.record.ContactRecord;
import edu.iu.grid.oim.model.db.record.VOReportContactRecord;

public class VOReport {
	public VOReportNameRecord name;
	public ArrayList<VOReportNameFqanRecord> fqans;
	public ArrayList<ContactRecord> contacts;
}
