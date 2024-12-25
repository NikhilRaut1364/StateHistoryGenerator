package com.stellantis.team.utility.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.client.ITeamRepository;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IQueryClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.common.IQueryCommon;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.query.IQueryDescriptor;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;
import com.ibm.team.workitem.common.query.QueryTypes;
import com.stellantis.team.utility.model.DataDictionary;
import com.stellantis.team.utility.model.TeamRepositoryInstance;
import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;

public class QueryController {
	private TeamRepositoryInstance teamRepositoryInstance;

	public QueryController() {
		teamRepositoryInstance = TeamRepositoryInstance.getInstance();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, String> fetchQueryAssignedToSelectedProjectArea(IProjectArea projectArea) {
		CustomLogger.logMessage("fetchQueryAssignedToSelectedProjectArea");
		Map<String, String> hmapQueryNameAndId = new TreeMap<>();
		try {
			ProcessAreaController processAreaController = new ProcessAreaController();
			IProjectAreaHandle projectAreaHandle = (IProjectAreaHandle) projectArea.getItemHandle();
			IWorkItemClient client = (IWorkItemClient) teamRepositoryInstance.getRepo()
					.getClientLibrary(IWorkItemClient.class);
			IQueryClient queryClient = client.getQueryClient();
			IQueryCommon iQueryCommon = (IQueryCommon) teamRepositoryInstance.getRepo()
					.getClientLibrary(IQueryCommon.class);

			List sharingTargets = new ArrayList<>();
			sharingTargets.add(processAreaController.getProjectArea(projectArea.getName()));

			List sharedQueryList = getSharedQueryList(queryClient, iQueryCommon, projectAreaHandle, sharingTargets);
			if (sharedQueryList != null) {
				for (Object object : sharedQueryList) {
					if (object instanceof IQueryDescriptor) {
						hmapQueryNameAndId.put(((IQueryDescriptor) object).getItemId().getUuidValue(),
								((IQueryDescriptor) object).getName());
					}
				} 
			}
			List personalQueryList = getPersonalQueryList(teamRepositoryInstance.getRepo(), queryClient, iQueryCommon,
					projectAreaHandle);
			if (personalQueryList != null) {
				for (Object object : personalQueryList) {
					if (object instanceof IQueryDescriptor) {
						hmapQueryNameAndId.put(((IQueryDescriptor) object).getItemId().getUuidValue(),
								((IQueryDescriptor) object).getName());

					}
				} 
			}
			return CommonUtils.sortValueSetOfMap(hmapQueryNameAndId);

		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return hmapQueryNameAndId;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List getSharedQueryList(IQueryClient queryClient, IQueryCommon iQueryCommon,
			IProjectAreaHandle projectAreaHandle, List sharingTargets) {
		CustomLogger.logMessage("getSharedQueryList");
		List lstSharedQueries = new ArrayList<>();
		try {
			List lstAllSharedQueries = queryClient.findSharedQueries(projectAreaHandle, sharingTargets,
					QueryTypes.WORK_ITEM_QUERY, IQueryDescriptor.FULL_PROFILE, teamRepositoryInstance.getMonitor());
			for (Object object : lstAllSharedQueries) {
				if (!lstSharedQueries.contains(object)) {
					lstSharedQueries.add(object);
				}
			}
			return lstSharedQueries;
		} catch (TeamRepositoryException e) {
			CustomLogger.logException(e);
		}
		return lstSharedQueries;
	}

	@SuppressWarnings("rawtypes")
	private List getPersonalQueryList(ITeamRepository repo, IQueryClient queryClient, IQueryCommon iQueryCommon,
			IProjectAreaHandle projectAreaHandle) {
		CustomLogger.logMessage("getPersonalQueryList");
		try {
			return queryClient.findPersonalQueries(projectAreaHandle, repo.loggedInContributor(),
					QueryTypes.WORK_ITEM_QUERY, IQueryDescriptor.FULL_PROFILE, teamRepositoryInstance.getMonitor());
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return null;
	}
	
	public Map<String, String> getWorkItemTypeFromQuery(IProjectArea projectArea, String queryUUID){
		CustomLogger.logMessage("getWorkItemFromQuery");
		Map<String, String> mapWorkItemType = new TreeMap<>();
		try {
			ProcessAreaController processAreaController = new ProcessAreaController();
			IProjectAreaHandle projectAreaHandle = (IProjectAreaHandle) projectArea.getItemHandle();
			IWorkItemClient client = (IWorkItemClient) teamRepositoryInstance.getRepo()
					.getClientLibrary(IWorkItemClient.class);
			IQueryClient queryClient = client.getQueryClient();
			IQueryCommon iQueryCommon = (IQueryCommon) teamRepositoryInstance.getRepo()
					.getClientLibrary(IQueryCommon.class);
			
			List<IWorkItemType> allWorkItemTypes = client.findWorkItemTypes(projectAreaHandle,
					teamRepositoryInstance.getMonitor());
			
			IQueryDescriptor queryDescriptor = getSharedQueryDescriptor(processAreaController, queryClient,
					iQueryCommon, projectAreaHandle, projectArea.getName(), queryUUID);
			if (queryDescriptor == null) {
				queryDescriptor = getPersonalQueryDescriptor(processAreaController, queryClient, iQueryCommon,
						projectAreaHandle, projectArea.getName(), queryUUID);
			}
			
			if (queryDescriptor != null){
				DataDictionary instance = DataDictionary.getInstance();
				instance.setQueryDescriptor(queryDescriptor);
				IQueryResult<IResolvedResult<IWorkItem>> resolvedQueryResults = queryClient
						.getResolvedQueryResults(queryDescriptor, IWorkItem.SMALL_PROFILE);
				if (resolvedQueryResults.getResultSize(teamRepositoryInstance.getMonitor()).getTotal() > 0){
					while (resolvedQueryResults.hasNext(teamRepositoryInstance.getMonitor())){
						IResolvedResult<IWorkItem> next = resolvedQueryResults
								.next(teamRepositoryInstance.getMonitor());
						IWorkItem item = (IWorkItem) client.getAuditableCommon().resolveAuditable(next.getItem(),
								IWorkItem.FULL_PROFILE, teamRepositoryInstance.getMonitor()).getWorkingCopy();
						for (IWorkItemType iWorkItemType : allWorkItemTypes){
							if(item.getWorkItemType().equals(iWorkItemType.getIdentifier())){
								if(!mapWorkItemType.containsKey(item.getWorkItemType())){
									mapWorkItemType.put(iWorkItemType.getIdentifier(), iWorkItemType.getDisplayName());
								}
							}
						}
					}
				}
			}
			return CommonUtils.sortValueSetOfMap(mapWorkItemType);
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return mapWorkItemType;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IQueryDescriptor getSharedQueryDescriptor(ProcessAreaController processAreaController,
			IQueryClient queryClient, IQueryCommon iQueryCommon, IProjectAreaHandle projectAreaHandle,
			String projectAreaName, String queryUUID) throws TeamRepositoryException {
		CustomLogger.logMessage("getSharedQueryDescriptor");
		List sharingTargets = new ArrayList<>();
		sharingTargets.add(processAreaController.getProjectArea(projectAreaName));

		List sharedQueryList = getSharedQueryList(queryClient, iQueryCommon, projectAreaHandle, sharingTargets);
		for (Object object : sharedQueryList) {
			if (object instanceof IQueryDescriptor) {
				if (((IQueryDescriptor) object).getItemId().getUuidValue().equals(queryUUID)) {
					return ((IQueryDescriptor) object);
				}
			}
		}
		return null;
	}

	@SuppressWarnings("rawtypes")
	private IQueryDescriptor getPersonalQueryDescriptor(ProcessAreaController processAreaController,
			IQueryClient queryClient, IQueryCommon iQueryCommon, IProjectAreaHandle projectAreaHandle,
			String projectAreaName, String queryUUID) {
		CustomLogger.logMessage("getPersonalQueryDescriptor");
		List personalQueryList = getPersonalQueryList(teamRepositoryInstance.getRepo(), queryClient, iQueryCommon,
				projectAreaHandle);
		for (Object object : personalQueryList) {
			if (object instanceof IQueryDescriptor) {
				if (((IQueryDescriptor) object).getItemId().getUuidValue().equals(queryUUID)) {
					return ((IQueryDescriptor) object);
				}
			}
		}
		return null;
	}
}
