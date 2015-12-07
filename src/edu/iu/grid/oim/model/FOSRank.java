package edu.iu.grid.oim.model;

public enum FOSRank {
	Primary(1), Secondary(2);

	public int id;
	private FOSRank(int id) {
		this.id = id;
	}

	public static FOSRank get(Integer id) {
		for (FOSRank type : FOSRank.values()) {
			if (id.equals(type.id)) {
				return type;
			}
		}
		return null;
	}
	
	//use .value for id
}