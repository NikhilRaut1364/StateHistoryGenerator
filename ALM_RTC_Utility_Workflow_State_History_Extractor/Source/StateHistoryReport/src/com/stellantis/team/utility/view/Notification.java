package com.stellantis.team.utility.view;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.stellantis.team.utility.utils.CommonUtils;

@SuppressWarnings("serial")
public class Notification extends JTable {
	private static DefaultTableModel model = new DefaultTableModel();

	@Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
	
	public Notification() {
		CommonUtils.customizeTableRowHeight(Notification.this);
		CommonUtils.customizeTableHeaders(getTableHeader());
		CommonUtils.setTableHeaderFontToBold(getTableHeader());
		model.addColumn("Date");
		model.addColumn("Status");
		model.addColumn("Message");

		setModel(model);

		TableColumnModel columnModel = getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(10); // Date column width
		columnModel.getColumn(1).setPreferredWidth(10);
		columnModel.getColumn(2).setPreferredWidth(900);

		setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
	}

	public static void addMessage(String status, String message) {
		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String formattedDateTime = currentDateTime.format(formatter);
		DefaultTableModel model1 = (DefaultTableModel) model;
		model1.insertRow(0, new Object[] { formattedDateTime, status, message });
		model1.fireTableDataChanged();
	}
}
