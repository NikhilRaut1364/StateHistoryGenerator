package com.stellantis.team.utility.utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.stellantis.team.utility.model.WIHistory;

public class GenerateExcel {

	private static SimpleDateFormat SIMPLE_DATE_FORMAT;
	
	public GenerateExcel(String dateTimeFormat) {
		if(dateTimeFormat.length() > 0)
			SIMPLE_DATE_FORMAT = new SimpleDateFormat(dateTimeFormat);
		else
			SIMPLE_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy H:mm:ss a");
	}
	
	public boolean writeToFile(String filePath, Map<String, WIHistory> history, List<String> workItemStateNames) {
		try {
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet();
			sheet.createRow(0);
			createHeader(sheet, workItemStateNames);
			
			int rowCount = 1;
//			sheet.createRow(rowCount);
//			rowCount++;
			createContent(sheet, history, workItemStateNames, rowCount);

			File file = new File(filePath);
			workbook.write(file);
			workbook.close();

		} catch (Exception e) {
			CustomLogger.logException(e);
			return false;
		}
		return true;
	}

	private void createHeader(HSSFSheet sheet, List<String> workItemStateNames) {
		sheet.getRow(0).createCell(0).setCellValue("Id");
		sheet.getRow(0).createCell(1).setCellValue("Type");
		sheet.getRow(0).createCell(2).setCellValue("Status");
		sheet.getRow(0).createCell(3).setCellValue("Owned By");
		int cellCount = 4;
		for(int i = 0; i < workItemStateNames.size(); i++){
			sheet.getRow(0).createCell(cellCount + i).setCellValue(workItemStateNames.get(i));
		}
//		sheet.getRow(0).createCell(cellCount + workItemStateNames.size()).setCellValue("History");
	}
	
	private void createContent(HSSFSheet sheet, Map<String, WIHistory> history, List<String> workItemStateNames, int rowCount){
		for (Map.Entry<String, WIHistory> entry : history.entrySet()){
			WIHistory value = entry.getValue();
			sheet.createRow(rowCount);
			sheet.getRow(rowCount).createCell(0).setCellValue(value.getId());
			sheet.getRow(rowCount).createCell(1).setCellValue(value.getType());
			sheet.getRow(rowCount).createCell(2).setCellValue(value.getStatus());
			sheet.getRow(rowCount).createCell(3).setCellValue(value.getOwnerName());
			int cellCount = 4;
			for(int i = 0; i < workItemStateNames.size(); i++){
				String date = getDate(workItemStateNames.get(i), value);
				if(date == null) {
					date = "";
				}
				sheet.getRow(rowCount).createCell(cellCount + i).setCellValue(date);
			}
//			sheet.getRow(rowCount).createCell(cellCount + workItemStateNames.size()).setCellValue(value.getHistory().toString());
			rowCount++;
		}
	}
	
	private String getDate(String string, WIHistory value) {
		String result = "";
		Map<String, Date> firstDateOfStatusMap = value.getFirstDateOfStatusMap();
		if(firstDateOfStatusMap != null) {
			Date date = firstDateOfStatusMap.get(string);
			if(date != null) {
				result = SIMPLE_DATE_FORMAT.format(date);
			}
		}
		return result;
	}

}
