package com.stellantis.team.utility.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.QueryController;
import com.stellantis.team.utility.model.KeyValuePair;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.utils.CustomLogger;


public class FetchQueryWorker extends UtilitySwingWorker{
	private IProjectArea projectArea;
	private List<KeyValuePair> lstQueryAssignedToSelectedProjectArea;
	
	public List<KeyValuePair> getLstQueryAssignedToSelectedProjectArea() {
		if(lstQueryAssignedToSelectedProjectArea == null)
			lstQueryAssignedToSelectedProjectArea = new ArrayList<KeyValuePair>();
		return lstQueryAssignedToSelectedProjectArea;
	}
	
	public void setLstQueryAssignedToSelectedProjectArea(List<KeyValuePair> lstQueryAssignedToSelectedProjectArea) {
		this.lstQueryAssignedToSelectedProjectArea = lstQueryAssignedToSelectedProjectArea;
	}
	
	public FetchQueryWorker(IProjectArea projectArea) {
		this.projectArea = projectArea;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		try {
//			publish(Status.INFO.toString() + UtilityConstants.STATUS_SEPERATOR + UtilityConstants.FETCHING_QUERIES_FROM_SELECTED_PROJECT_AREA);
			publish(Status.INFO.toString() + "@" + "Please wait! we are fetching all the queries");
			QueryController queryController = new QueryController();
			Map<String, String> fetchQueryAssignedToSelectedProjectArea = queryController.fetchQueryAssignedToSelectedProjectArea(projectArea);
			for (Map.Entry<String, String> entry : fetchQueryAssignedToSelectedProjectArea.entrySet()) {
	            getLstQueryAssignedToSelectedProjectArea().add(new KeyValuePair(entry.getValue(), entry.getKey()));
	        }

			if (getLstQueryAssignedToSelectedProjectArea() != null && !getLstQueryAssignedToSelectedProjectArea().isEmpty()) {
				if (getLstQueryAssignedToSelectedProjectArea().size() > 0)
					return true;
			}
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return false;
	}
}
