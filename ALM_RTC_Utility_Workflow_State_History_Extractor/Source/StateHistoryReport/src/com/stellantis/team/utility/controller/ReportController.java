package com.stellantis.team.utility.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.process.common.IProjectAreaHandle;
import com.ibm.team.repository.client.IItemManager;
import com.ibm.team.repository.common.IContributor;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.ibm.team.workitem.client.IQueryClient;
import com.ibm.team.workitem.client.IWorkItemClient;
import com.ibm.team.workitem.common.IAuditableCommon;
import com.ibm.team.workitem.common.IQueryCommon;
import com.ibm.team.workitem.common.expression.AttributeExpression;
import com.ibm.team.workitem.common.expression.Expression;
import com.ibm.team.workitem.common.expression.IQueryableAttribute;
import com.ibm.team.workitem.common.expression.QueryableAttributes;
import com.ibm.team.workitem.common.expression.Term;
import com.ibm.team.workitem.common.model.AttributeOperation;
import com.ibm.team.workitem.common.model.IState;
import com.ibm.team.workitem.common.model.IWorkItem;
import com.ibm.team.workitem.common.model.IWorkItemHandle;
import com.ibm.team.workitem.common.model.IWorkItemType;
import com.ibm.team.workitem.common.model.Identifier;
import com.ibm.team.workitem.common.query.IQueryDescriptor;
import com.ibm.team.workitem.common.query.IQueryResult;
import com.ibm.team.workitem.common.query.IResolvedResult;
import com.ibm.team.workitem.common.query.IResult;
import com.ibm.team.workitem.common.workflow.IWorkflowInfo;
import com.stellantis.team.utility.model.Choice;
import com.stellantis.team.utility.model.DataDictionary;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.model.TeamRepositoryInstance;
import com.stellantis.team.utility.model.WIHistory;
import com.stellantis.team.utility.utils.CustomLogger;
import com.stellantis.team.utility.utils.GenerateExcel;
import com.stellantis.team.utility.view.Notification;
import com.stellantis.team.utility.view.StateReportGenerator;

public class ReportController {
	private TeamRepositoryInstance teamRepositoryInstance;

	public ReportController() {
		this.teamRepositoryInstance = TeamRepositoryInstance.getInstance();
	}

