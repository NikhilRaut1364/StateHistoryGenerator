package com.stellantis.team.utility.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.client.WorkItemWorkingCopy;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;
import com.stellantis.team.utility.model.TeamRepositoryInstance;
import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;

public class WorkItemController {
	private TeamRepositoryInstance teamRepositoryInstance;

	public WorkItemController() {
		teamRepositoryInstance = TeamRepositoryInstance.getInstance();
	}

	public Map<String, String> fetchWorkItemTypeOfSpecificProjectArea(IProjectArea projectArea) {
		Map<String, String> mapWorkItemType = new TreeMap<>();
		try {
			IProjectAreaHandle projectAreaHandle = (IProjectAreaHandle) projectArea.getItemHandle();
			IWorkItemClient client = (IWorkItemClient) teamRepositoryInstance.getRepo()
					.getClientLibrary(IWorkItemClient.class);
			List<IWorkItemType> findWorkItemTypes = client.findWorkItemTypes(projectAreaHandle,
					teamRepositoryInstance.getMonitor());
			for (IWorkItemType iWorkItemType : findWorkItemTypes) {
				mapWorkItemType.put(iWorkItemType.getIdentifier(), iWorkItemType.getDisplayName());
			}
			return CommonUtils.sortValueSetOfMap(mapWorkItemType);

		} catch (TeamRepositoryException e) {
			CustomLogger.logException(e);
		}
		return mapWorkItemType;
	}

	public List<String> getWorkFlowState(IProjectArea projectArea, String workItemTypeId) {
		List<String> workItemStates = new ArrayList<>();
		try {
			IWorkItemClient service = (IWorkItemClient) teamRepositoryInstance.getRepo()
					.getClientLibrary(IWorkItemClient.class);
			IProjectAreaHandle projectAreaHandle = (IProjectAreaHandle) projectArea.getItemHandle();
			IWorkItemType workItemTypeObj = service.findWorkItemType(projectAreaHandle, workItemTypeId,
					teamRepositoryInstance.getMonitor());
			IWorkItemHandle workItemHandle = service.getWorkItemWorkingCopyManager().connectNew(workItemTypeObj,
					teamRepositoryInstance.getMonitor());
			WorkItemWorkingCopy wc = service.getWorkItemWorkingCopyManager().getWorkingCopy(workItemHandle);
			IWorkItem workItem = wc.getWorkItem();
			IWorkflowInfo workFlowInfo = service.findWorkflowInfo(workItem, teamRepositoryInstance.getMonitor());
			Identifier<IState>[] allStateIds = workFlowInfo.getAllStateIds();
			workItemStates = getStateNames(workFlowInfo, allStateIds);
			Collections.sort(workItemStates);
			return workItemStates;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return workItemStates;
	}

	private List<String> getStateNames(IWorkflowInfo workflowInfo, Identifier<IState>[] allStateIds) {
		List<String> workItemStates = new ArrayList<>(0);
		for (Identifier<IState> identifier : allStateIds) {
			String stateName = workflowInfo.getStateName(identifier);
			workItemStates.add(stateName);
		}
		return workItemStates;
	}
}
