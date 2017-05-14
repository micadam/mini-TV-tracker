package miniTVTracker;

import java.awt.BorderLayout;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class LatestEpisodesWindow extends JDialog {
	private JTable table;
	
	public LatestEpisodesWindow(SQLHelper sqlHelper, int userID) {
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();
		table.setModel(new DefaultTableModel(
			new Object[][] {
			},
			new String[] {
				"Show", "Episode title", "Airdate"
			}
		) {
			boolean[] columnEditables = new boolean[] {
				false, false, false
			};
			public boolean isCellEditable(int row, int column) {
				return columnEditables[column];
			}
		});
		table.getColumnModel().getColumn(1).setPreferredWidth(158);
		
		ResultSet resultSet = sqlHelper.getLatestEpisodes(userID);
		try {
			while(resultSet.next() != false) {
				Object[] newRow = new Object[3];
				newRow[0] = resultSet.getString("showTitle");
				newRow[1] = resultSet.getString("epTitle");
				newRow[2] = resultSet.getString("airdate");
				
				((DefaultTableModel)table.getModel()).insertRow(resultSet.getRow() - 1, newRow);
				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
		scrollPane.setViewportView(table);
		
		this.setSize(800, 480);
		this.setVisible(true);

	}

}