	public boolean processReport(IProjectArea projectArea, String workItemTypeId, List<String> lstState,
			String filePath, String dateformat, String resultChoice) {
		try {
			IWorkItemClient wiServer = (IWorkItemClient) teamRepositoryInstance.getRepo()
					.getClientLibrary(IWorkItemClient.class);
			IAuditableCommon iAuditableCommon = (IAuditableCommon) teamRepositoryInstance.getRepo()
					.getClientLibrary(IAuditableCommon.class);
			IQueryCommon fQueryCommon = (IQueryCommon) teamRepositoryInstance.getRepo()
					.getClientLibrary(IQueryCommon.class);

			if (projectArea != null) {
				IProjectAreaHandle projectAreaHandle = (IProjectAreaHandle) projectArea.getItemHandle();
				IWorkItemType workItemTypeObj = wiServer.findWorkItemType(projectAreaHandle, workItemTypeId,
						teamRepositoryInstance.getMonitor());
				if (workItemTypeObj != null) {
					Map<String, WIHistory> history = new ConcurrentHashMap<>(0);
					if(resultChoice.equals(Choice.WORKITEM_TYPE.getValue())){
						processWorkItemType(wiServer, iAuditableCommon, fQueryCommon, history, projectAreaHandle,
								workItemTypeObj, lstState, workItemTypeId);
					} else if(resultChoice.equals(Choice.WORKITEM_QUERY.getValue())){
						processWorkItemTypeFromQuery(wiServer, iAuditableCommon, fQueryCommon, history, projectAreaHandle,
								workItemTypeObj, lstState, workItemTypeId);
					}
					
					Runtime.getRuntime().gc();

					GenerateExcel generateExcel = new GenerateExcel(dateformat);
					boolean isFileReady = false;
					if(!StateReportGenerator.isCancelled){
						isFileReady = generateExcel.writeToFile(filePath, history, lstState);
					}
					return isFileReady;
				}
			}
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return false;
	}

	@SuppressWarnings("unused")
	private List<String> getStateNames(IWorkflowInfo workflowInfo, Identifier<IState>[] allStateIds) {
		List<String> workItemStates = new ArrayList<>(0);
		for (Identifier<IState> identifier : allStateIds) {
			String stateName = workflowInfo.getStateName(identifier);
			workItemStates.add(stateName);
		}
		return workItemStates;
	}
	
	private IWorkItem processWorkItemTypeFromQuery(IWorkItemClient wiServer, IAuditableCommon iAuditableCommon,
			IQueryCommon fQueryCommon, Map<String, WIHistory> history, IProjectAreaHandle projectAreaHandle,
			IWorkItemType workItemType, List<String> lstState, String workItemTypeId){
		try {
			List<IWorkItemHandle> iWorkItems = new ArrayList<IWorkItemHandle>();
			DataDictionary dataDictionary = DataDictionary.getInstance();
			IQueryDescriptor queryDescriptor = dataDictionary.getQueryDescriptor();
			IQueryClient queryClient = wiServer.getQueryClient();
			if(queryDescriptor != null){
				IQueryResult<IResolvedResult<IWorkItem>> resolvedQueryResults = queryClient
						.getResolvedQueryResults(queryDescriptor, IWorkItem.SMALL_PROFILE);
				if (resolvedQueryResults.getResultSize(teamRepositoryInstance.getMonitor()).getTotal() > 0){
					while (resolvedQueryResults.hasNext(teamRepositoryInstance.getMonitor())){
						IResolvedResult<IWorkItem> next = resolvedQueryResults
								.next(teamRepositoryInstance.getMonitor());
						IWorkItem item = (IWorkItem) wiServer.getAuditableCommon().resolveAuditable(next.getItem(),
								IWorkItem.FULL_PROFILE, teamRepositoryInstance.getMonitor()).getWorkingCopy();
						iWorkItems.add((IWorkItemHandle)item.getItemHandle());
					}
				}
				IWorkItem result = setHistoricalData(wiServer, history, workItemType, iWorkItems, lstState, workItemTypeId);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "deprecation", "unchecked" })
	private IWorkItem processWorkItemType(IWorkItemClient wiServer, IAuditableCommon iAuditableCommon,
			IQueryCommon fQueryCommon, Map<String, WIHistory> history, IProjectAreaHandle projectAreaHandle,
			IWorkItemType workItemType, List<String> lstState, String workItemTypeId) {
		try {
			IQueryableAttribute attribute = QueryableAttributes.getFactory(IWorkItem.ITEM_TYPE).findAttribute(
					projectAreaHandle, IWorkItem.PROJECT_AREA_PROPERTY, iAuditableCommon,
					teamRepositoryInstance.getMonitor());
			IQueryableAttribute type = QueryableAttributes.getFactory(IWorkItem.ITEM_TYPE).findAttribute(
					projectAreaHandle, IWorkItem.TYPE_PROPERTY, iAuditableCommon, teamRepositoryInstance.getMonitor());

			Expression inProjectArea = new AttributeExpression(attribute, AttributeOperation.EQUALS, projectAreaHandle);
			Expression isType = new AttributeExpression(type, AttributeOperation.EQUALS, workItemType.getIdentifier());

			Term typeinProjectArea = new Term(Term.Operator.AND);
			typeinProjectArea.add(inProjectArea);
			typeinProjectArea.add(isType);

			IQueryResult queryResult = fQueryCommon.getExpressionResults(projectAreaHandle, typeinProjectArea);
			queryResult.setLimit(Integer.MAX_VALUE);
			
			int resultSize = queryResult.getTotalSize(teamRepositoryInstance.getMonitor());
			List<IWorkItemHandle> iWorkItems = getWorkItemHandles(wiServer, queryResult, resultSize);
			IWorkItem result = setHistoricalData(wiServer, history, workItemType, iWorkItems, lstState, workItemTypeId);
			return result;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return null;
	}

	private List<IWorkItemHandle> getWorkItemHandles(IWorkItemClient wiServer,
			IQueryResult<IResolvedResult<IWorkItem>> queryResult, int resultSize) throws TeamRepositoryException {
		List<IWorkItemHandle> iWorkItems = new ArrayList<>(0);
		if (resultSize > 0) {

			int i = 0;

			while (queryResult.hasNext(teamRepositoryInstance.getMonitor())) {
				if (i % 50 == 0) {
					Runtime.getRuntime().gc();
				}
				collectWorkItem(wiServer, queryResult, iWorkItems);
				++i;
			}
		}
		return iWorkItems;
	}

	private void collectWorkItem(IWorkItemClient wiServer, IQueryResult<IResolvedResult<IWorkItem>> queryResult,
			List<IWorkItemHandle> iWorkItems) throws TeamRepositoryException {
		IResult result = (IResult) queryResult.next(teamRepositoryInstance.getMonitor());
		if (result != null) {
			IWorkItemHandle resultHandle = (IWorkItemHandle) result.getItem();
			if (resultHandle != null) {
				iWorkItems.add(resultHandle);
			}
		}
	}

	private IWorkItem setHistoricalData(IWorkItemClient wiServer, Map<String, WIHistory> history,
			IWorkItemType workItemType, List<IWorkItemHandle> iWorkItems, List<String> lstState, String workItemTypeId) {
		try {
			// Use available processors for the pool
			int numThreads = Runtime.getRuntime().availableProcessors(); 
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			CustomLogger.logMessage("Start time: " + LocalDateTime.now().format(formatter));
			ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
			List<Future<Void>> futures = new ArrayList<>();
//			IWorkflowInfo workflowInfo = null;
			IWorkItem result = null;
			int counter = 1;
			int totalListSize = iWorkItems.size();
			for (IWorkItemHandle iWorkItemHandle : iWorkItems) {
				if(StateReportGenerator.isCancelled){
					Notification.addMessage(Status.INFO.toString(),
							"Report generation has been halted.");
					CustomLogger.logMessage("Operation Terminated");
					CustomLogger.logMessage("End time: " + LocalDateTime.now().format(formatter));
					StateReportGenerator.reportProgress.setValue(100);
					break;
				}
					
				IWorkItem workItem = (IWorkItem) wiServer.getAuditableCommon().resolveAuditable(iWorkItemHandle,
						IWorkItem.FULL_PROFILE, null);
				if (workItemTypeId.equals(workItem.getWorkItemType())) {
					IWorkflowInfo workflowInfo = wiServer.findWorkflowInfo(workItem, null);
					String currentState = getStatus(workflowInfo, workItem);
					if (lstState.contains(currentState)) {
						final int count = counter;
						Callable<Void> task = () -> {
							CustomLogger.logMessage(
									count + " : " + workItem.getHTMLSummary().getPlainText() + " = " + currentState);
							setHistory(wiServer, history, workItem, teamRepositoryInstance.getRepo().itemManager(),
									workflowInfo, workItemType.getDisplayName());
							return null;
						};
						futures.add(executorService.submit(task));
						result = workItem;
						float progress = (float) count / totalListSize * 100;
						StateReportGenerator.reportProgress.setValue((int) progress);
						counter++;
					} 
				}
			}
			for (Future<Void> future : futures) {
				future.get(); // This will block until the task is complete
			}
			executorService.shutdown();
			CustomLogger.logMessage("End time: " + LocalDateTime.now().format(formatter));
			return result;
		} catch (InterruptedException e) {
			CustomLogger.logException(e);
		} catch (ExecutionException e) {
			CustomLogger.logException(e);
		} catch (TeamRepositoryException e) {
			CustomLogger.logException(e);
		}
		return null;
	}

	// private void getEntireHistory(IWorkItemClient wiServer,
	// List<IWorkItemHandle> iWorkItems) {
	// try {
	// List<List<IWorkItemHandle>> sublists = splitList(iWorkItems, 100);
	//
	// for (List<IWorkItemHandle> sublist : sublists) {
	// StringBuffer stringBuffer = new StringBuffer("id='");
	// for (IWorkItemHandle iWorkItemHandle : sublist) {
	// IWorkItem workItem = (IWorkItem)
	// wiServer.getAuditableCommon().resolveAuditable(iWorkItemHandle,
	// IWorkItem.FULL_PROFILE, null);
	// stringBuffer.append(workItem.getId()).append("' or id='");
	// }
	// if (stringBuffer.length() > 0) {
	// stringBuffer.setLength(stringBuffer.length() - " or id='".length());
	// }
	// System.out.println(stringBuffer.toString());
	// getHistoryFromAPI(stringBuffer.toString());
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private void getHistoryFromAPI(String workitemIds) {
	// try {
	// String url = teamRepositoryInstance.getServerURL() +
	// "/rpt/repository/workitem?fields=workitem/workItem["
	// + URLEncoder.encode(workitemIds, "UTF-8")
	// + "]/" +
	// URLEncoder.encode("(id|(type/name)|(state/name)|(owner/name)|stateTransitions/(transitionDate|targetStateId))",
	// "UTF-8");
	//// String encode = URLEncoder.encode(url, "UTF-8");
	// HttpGet rootServiceDoc = new HttpGet(url);
	// rootServiceDoc.addHeader("Accept", "application/rdf+xml");
	// HttpClient httpclient = new DefaultHttpClient();
	// HttpUtils.setupLazySSLSupport(httpclient);
	// HttpUtils.doAuth(teamRepositoryInstance.getUsername(),
	// teamRepositoryInstance.getPassword(), httpclient,
	// teamRepositoryInstance.getServerURL());
	//
	// HttpResponse serviceURLResponse =
	// HttpUtils.sendGetForSecureDocument(teamRepositoryInstance.getServerURL(),
	// rootServiceDoc, teamRepositoryInstance.getUsername(),
	// teamRepositoryInstance.getPassword(), httpclient);
	// if (serviceURLResponse.getStatusLine().getStatusCode() == 200){
	// HttpEntity entity = serviceURLResponse.getEntity();
	// String responseString = EntityUtils.toString(entity);
	// System.out.println(responseString);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private <T> List<List<T>> splitList(List<T> originalList, int batchSize)
	// {
	// List<List<T>> sublists = new ArrayList<>();
	//
	// for (int i = 0; i < originalList.size(); i += batchSize) {
	// int endIndex = Math.min(i + batchSize, originalList.size());
	// List<T> sublist = originalList.subList(i, endIndex);
	// sublists.add(new ArrayList<>(sublist));
	// }
	//
	// return sublists;
	// }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void setHistory(IWorkItemClient wiServer, Map<String, WIHistory> history, IWorkItem workItem,
			IItemManager iItemManager, IWorkflowInfo workflowInfo, String type) throws TeamRepositoryException {
		String uuidValue = workItem.getItemId().getUuidValue();
		history.put(uuidValue, new WIHistory());
		history.get(uuidValue).setId(workItem.getId()).setOwnerName(getOwner(iItemManager, workItem))
				.setStatus(getStatus(workflowInfo, workItem))
		/*
		 * .setSummary(setTheSummary(workItem.getHTMLSummary().getPlainText()))
		 */;
		history.get(uuidValue).setType(type);
		List allStateHandles = iItemManager.fetchAllStateHandles(workItem, null);
		if (allStateHandles != null && !allStateHandles.isEmpty()) {

			List<IWorkItem> fetchCompleteStates = (List<IWorkItem>) iItemManager.fetchCompleteStates(allStateHandles,
					null);
			WIHistory WIHistory = history.get(uuidValue);
			if (WIHistory != null) {
				Map<String, Date> firstDateOfStatusMap = WIHistory.getFirstDateOfStatusMap();
				StringBuilder historyText = WIHistory.getHistory();
				String status = null;
				int wiC = fetchCompleteStates.size() - 1;
				do {
					IWorkItem wi = fetchCompleteStates.get(wiC);
					String currentState = appendChangeTrailToWIHistory(workflowInfo, firstDateOfStatusMap, historyText,
							status, wi);
					status = currentState;
					wiC = wiC - 1;
				} while (wiC >= 0);
			}

		}

	}

	private String appendChangeTrailToWIHistory(IWorkflowInfo workflowInfo, Map<String, Date> firstDateOfStatusMap,
			StringBuilder historyText, String status, IWorkItem wi) throws TeamRepositoryException {
		String currentState = getStatus(workflowInfo, wi);
		if (currentState != null) {
			if (status == null || !currentState.equalsIgnoreCase(status)) {

				if (!firstDateOfStatusMap.containsKey(currentState)) {
					firstDateOfStatusMap.put(currentState, wi.modified());
				}

				appendHistoryText(historyText, getHistoryText(wi, currentState));
			}
		}
		return currentState;
	}

	private String getStatus(IWorkflowInfo workflowInfo, IWorkItem workItem) throws TeamRepositoryException {
		return workflowInfo.getStateName(workItem.getState2());
	}

	private String getHistoryText(IWorkItem wi, String currentState) {
		return wi.modified() != null
				? (new StringBuilder()).append(wi.modified()).append("_").append(currentState).toString() : null;
	}

	private void appendHistoryText(StringBuilder historyText, String historyTextOfVersion) {
		if (historyTextOfVersion != null && !historyTextOfVersion.isEmpty()) {
			if (historyText.length() > 0) {
				historyText.append(";");
			}
			historyText.append(historyTextOfVersion);
		}

	}

	private String getOwner(IItemManager iItemManager, IWorkItem workItem) throws TeamRepositoryException {
		IContributor ownedBy = (IContributor) iItemManager.fetchPartialItem(workItem.getOwner(), IItemManager.DEFAULT,
				Arrays.asList(IContributor.NAME_PROPERTY), null);
		if (ownedBy != null) {
			return ownedBy.getName();
		}
		return null;
	}
}
