package edu.iu.grid.oim.model.db.record;

public interface IKeyComparable<T> {
	public int compareKeyTo(T o);
}
