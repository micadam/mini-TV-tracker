package miniTVTracker;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;



public class ShowSelectionDialog extends JDialog {

	private int choice = -10;
	
	public ShowSelectionDialog(List<String> showTitles) {
		setTitle("Show selection");
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0, 0};
		gridBagLayout.rowHeights = new int[]{120, 45, 0};
		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		getContentPane().setLayout(gridBagLayout);
		
		System.out.println(showTitles.get(0));;
		
		JList list = new JList(showTitles.toArray());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		GridBagConstraints gbc_list = new GridBagConstraints();
		gbc_list.gridwidth = 2;
		gbc_list.insets = new Insets(0, 0, 5, 0);
		gbc_list.fill = GridBagConstraints.BOTH;
		gbc_list.gridx = 0;
		gbc_list.gridy = 0;
		getContentPane().add(new JScrollPane(list), gbc_list);
		
		JButton selectButton = new JButton("Select");
		GridBagConstraints gbc_selectButton = new GridBagConstraints();
		gbc_selectButton.insets = new Insets(0, 0, 0, 5);
		gbc_selectButton.gridx = 0;
		gbc_selectButton.gridy = 1;
		getContentPane().add(selectButton, gbc_selectButton);
		
		JButton cancelButton = new JButton("Cancel");
		GridBagConstraints gbc_cancelButton = new GridBagConstraints();
		gbc_cancelButton.gridx = 1;
		gbc_cancelButton.gridy = 1;
		getContentPane().add(cancelButton, gbc_cancelButton);
		this.setSize(320, 240);
		this.setModal(true);
		
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				choice = list.getSelectedIndex();
				System.out.println(showTitles.get(choice));
				dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				choice = -1;
				dispose();
			}
		});
		this.setVisible(true);
	}
	
	public int getChoice() {
		return choice;
	}
}
