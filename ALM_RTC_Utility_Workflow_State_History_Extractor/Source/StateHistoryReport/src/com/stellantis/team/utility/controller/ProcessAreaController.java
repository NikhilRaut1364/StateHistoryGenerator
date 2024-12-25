package com.stellantis.team.utility.controller;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.team.process.client.IProcessClientService;
import com.ibm.team.process.client.IProcessItemService;
import com.ibm.team.process.common.IProcessArea;
import com.ibm.team.process.common.IProjectArea;
import com.ibm.team.repository.common.TeamRepositoryException;
import com.stellantis.team.utility.model.TeamRepositoryInstance;
import com.stellantis.team.utility.utils.CommonUtils;
import com.stellantis.team.utility.utils.CustomLogger;

public class ProcessAreaController {
	private TeamRepositoryInstance teamRepositoryInstance;
	
	public ProcessAreaController() {
		this.teamRepositoryInstance = TeamRepositoryInstance.getInstance();
	}
	
	@SuppressWarnings("rawtypes")
	public Map<Object, String> fetchAllProjectAreas() {
		Map<Object, String> mapProcessArea = new HashMap<>();
		try {
			IProcessItemService itemService = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
			List findAllProjectAreas = itemService.findAllProjectAreas(null, null);
			for (Object object : findAllProjectAreas) {
				if (object instanceof IProjectArea) {
					IProjectArea proArea = (IProjectArea) object;
					if(!proArea.isArchived()){
						mapProcessArea.put(proArea, proArea.getName());
					}
				}
			}

			return CommonUtils.sortValueSetOfMapOfObject(mapProcessArea);
		} catch (Exception e) {
			CustomLogger.logException(e);
		}
		return mapProcessArea;
	}
	
	public IProcessArea getProjectArea(String projectAreaName){
		try {
			IProcessClientService processClientService = (IProcessClientService) teamRepositoryInstance.getRepo()
					.getClientLibrary(IProcessClientService.class);
			IProcessArea projectAreas = processClientService.findProcessArea(URI.create(URLEncoder.encode(projectAreaName, "UTF-8").replaceAll("\\+", "%20")), null, teamRepositoryInstance.getMonitor());
			return projectAreas;
		} catch (UnsupportedEncodingException e) {
			CustomLogger.logException(e);
		} catch (TeamRepositoryException e) {
			CustomLogger.logException(e);
		}
		return null;
	}
	
	@SuppressWarnings("rawtypes")
	public IProjectArea getProjectAreas(String projectAreaName)
			throws TeamRepositoryException {
		IProcessItemService service = (IProcessItemService) teamRepositoryInstance.getRepo().getClientLibrary(IProcessItemService.class);
		IProjectArea projectArea = null;
		List areas = service.findAllProjectAreas(IProcessClientService.ALL_PROPERTIES, teamRepositoryInstance.getMonitor());
		for (Object proArea : areas) {
			if (proArea instanceof IProjectArea) {
				projectArea = (IProjectArea) proArea;
				if (projectArea.getName().equals(projectAreaName)) {
					return projectArea;
				}
			}
		}
		return projectArea;
	}
}
