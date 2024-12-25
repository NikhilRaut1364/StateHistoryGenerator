package com.stellantis.team.utility.model;

import com.ibm.team.workitem.common.query.IQueryDescriptor;

public class DataDictionary {
	private static DataDictionary dataDictionary;
	private IQueryDescriptor queryDescriptor;
	
	public IQueryDescriptor getQueryDescriptor() {
		return queryDescriptor;
	}

	public void setQueryDescriptor(IQueryDescriptor queryDescriptor) {
		this.queryDescriptor = queryDescriptor;
	}

	private DataDictionary() {
		
	}
	
	public static DataDictionary getInstance() {
		if (dataDictionary == null)
			dataDictionary = new DataDictionary();
		return dataDictionary;
	}
}
