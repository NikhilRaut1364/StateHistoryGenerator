package com.stellantis.team.utility.service;

import java.util.List;

import com.ibm.team.process.common.IProjectArea;
import com.stellantis.team.utility.controller.ReportController;
import com.stellantis.team.utility.model.Status;
import com.stellantis.team.utility.utils.CustomLogger;

public class GenerateReportWorker extends UtilitySwingWorker{

	private IProjectArea projectArea;
	private String workItemTypeId;
	private List<String> lstStates;
	private String filePath;
	private String dateformat;
	private String resultChoice;

	public GenerateReportWorker(IProjectArea projectArea, String workItemTypeId, List<String> lstStates, String filePath, String dateformat, String resultChoice) {
		this.projectArea = projectArea;
		this.workItemTypeId = workItemTypeId;
		this.lstStates = lstStates;
		this.filePath = filePath;
		this.dateformat = dateformat;
		this.resultChoice = resultChoice;
	}
	
	@Override
	protected Boolean doInBackground() throws Exception {
		try {
			publish(Status.INFO.toString() + "@" + "Please Wait!! while the report is generating");
			ReportController reportController = new ReportController();
			boolean processReport = reportController.processReport(projectArea, workItemTypeId, lstStates, filePath, dateformat, resultChoice);
			return processReport;
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return false;
	}
}
