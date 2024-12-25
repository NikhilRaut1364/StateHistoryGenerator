package com.stellantis.team.utility.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.WorkItemController;
import com.stellantis.team.utility.model.KeyValuePair;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.utils.CustomLogger;

public class WorkItemTypeWorker extends UtilitySwingWorker{

	private IProjectArea projectArea;
	private List<KeyValuePair> lstWorkItemType;

	public List<KeyValuePair> getLstWorkItemType() {
		if(lstWorkItemType == null)
			lstWorkItemType = new ArrayList<>();
		return lstWorkItemType;
	}

	public void setLstWorkItemType(List<KeyValuePair> lstWorkItemType) {
		this.lstWorkItemType = lstWorkItemType;
	}

	public WorkItemTypeWorker(IProjectArea projectArea) {
		this.projectArea = projectArea;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		boolean isValid = false;
		try {
			publish(Status.INFO.toString() + "@" + "Please wait as we retrieve the work item types from the selected project area.");
			WorkItemController workItemController = new WorkItemController();
			Map<String, String> fetchWorkItemType = workItemController.fetchWorkItemTypeOfSpecificProjectArea(projectArea);
			for (Map.Entry<String, String> entry : fetchWorkItemType.entrySet()){
				getLstWorkItemType().add(new KeyValuePair(entry.getValue(), entry.getKey()));
			}
			if(getLstWorkItemType().size() > 0)
				isValid = true;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return isValid;
	}
}
