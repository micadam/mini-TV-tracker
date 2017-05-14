package miniTVTracker;
import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ToolTipManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class EpisodesWindow extends JDialog {
	private JTable table;
	private SQLHelper sqlHelper;
	private int showID;
	private int userID;
	
	public EpisodesWindow(SQLHelper sqlHelper, int showID, int userID) {
		
		this.sqlHelper = sqlHelper;
		this.showID = showID;
		this.userID = userID;
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable() {
			@Override
			public String getToolTipText(MouseEvent e) {
				int row = rowAtPoint(e.getPoint());
				int column = columnAtPoint(e.getPoint());
				
				Object value = getValueAt(row, column);
				if(value == null) {
					return null;
				}
				String raw = value.toString();
				String tooltip = "<html>";
				int i = 0;
				while(i < raw.length()) {
					int substrEnd = Math.min(i + 100, raw.length());
					while(substrEnd < raw.length() && raw.charAt(substrEnd) != ' ') {
						substrEnd++;
					}
					String fragment = raw.substring(i, substrEnd);
					tooltip += fragment + "<br>";
					i = substrEnd;
				}
				tooltip += "</html>";
				return tooltip;
			}
		};

		ToolTipManager.sharedInstance().setDismissDelay(1000000);
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{null, null, null, null, null},
			},
			new String[] {
				"Season no.", "Episode no.", "Title", "Description", "airdate", "Watched?", "id", "rating"
			}
		) {
			Class[] columnTypes = new Class[] {
				Integer.class, Integer.class, String.class, String.class, String.class, Boolean.class, Integer.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			public boolean isCellEditable(int row, int column) {
				return column == 5;
			}
		});
		
		
		table.getModel().addTableModelListener(new TableModelListener() {
			
			@Override
			public void tableChanged(TableModelEvent e) {
				if(e.getColumn() == 5) {
					boolean newValue = (boolean)((TableModel)e.getSource()).getValueAt(e.getFirstRow(), e.getColumn());
					if(newValue == true) {
						episodeWatched((int)table.getModel().getValueAt(e.getFirstRow(), 6));
					} else {
						episodeUnwatched((int)table.getModel().getValueAt(e.getFirstRow(), 6));
					}
				}
				
			}
		});
		TableColumnModel tcm = table.getColumnModel();
		tcm.removeColumn(tcm.getColumn(6));
		table.getColumnModel().getColumn(1).setPreferredWidth(87);
		table.getColumnModel().getColumn(2).setPreferredWidth(170);
		table.getColumnModel().getColumn(3).setPreferredWidth(220);
		scrollPane.setViewportView(table);
		
		this.setSize(800, 480);
		refreshEpisodes();
		this.setTitle(sqlHelper.getTitleForShowID(showID) + "- Episodes");
		this.setVisible(true);
		
	}
	
	private void refreshEpisodes() {
		ResultSet resultSet = sqlHelper.getEpisodesForShow(showID, userID);
		
		while(table.getRowCount() > 0) {
			((DefaultTableModel)table.getModel()).removeRow(0);
		}
		try {
			int columns = table.getModel().getColumnCount();
			while(resultSet.next() != false) {
				Object[] newRow = new Object[columns];
				newRow[0] = resultSet.getInt("numerSezonu");
				newRow[1] = resultSet.getInt("numerWSezonie");
				newRow[2] = resultSet.getString("tytul");
				newRow[3] = resultSet.getString("opis");
				newRow[4] = resultSet.getString("dataPremiery");
				newRow[5] = resultSet.getBoolean("watched");
				newRow[6] = resultSet.getInt("odcinekID");
				newRow[7] = resultSet.getInt("ocenaUzytkownika");
				((DefaultTableModel)table.getModel()).insertRow(resultSet.getRow() - 1, newRow);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void episodeWatched(int epID) {
		Object[] options = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		int rating = (int)JOptionPane.showInputDialog(this, "Rate this episode", "Episode watched", JOptionPane.PLAIN_MESSAGE, null, options, 10);
		
		sqlHelper.startTransaction();
		sqlHelper.episodeWatched(epID, userID, rating);
		sqlHelper.finishTransaction();
		
		refreshEpisodes();
	}
	
	private void episodeUnwatched(int epID) {
		sqlHelper.startTransaction();
		sqlHelper.episodeUnwatched(epID, userID);
		sqlHelper.finishTransaction();
		
		refreshEpisodes();
	}
}
