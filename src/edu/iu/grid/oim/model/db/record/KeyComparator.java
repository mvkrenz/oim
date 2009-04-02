package edu.iu.grid.oim.model.db.record;

import java.util.Comparator;

public class KeyComparator implements Comparator<RecordBase>
{
	public int compare(RecordBase o1, RecordBase o2) {
		return o1.compareKeysTo(o2);
	}	
}
