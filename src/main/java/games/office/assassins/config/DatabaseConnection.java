package games.office.assassins.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import lombok.Getter;

/**
 * Handles all connection and configuration with the SQlite database that retains the assassins data.
 */
public class DatabaseConnection {
	/** */
	@Getter
	private Connection connection;

	/**
	 * Open a connection with a SQlite database
	 * @param database_filename The filename where the database is stored
	 * @return True for success
	 */
	public boolean openConnection(String database_filename) {
		closeConnection();

		try {
			String url = "jdbc:sqlite:" + database_filename;
			connection = DriverManager.getConnection(url);
			System.out.println("Established DB connection with file " + database_filename);
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
			return false;
		}

		return true;
	}

	/** Closes a connection to an open database. Does nothing if there is no open connection */
	public void closeConnection() {
		if (connection == null) {
			return;
		}

		try {
			connection.close();
			connection = null;
		}
		catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}
}
