package com.stellantis.team.utility.service;

import java.util.ArrayList;
import java.util.List;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.WorkItemController;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.utils.CustomLogger;

public class FetchStatesWorker extends UtilitySwingWorker{

	private IProjectArea projectArea;
	private String workItemTypeId;
	private List<String> lstWorkflowState;

	public List<String> getLstWorkflowState() {
		if(lstWorkflowState == null)
			lstWorkflowState = new ArrayList<>();
		return lstWorkflowState;
	}

	public void setLstWorkflowState(List<String> lstWorkflowState) {
		this.lstWorkflowState = lstWorkflowState;
	}

	public FetchStatesWorker(IProjectArea projectArea, String workItemTypeId) {
		this.projectArea = projectArea;
		this.workItemTypeId = workItemTypeId;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		boolean isValid = false;
		try {
			publish(Status.INFO.toString() + "@" + "Please wait as we retrieve the states for the selected work item type.");
			WorkItemController workItemController = new WorkItemController();
			List<String> workFlowState = workItemController.getWorkFlowState(projectArea, workItemTypeId);
			setLstWorkflowState(workFlowState);
			if(workFlowState.size() > 0)
				isValid = true;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return isValid;
	}
}
