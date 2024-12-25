package com.stellantis.team.utility.model;

public enum DateTimeFormat {
	MM_DD_YYYY("MM/dd/yyyy hh:mm:ss a"),
	YYYY_MM_DD("yyyy-MM-dd HH:mm:ss"),
	DD_MM_YYYY("dd-MM-yyyy HH:mm:ss"),
	MMM_DD_YYYY("MMM dd, yyyy"),
	E_DD_MMM_YYYY("E, dd MMM yyyy HH:mm:ss z");
	
	private final String value;

	DateTimeFormat(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
