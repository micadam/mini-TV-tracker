package miniTVTracker;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.JOptionPane;

public class SQLHelper {
	
	private String url = "jdbc:mysql://localhost:3306/tvbase";
	private String username = "tvtracker";
	private String password = "tvpassword";
	private Connection connection;
	
	
	public void initDB() {
		try {
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connection succesful!");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new IllegalStateException("Cannot connect to the database!");
		}
		
		
	}
	
	public void addShow(int ID, String title, String network, String description, String showStatus) {
		String preparedQuery = "CALL addShow(?, ?, ?, ?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, ID);
			preparedStatement.setString(2, title);
			preparedStatement.setString(3, network);
			preparedStatement.setString(4, description);
			preparedStatement.setString(5, showStatus);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	public void addEpisode(int showID, String showTitle, int numberInShow, int numberInSeason, int seasonNumber, String airdate, String description) {
		String preparedQuery = "CALL addEpisode(?, ?, ?, ?, ?, ?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, showID);
			preparedStatement.setString(2, showTitle);
			if(numberInShow != -1) {				
				preparedStatement.setInt(3, numberInShow);
			} else {
				preparedStatement.setNull(3, Types.INTEGER);
			}
			preparedStatement.setInt(4, numberInSeason);
			preparedStatement.setInt(5, seasonNumber);
			preparedStatement.setString(6, airdate);
			preparedStatement.setString(7, description);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void addShowForUser(int userID, int showID) {
		String preparedQuery = "CALL addShowForUser(?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, userID);
			preparedStatement.setInt(2, showID);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public ResultSet getShowsForUser(int userID) {
		String preparedQuery = "CALL getShows(?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, userID);
			preparedStatement.executeQuery();
			return preparedStatement.getResultSet(); 
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet getEpisodesForShow(int showID, int userID) {
		String prepareQuery = "CALL getEpisodesForShow(?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(prepareQuery);
			preparedStatement.setInt(1, showID);
			preparedStatement.setInt(2, userID);
			preparedStatement.executeQuery();
			return preparedStatement.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void deleteShowForUser(int userID, int showID) {
		String preparedQuery = "CALL deleteShowForUser(?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, userID);
			preparedStatement.setInt(2, showID);
			preparedStatement.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public ResultSet getUsers() {
		String preparedQuery = "CALL getUsers()";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.executeQuery();
			return preparedStatement.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void episodeWatched(int episodeID, int userID, int rating) {
		String preparedQuery = "CALL episodeWatched(?, ?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, episodeID);
			preparedStatement.setInt(2, userID);
			preparedStatement.setInt(3, rating);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	public void episodeUnwatched(int episodeID, int userID) {
		String preparedQuery = "CALL episodeUnwatched(?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, episodeID);
			preparedStatement.setInt(2, userID);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	public void addActorForShow(int showID, String name, String role) {
		String preparedQuery = "CALL addActorForShow(?, ?, ?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, showID);
			preparedStatement.setString(2, name);
			preparedStatement.setString(3, role);
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ResultSet getActorsForShow(int showID) {
		String preparedQuery = "CALL getActorsForShow(?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, showID);
			preparedStatement.executeQuery();
			return preparedStatement.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public ResultSet getLatestEpisodes(int userID) {
		String preparedQuery = "CALL getLatestEpisodes(?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, userID);
			preparedStatement.executeQuery();
			return preparedStatement.getResultSet();
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getTitleForShowID(int showID) {
		String preparedQuery = "CALL getTitleForShowID(?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setInt(1, showID);
			preparedStatement.executeQuery();
			ResultSet resultSet = preparedStatement.getResultSet();
			resultSet.next();
			return resultSet.getString("tytul");
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public int addUser(String username) {
		String preparedQuery = "CALL addUser(?)";
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(preparedQuery);
			preparedStatement.setString(1, username);
			preparedStatement.execute();
			ResultSet resultSet = preparedStatement.getResultSet();
			resultSet.next();
			return resultSet.getInt("id");
		} catch (SQLException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	public void startTransaction() {
		try {
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void finishTransaction() {
		try {
			connection.commit();
			connection.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void closeConnecton() {
		try {
			System.out.println("Closing connection to the SQL database");
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public SQLHelper() {
		initDB();
	}

}
