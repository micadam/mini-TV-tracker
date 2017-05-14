package miniTVTracker;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import javax.swing.JButton;

public class MainWindow extends JFrame {
	
	public JTable table;
	private TVAPIHelper tvApiHelper;
	private SQLHelper sqlHelper;
	
	private String chosenUsername;
	private int chosenUserID = 0;
	private ShowsPopupMenu showsPopupMenu;
	
	public MainWindow(String username, int userID, SQLHelper sqlHelper) {
		this.chosenUsername = username;
		this.chosenUserID = userID;
		this.sqlHelper = sqlHelper;
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JButton addShowButton = new JButton("Add new show");
		menuBar.add(addShowButton);
		
		JButton refreshButton = new JButton("Refresh shows");
		menuBar.add(refreshButton);
		
		JButton latestEpisodesButton = new JButton("Show latest episodes");
		menuBar.add(latestEpisodesButton);
		addShowButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				addShow();
			}
			
		});
		refreshButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				refreshShows();				
			}
		});
		
		latestEpisodesButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				LatestEpisodesWindow latestEpisodesWindow = new LatestEpisodesWindow(sqlHelper, chosenUserID);				
			}
		});
		
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
			},
			new String[] {
				"Title", "Network", "Description", "Status", "Episodes total", "watched", "id", "avg. rating"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, String.class, String.class, String.class, Integer.class, Integer.class, Integer.class, Float.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		});
		
		TableColumnModel tcm = table.getColumnModel();
		tcm.removeColumn(tcm.getColumn(6));
		
		table.getColumnModel().getColumn(0).setPreferredWidth(180);
		table.getColumnModel().getColumn(2).setPreferredWidth(333);
		showsPopupMenu = new ShowsPopupMenu(this);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				int row = table.rowAtPoint(e.getPoint());
				if(row >= 0 && row < table.getRowCount()) {
					table.setRowSelectionInterval(row, row);
				} else {
					table.clearSelection();
				}
				if(SwingUtilities.isRightMouseButton(e) && table.getSelectedRow() != -1) {
					showsPopupMenu.show(e.getX(), e.getY());
				} else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
					EpisodesWindow episodesWindow = new EpisodesWindow(sqlHelper, (int)(table.getModel().getValueAt(table.getSelectedRow(), 6)), chosenUserID);
				}
			}
			
		});
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		
		
		this.setSize(800, 480);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle(chosenUsername + "'s shows");
		this.setVisible(true);
		JFrame frame = this;
		this.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e) {
				sqlHelper.closeConnecton();
				frame.dispose();
				
			}
		});
		tvApiHelper = new TVAPIHelper();
		
		refreshShows();
	}
	
	private void addShow() {		
		String inputTitle =  (String)JOptionPane.showInputDialog(this, "Enter the title of the show you wish to add", "Search", JOptionPane.PLAIN_MESSAGE, null, null, "");
		
		if(inputTitle == null){			
			return;
		}
		
		JSONArray array = tvApiHelper.getShowsforTitle(inputTitle);
		
		List<String> showTitles = new ArrayList<String>();
		JSONObject showToAdd;
		if(array.size() > 1) {			
			for(Object o : array){
				JSONObject jo = (JSONObject)o;
				String showTitle =  (String)(jo.get("seriesName"));
				showTitles.add(showTitle);
				
			}
			int choice = new ShowSelectionDialog(showTitles).getChoice();
			if(choice >= 0) {
				showToAdd = (JSONObject)array.get(choice);
			} else {
				return;
			}
		} else {
			showToAdd = (JSONObject)array.get(0);
		}
		//start adding show
		sqlHelper.startTransaction();
		int showID = myParseInt(showToAdd.get("id"));
		String showTitle = (String)showToAdd.get("seriesName");
		System.out.println("Adding " + showTitle);
		String network = (String)showToAdd.get("network");
		String description = (String)showToAdd.get("overview");
		String showStatus = (String)showToAdd.get("status");
		
		sqlHelper.addShow(showID, showTitle, network, description, showStatus);
		
		int pageNumber = 1;
		JSONArray episodesArray = tvApiHelper.getEpisodesPage(showID, pageNumber);
		while(episodesArray != null) {
			for(Object o : episodesArray) {
				JSONObject jo = (JSONObject)o;
				String title = (String)jo.get("episodeName");
				int numberInShow = myParseInt(jo.get("absoluteNumber"));
				int numberInSeason = myParseInt(jo.get("airedEpisodeNumber"));
				int seasonNumber = myParseInt(jo.get("airedSeason"));
				if(seasonNumber == 0)
					continue;
				String airdate = (String)jo.get("firstAired");
				if(airdate == ""){
					airdate = "NULL";
				}
				String epDescription = (String)jo.get("overview");
				
				sqlHelper.addEpisode(showID, title, numberInShow, numberInSeason, seasonNumber, airdate, epDescription);
			}
			episodesArray = tvApiHelper.getEpisodesPage(showID, ++pageNumber);

		}
		
		JSONArray actorsArray = tvApiHelper.getActorsForShow(showID);
		for(Object o : actorsArray) {
			JSONObject jo = (JSONObject)o;
			
			String actorName = (String)jo.get("name");
			String role = (String)jo.get("role");
			
			sqlHelper.addActorForShow(showID, actorName, role);
		}
		
		sqlHelper.finishTransaction();
		
		sqlHelper.startTransaction();
		sqlHelper.addShowForUser(chosenUserID, showID);
		sqlHelper.finishTransaction();
		refreshShows();
		
		
	}
	
	private void refreshShows() {
		ResultSet resultSet = sqlHelper.getShowsForUser(chosenUserID);
		try {
			while(table.getRowCount() != 0) {
				((DefaultTableModel)table.getModel()).removeRow(0);
			}
			int columns = table.getModel().getColumnCount();
			while(resultSet.next() != false) {
				Object[] newRow = new Object[columns];
				newRow[0] = resultSet.getString("tytul");
				newRow[1] = resultSet.getString("stacja");
				newRow[2] = resultSet.getString("opis");
				newRow[3] = resultSet.getString("statusSerialu");
				newRow[4] = resultSet.getInt("epsTotal");
				newRow[5] = resultSet.getInt("epsWatched");
				newRow[6] = resultSet.getInt("serialID");
				newRow[7] = resultSet.getFloat("rating");
				((DefaultTableModel)table.getModel()).insertRow(resultSet.getRow() - 1, newRow);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void deleteShowAtRow(int row) {
		int showID = (int)table.getModel().getValueAt(row, 6);
		
		sqlHelper.startTransaction();
		sqlHelper.deleteShowForUser(chosenUserID, showID);
		sqlHelper.finishTransaction();
		refreshShows();
		
	}
	
	public void showActorsForShowAtRow(int row) {
		ActorsWindow actorsWindow = new ActorsWindow(sqlHelper, (int)table.getModel().getValueAt(row, 6));
	}
	
	private int myParseInt (Object o) {
		if(o == null){		
			return -1;
		} else {
			return Math.toIntExact((long)o);
		}
	}
	
}

class ShowsPopupMenu extends JPopupMenu {
	JMenuItem deleteShow;
	JMenuItem showActors;
	JTable table;
	MainWindow window;
	
	public ShowsPopupMenu(MainWindow window) {
		this.window = window;
		this.table = window.table;
		deleteShow = new JMenuItem("Delete show");
		showActors = new JMenuItem("Show actors");
		this.add(deleteShow);
		this.add(showActors);
		
		deleteShow.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				window.deleteShowAtRow(table.getSelectedRow());				
			}
		});
		
		showActors.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				window.showActorsForShowAtRow(table.getSelectedRow());
			}
		});
	}
	
	public void show(int x, int y) {
		super.show(table, x, y);
	}

	
}
