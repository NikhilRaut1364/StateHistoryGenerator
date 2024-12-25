package com.stellantis.team.utility.model;

public enum Choice {
	SELECT("- Select -"),
	WORKITEM_TYPE("WorkItem Type"),
	WORKITEM_QUERY("WorkItem Query");
	
	private final String value;

	Choice(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
