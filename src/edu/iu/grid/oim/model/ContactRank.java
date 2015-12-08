package edu.iu.grid.oim.model;

public enum ContactRank {
	Primary(1), Secondary(2), Tertiary(3);

	public int id;
	private ContactRank(int id) {
		this.id = id;
	}

	public static ContactRank get(Integer id) {
		for (ContactRank type : ContactRank.values()) {
			if (id.equals(type.id)) {
				return type;
			}
		}
		return null;
	}
	
	//use .value for id
}