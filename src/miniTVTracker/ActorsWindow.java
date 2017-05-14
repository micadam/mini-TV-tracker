package miniTVTracker;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class ActorsWindow extends JDialog {
	private int showID;
	private SQLHelper sqlHelper;
	private JTable table;
	public ActorsWindow(SQLHelper sqlHelper, int showID) {
		
		this.showID = showID;
		this.sqlHelper = sqlHelper;
		
		table = new JTable();
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null},
			},
			new String[] {
				"Name", "Role"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		
		this.setSize(320, 240);
		refreshActors();
		this.setVisible(true);
		this.setTitle(sqlHelper.getTitleForShowID(showID) + " - Actors");
	}
	
	private void refreshActors() {
		System.out.println("Looking for actors for the show ID " + showID);
		ResultSet resultSet = sqlHelper.getActorsForShow(showID);

		while(table.getRowCount() > 0) {
			((DefaultTableModel)table.getModel()).removeRow(0);
		}
		try {
			int columns = table.getModel().getColumnCount();
			while(resultSet.next() != false) {
				Object[] newRow = new Object[columns];
				newRow[0] = resultSet.getString("nazwisko");
				newRow[1] = resultSet.getString("rola");
				((DefaultTableModel)table.getModel()).insertRow(resultSet.getRow() - 1, newRow);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
