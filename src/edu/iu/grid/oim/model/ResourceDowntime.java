package edu.iu.grid.oim.model;

import java.util.ArrayList;

import edu.iu.grid.oim.model.db.record.ResourceDowntimeRecord;
import edu.iu.grid.oim.model.db.record.ResourceDowntimeServiceRecord;

public class ResourceDowntime {
	public ResourceDowntimeRecord downtime;
	public ArrayList<ResourceDowntimeServiceRecord> services;
}
