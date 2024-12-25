package com.stellantis.team.utility.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WIHistory {

	private int id;
	private String type;
	private String summary;
	private String status;
	private String ownerName;
	private StringBuilder history = new StringBuilder();
	private Map<String, Date> firstDateOfStatusMap;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Date> getFirstDateOfStatusMap() {
		if (firstDateOfStatusMap == null) {
			firstDateOfStatusMap = new HashMap<>(0);
		}
		return firstDateOfStatusMap;
	}

	public int getId() {
		return id;
	}

	public WIHistory setId(int id) {
		this.id = id;
		return this;
	}

	public String getSummary() {
		return summary;
	}

	public WIHistory setSummary(String summary) {
		this.summary = summary;
		return this;
	}

	public String getStatus() {
		return status;
	}

	public WIHistory setStatus(String status) {
		this.status = status;
		return this;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public WIHistory setOwnerName(String ownerName) {
		this.ownerName = ownerName;
		return this;
	}

	public StringBuilder getHistory() {
		return history;
	}

	public WIHistory setHistory(StringBuilder history) {
		this.history = history;
		return this;
	}
}
