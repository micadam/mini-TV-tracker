package miniTVTracker;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;


public class LoginDialog extends JDialog {
	
	private int chosenID;
	private String chosenName;
	
	public LoginDialog(ResultSet resultSet) {
		
		List<String> usernames = new ArrayList<String>();
		List<Integer> userIDs = new ArrayList<Integer>();
		try {
			while(resultSet.next() != false) {
				usernames.add(resultSet.getString("nazwa"));
				userIDs.add(resultSet.getInt("userID"));
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
			
		setTitle("Select user");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{218, 233, 0};
		gridBagLayout.rowHeights = new int[]{0, 50, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 2;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		getContentPane().add(scrollPane, gbc_scrollPane);
		
		JList list = new JList(usernames.toArray());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(list);
		
		JButton okButton = new JButton("OK");
		GridBagConstraints gbc_okButton = new GridBagConstraints();
		gbc_okButton.insets = new Insets(0, 0, 0, 5);
		gbc_okButton.gridx = 0;
		gbc_okButton.gridy = 1;
		getContentPane().add(okButton, gbc_okButton);
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(list.getSelectedIndex() >= 0) {					
					chosenName = usernames.get(list.getSelectedIndex());
					chosenID = userIDs.get(list.getSelectedIndex());
					dispose();
				}
			}
		});
		
		JButton newUserButton = new JButton("New user");
		GridBagConstraints gbc_newUserButton = new GridBagConstraints();
		gbc_newUserButton.gridx = 1;
		gbc_newUserButton.gridy = 1;
		JDialog dialog = this;
		getContentPane().add(newUserButton, gbc_newUserButton);
		
		newUserButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				chosenName =  (String)JOptionPane.showInputDialog(dialog, "Enter your username",
						"Add user", JOptionPane.PLAIN_MESSAGE, null, null, "");
				if(chosenName != null) {					
					chosenID = -1;
					dispose();
				}
				
				
			}
		});
		
		this.setSize(320, 240);
		this.setModal(true);
		this.setVisible(true);
	}

	
	public String getUsername() {
		return chosenName;
	}
	public int getUserID() {
		return chosenID;
	}
}
