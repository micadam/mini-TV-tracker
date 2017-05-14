package miniTVTracker;

import java.sql.ResultSet;

public class MiniTVTracker {
	
	private MainWindow mainWindow;
	private SQLHelper sqlHelper;
	
	public MiniTVTracker() {
		sqlHelper = new SQLHelper();
		ResultSet resultSet = sqlHelper.getUsers();
		
		
		LoginDialog login = new LoginDialog(resultSet);
		
		String username = login.getUsername();
		int userID = login.getUserID();
		
		sqlHelper.startTransaction();
		if(userID == -1) {
			System.out.println("Adding user " + username);
			userID = sqlHelper.addUser(username);
			if(userID == -1) {
				throw new IllegalStateException();
			}
		}
		sqlHelper.finishTransaction();
		this.mainWindow = new MainWindow(username, userID, sqlHelper);
	}
	
	public static void main(String[] args) {
		MiniTVTracker miniTVTracker = new MiniTVTracker();
	}

}
