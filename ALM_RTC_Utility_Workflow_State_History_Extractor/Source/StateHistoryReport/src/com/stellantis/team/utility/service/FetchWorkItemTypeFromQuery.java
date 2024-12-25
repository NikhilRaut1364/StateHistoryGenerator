package com.stellantis.team.utility.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.QueryController;
import com.stellantis.team.utility.model.KeyValuePair;
import com.stellantis.team.utility.model.Status;

public class FetchWorkItemTypeFromQuery extends UtilitySwingWorker{
	
	private IProjectArea projectArea;
	private String queryUUID;
	private List<KeyValuePair> lstWorkitemTypeFromQuery;

	public List<KeyValuePair> getLstWorkitemTypeFromQuery() {
		if(lstWorkitemTypeFromQuery == null)
			lstWorkitemTypeFromQuery = new ArrayList<>();
		return lstWorkitemTypeFromQuery;
	}

	public void setLstWorkitemTypeFromQuery(List<KeyValuePair> lstWorkitemTypeFromQuery) {
		this.lstWorkitemTypeFromQuery = lstWorkitemTypeFromQuery;
	}

	public FetchWorkItemTypeFromQuery(IProjectArea projectArea, String queryUUID) {
		this.projectArea = projectArea;
		this.queryUUID = queryUUID;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		try {
			publish(Status.INFO.toString() + "@" + "Please Wait! we are fetching workitem types from selected query.");
			QueryController queryController = new QueryController();
			Map<String, String> workItemTypeFromQuery = queryController.getWorkItemTypeFromQuery(projectArea, queryUUID);
			for (Map.Entry<String, String> entry : workItemTypeFromQuery.entrySet()){
				getLstWorkitemTypeFromQuery().add(new KeyValuePair(entry.getValue(), entry.getKey()));
			}
			if (getLstWorkitemTypeFromQuery() != null && !getLstWorkitemTypeFromQuery().isEmpty()) {
				if (getLstWorkitemTypeFromQuery().size() > 0)
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
